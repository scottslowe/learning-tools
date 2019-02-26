package demo.jobdependency;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import demo.LogFileInputFormat;
import demo.LogProcessorMap;
import demo.LogProcessorReduce;

public class JobControlDemo extends Configured implements Tool{
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new JobControlDemo(),
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
		String intPath = outputPath + "Job1";
		int numReduce = Integer.parseInt(args[2]); 
		

		Job job1 = new Job(getConf(), "log-grep");
		job1.setJarByClass(RegexMapper.class);
		job1.setMapperClass(RegexMapper.class);	
	    job1.setOutputKeyClass(Text.class);
	    job1.setOutputValueClass(NullWritable.class);
		FileInputFormat.setInputPaths(job1, new Path(inputPath));
		FileOutputFormat.setOutputPath(job1, new Path(intPath));		
		job1.setNumReduceTasks(0);	

		Job job2 = new Job(getConf(), "log-analysis");
		job2.setJarByClass(LogProcessorMap.class);
		job2.setMapperClass(LogProcessorMap.class);
		job2.setReducerClass(LogProcessorReduce.class);
	    job2.setOutputKeyClass(Text.class);
	    job2.setOutputValueClass(IntWritable.class);
	    job2.setInputFormatClass(LogFileInputFormat.class);	    
		FileInputFormat.setInputPaths(job2, new Path(intPath+"/part*"));
		FileOutputFormat.setOutputPath(job2, new Path(outputPath));		
		job2.setNumReduceTasks(numReduce);		
		
		
		ControlledJob controlledJob1 =  new ControlledJob(job1.getConfiguration());
		ControlledJob controlledJob2 =  new ControlledJob(job2.getConfiguration());
		controlledJob2.addDependingJob(controlledJob1);
		
		JobControl jobControl = new JobControl("JobControlDemoGroup");
		jobControl.addJob(controlledJob1);
		jobControl.addJob(controlledJob2);
		
		Thread jobControlThread = new Thread(jobControl);
		jobControlThread.start();
		
		while (!jobControl.allFinished()){
			Thread.sleep(500);
		}
	
		jobControl.stop();
		return 0;
	}
}
