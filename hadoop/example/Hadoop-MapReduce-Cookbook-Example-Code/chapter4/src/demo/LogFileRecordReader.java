package demo;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;


public class LogFileRecordReader  extends RecordReader<LongWritable, LogWritable>{

	LineRecordReader lineReader;
	LogWritable value;
	
	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext attempt)
			throws IOException, InterruptedException {
		lineReader = new LineRecordReader();
		lineReader.initialize(inputSplit, attempt);		
				
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!lineReader.nextKeyValue())
		{
			return false;
		}
		String logEntryPattern = "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+)";

		Pattern p = Pattern.compile(logEntryPattern);
		Matcher matcher = p.matcher(lineReader.getCurrentValue().toString());
		if (!matcher.matches()) {
			System.out.println("Bad Record:"+ lineReader.getCurrentValue());
		    return nextKeyValue();
		}
		
		String userIP = matcher.group(1);
		String timestamp = matcher.group(4);
		String request = matcher.group(5);
		int status = Integer.parseInt(matcher.group(6));
		int bytes = Integer.parseInt(matcher.group(7));
		
		value = new LogWritable();
		value.set(userIP, timestamp, request,
				status, bytes);
		return true;
	}
	
	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		return lineReader.getCurrentKey();
	}

	@Override
	public LogWritable getCurrentValue() throws IOException,
			InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return lineReader.getProgress();
	}
	
	@Override
	public void close() throws IOException {
		lineReader.close();		
	}

}
