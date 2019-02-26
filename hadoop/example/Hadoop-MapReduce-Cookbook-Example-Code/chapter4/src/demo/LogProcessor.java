package demo;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import demo.LogProcessorMap.LOG_PROCESSOR_COUNTER;

public class LogProcessor extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new LogProcessor(),
				args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage:  <input_path> <output_path> <num_reduce_tasks>");
			System.exit(-1);
		}

		/* input parameters */
		String inputPath = args[0];	
		String outputPath = args[1];
		int numReduce = Integer.parseInt(args[2]); 

		Job job = new Job(getConf(), "log-analysis");
		
		//DistributedCache.addCacheArchive(new URI("/user/thilina/ip2locationdb.tar.gz#ip2locationdb"), job.getConfiguration());

		job.setJarByClass(LogProcessor.class);
		job.setMapperClass(LogProcessorMap.class);
		job.setReducerClass(LogProcessorReduce.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setInputFormatClass(LogFileInputFormat.class);
	    job.setPartitionerClass(IPBasedPartitioner.class);
		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));		
		job.setNumReduceTasks(numReduce);			
		//job.setOutputFormatClass(SequenceFileOutputFormat.class);
	
		int exitStatus = job.waitForCompletion(true) ? 0 : 1;
		
		Counters counters = job.getCounters();
		Counter badRecordsCounter = counters.findCounter(LOG_PROCESSOR_COUNTER.BAD_RECORDS);
		System.out.println("# of Bad Records :"+badRecordsCounter.getValue());
		return exitStatus;
	}
}