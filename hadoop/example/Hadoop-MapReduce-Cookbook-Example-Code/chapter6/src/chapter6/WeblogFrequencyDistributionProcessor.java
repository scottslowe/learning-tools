package chapter6;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Sorts the number of hits received by each URL 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class WeblogFrequencyDistributionProcessor {
    public static final Pattern httplogPattern = Pattern.compile("([^\\s]+) - - \\[(.+)\\] \"([^\\s]+) (/[^\\s]*) HTTP/[^\\s]+\" [^\\s]+ ([0-9]+)"); 

  public static class AMapper 
       extends Mapper<Object, Text, IntWritable, Text>{
    
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        String[] tokens = value.toString().split("\\s");
        context.write(new IntWritable(Integer.parseInt(tokens[1])), new Text(tokens[0]));
    }
  }
  
  /**
   * <p>Reduce function receives all the values that has the same key as the input, and it output the key 
   * and the number of occurrences of the key as the output.</p>  
   */
  public static class AReducer 
       extends Reducer<IntWritable, Text, Text ,IntWritable> {
    public void reduce(IntWritable key, Iterable<Text> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      Iterator<Text> iterator = values. iterator(); 
      if(iterator.hasNext()){
          context.write(iterator.next(), key);
      }
    }
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
      JobConf conf = new JobConf();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: <in> <out>");
      System.exit(2);
    }
        
    Job job = new Job(conf, "WeblogFrequencyDistributionProcessor");
    job.setJarByClass(WeblogFrequencyDistributionProcessor.class);
    job.setMapperClass(AMapper.class);
    job.setReducerClass(AReducer.class);
    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

