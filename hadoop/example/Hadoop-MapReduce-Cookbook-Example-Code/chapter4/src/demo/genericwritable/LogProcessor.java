package demo.genericwritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


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

		Configuration conf = new Configuration();
		Job job = new Job(conf, "log-analysis");

		job.setJarByClass(LogProcessor.class);
		job.setMapperClass(LogProcessorMap.class);
		job.setReducerClass(LogProcessorReduce.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
		job.setMapOutputValueClass(MultiValueWritable.class);
		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));		
		job.setNumReduceTasks(numReduce);
	
		int exitStatus = job.waitForCompletion(true) ? 0 : 1;
		return exitStatus;
	}
}