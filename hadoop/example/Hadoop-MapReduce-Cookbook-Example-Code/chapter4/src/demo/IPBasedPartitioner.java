package demo;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;


public class IPBasedPartitioner extends Partitioner<Text, IntWritable>{

	@Override
	public int getPartition(Text ipAddress, IntWritable value, int numPartitions) {
		StringTokenizer tokenizer  = new StringTokenizer(ipAddress.toString(), ".");
		if (tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();			
			return ((token.hashCode() & Integer.MAX_VALUE) % numPartitions);
		}
		return 0;
	}
}
