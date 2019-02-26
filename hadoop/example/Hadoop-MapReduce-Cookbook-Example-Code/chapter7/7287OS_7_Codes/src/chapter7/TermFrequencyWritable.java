package chapter7;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class TermFrequencyWritable implements Writable {

	private Text documentID;
	private IntWritable freq;

	public TermFrequencyWritable() {
		this.documentID = new Text();
		this.freq = new IntWritable();
	}

	public void set(String docID, int freq) {
		this.documentID.set(docID);
		this.freq.set(freq);
	}

	@Override
	public void readFields(DataInput dataInput) throws IOException {
		documentID.readFields(dataInput);
		freq.readFields(dataInput);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		documentID.write(dataOutput);
		freq.write(dataOutput);
	}

	@Override
	public String toString() {
		return documentID.toString() + ":" + freq.get();
	}

	public Text getDocumentID() {
		return documentID;
	}

	public IntWritable getFreq() {
		return freq;
	}
}