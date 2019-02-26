package chapter6;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
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
 * Finds the number of hits received by each URL
 * 
 * @author Srinath Perera (hemapani@apache.org)
 */

public class WeblogTimeOfDayHistogramCreator {
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMMMM/yyyy:hh:mm:ss z");

    public static final Pattern httplogPattern = Pattern
            .compile("([^\\s]+) - - \\[(.+)\\] \"([^\\s]+) (/[^\\s]*) HTTP/[^\\s]+\" [^\\s]+ ([0-9]+)");

    /**
   */
    public static class AMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
        private final static IntWritable one = new IntWritable(1);

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            try {
                Matcher matcher = httplogPattern.matcher(value.toString());
                if (matcher.matches()) {
                    String timeAsStr = matcher.group(2);
                    Date time = dateFormatter.parse(timeAsStr);
                    Calendar calendar = GregorianCalendar.getInstance(); // creates
                                                                         // a
                                                                         // new
                                                                         // calendar
                                                                         // instance
                    calendar.setTime(time); // assigns calendar to given date
                    int hours = calendar.get(Calendar.HOUR_OF_DAY); // gets hour
                                                                    // in 24h
                                                                    // format
                    context.write(new IntWritable(hours), one);
                }
            } catch (ParseException e) {
                // we ignore if there is few misformatted entries
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * Reduce function receives all the values that has the same key as the
     * input, and it output the key and the number of occurrences of the key as
     * the output.
     * </p>
     */
    public static class AReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
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

        Job job = new Job(conf, "WeblogTimeOfDayHistogramCreator");
        job.setJarByClass(WeblogTimeOfDayHistogramCreator.class);
        job.setMapperClass(AMapper.class);
        job.setReducerClass(AReducer.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
