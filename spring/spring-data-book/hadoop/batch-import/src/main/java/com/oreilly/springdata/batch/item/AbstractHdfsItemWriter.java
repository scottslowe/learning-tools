package com.oreilly.springdata.batch.item;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.hadoop.fs.FsShell;

import com.oreilly.springdata.hadoop.streaming.HdfsTextFileWriterFactory;

public abstract class AbstractHdfsItemWriter<T> implements ItemWriter<T> {

	private final AtomicLong counter = new AtomicLong(0L);
	
	private final AtomicLong bytesWritten = new AtomicLong(0L);
	
	private volatile boolean initialized;
	
	private String baseFilename = HdfsTextFileWriterFactory.DEFAULT_BASE_FILENAME;
	private String basePath = HdfsTextFileWriterFactory.DEFAULT_BASE_PATH;
	private String fileSuffix = HdfsTextFileWriterFactory.DEFAULT_FILE_SUFFIX;
	private long rolloverThresholdInBytes = HdfsTextFileWriterFactory.DEFAULT_ROLLOVER_THRESHOLD_IN_BYTES;
	
	public abstract void write(List<? extends T> items) throws Exception;
	
	public abstract FileSystem getFileSystem();
	
	protected void initializeCounterIfNecessary() {
		if (!initialized) {
			FsShell fsShell = new FsShell(getFileSystem().getConf(), getFileSystem());
			int maxCounter = 0;
			boolean foundFile = false;
			Collection<FileStatus> fileStats = fsShell.ls(this.getBasePath());
			for (FileStatus fileStatus : fileStats) {
				String shortName = fileStatus.getPath().getName();
				int counterFromName = getCounterFromName(shortName);
				if (counterFromName != -1) {
					foundFile = true;
				}
				if (counterFromName > maxCounter) {
					maxCounter = counterFromName;
				}
			}
			if (foundFile) {
				this.setCounter(maxCounter+1);				
			}
			
			initialized = true;
		}
	}


	protected int getCounterFromName(String shortName) {
		Pattern pattern = Pattern.compile("([\\d+]{1,})");
		Matcher matcher = pattern.matcher(shortName);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		} 
		return -1;			
	}
	
	public long getRolloverThresholdInBytes() {
		return rolloverThresholdInBytes;
	}

	public void setRolloverThresholdInBytes(long rolloverThresholdInBytes) {
		this.rolloverThresholdInBytes = rolloverThresholdInBytes;
	}


	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	
	public String getBaseFilename() {
		return baseFilename;
	}

	public void setBaseFilename(String baseFilename) {
		this.baseFilename = baseFilename;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public long getCounter() {
		return counter.get();
	}
	
	public void setCounter(long value) {
		counter.set(value);
	}
	
	public void incrementCounter() {
		counter.incrementAndGet();
	}
	
	public void incrementBytesWritten(long bytesWritten) {
		this.bytesWritten.addAndGet(bytesWritten);
	}
	
	public void resetBytesWritten() {
		this.bytesWritten.set(0L);
	}
	
	public long getBytesWritten() {
		return bytesWritten.get();
	}
	
	public String getFileName() {
		return basePath + baseFilename + "-" + getCounter() + "." + fileSuffix;
	}

}
