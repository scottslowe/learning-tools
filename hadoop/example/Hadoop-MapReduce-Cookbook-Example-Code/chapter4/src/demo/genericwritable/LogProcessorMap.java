package demo.genericwritable;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class LogProcessorMap extends
		Mapper<Object, Text, Text, MultiValueWritable> {
	private Text userHostText = new Text();
	private Text requestText = new Text();
	private IntWritable bytesWritable = new IntWritable();

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {
		String logEntryPattern = "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+)";

		Pattern p = Pattern.compile(logEntryPattern);
		Matcher matcher = p.matcher(value.toString());
		if (!matcher.matches()) {
			return;
		}

		String userHost = matcher.group(1);
		userHostText.set(userHost); 
		String request = matcher.group(5);
		requestText.set(request);	
		int bytes = Integer.parseInt(matcher.group(7));
		bytesWritable.set(bytes);

		context.write(userHostText, new MultiValueWritable(requestText));
		context.write(userHostText, new MultiValueWritable(bytesWritable));
	}
}
