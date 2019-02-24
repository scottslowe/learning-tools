package com.oreilly.springdata.batch.item;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.util.Assert;

public class HdfsTextItemWriter<T> extends AbstractHdfsItemWriter<T> implements InitializingBean {

	private static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");
	 
	private FileSystem fileSystem;
	private FSDataOutputStream fsDataOutputStream;
    private LineAggregator<T> lineAggregator;
    
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

	private volatile String charset = "UTF-8";
	
	public HdfsTextItemWriter(FileSystem fileSystem) {
		Assert.notNull(fileSystem, "Hadoop FileSystem must not be null.");
		this.fileSystem = fileSystem;
	}
	
	@Override
	public void write(List<? extends T> items) throws Exception {
		initializeCounterIfNecessary();
		prepareOutputStream();
		copy(getItemsAsBytes(items), this.fsDataOutputStream);
	}

	
	private void prepareOutputStream() throws IOException {
		boolean found = false;
		Path name = null;
		
		//TODO improve algorithm
		while (!found) {
			name = new Path(getFileName());
			// If it doesn't exist, create it.  If it exists, return false
			if (getFileSystem().createNewFile(name)) {	
				found = true;
				this.resetBytesWritten();
				this.fsDataOutputStream = this.getFileSystem().append(name);
			}
			else {
				if (this.getBytesWritten() >= getRolloverThresholdInBytes()) {
					close();
					incrementCounter();
				}
				else {
					found = true;
				}
			}
		}
	}
	
	
	/**
	 * Simple not optimized copy
	 */
	public void copy(byte[] in, FSDataOutputStream out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No OutputStream specified");
		out.write(in);	
		incrementBytesWritten(in.length);
	}
	
	@Override
	public FileSystem getFileSystem() {
		return this.fileSystem;
	}
	
	/**
	 * Extracts the payload as a byte array.  
	 * @param message
	 * @return
	 */
	private byte[] getItemsAsBytes(List<? extends T> items) {
		
		StringBuilder lines = new StringBuilder();
		for (T item: items) {
			lines.append(lineAggregator.aggregate(item) + lineSeparator);
		}
		try {
			return lines.toString().getBytes(this.charset);
		} catch (UnsupportedEncodingException e) {
			   throw new WriteFailedException("Could not write data.", e);
		}
	}


	public void close() {
		if (fsDataOutputStream != null) {
			IOUtils.closeStream(fsDataOutputStream);
		}
	}
	
    /**
     * Public setter for the {@link LineAggregator}. This will be used to
     * translate the item into a line for output.
     * 
     * @param lineAggregator the {@link LineAggregator} to set
     */
    public void setLineAggregator(LineAggregator<T> lineAggregator) {
            this.lineAggregator = lineAggregator;
    }

	@Override
	public void afterPropertiesSet() throws Exception {		
		Assert.notNull(lineAggregator, "A LineAggregator must be provided.");
	}



}
