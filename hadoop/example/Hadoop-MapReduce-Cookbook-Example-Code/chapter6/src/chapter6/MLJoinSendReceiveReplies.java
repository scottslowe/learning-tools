package chapter6;

import java.io.IOException;
import java.text.SimpleDateFormat;

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
 * Joined the replies sent and replies received by each sender
 * 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class MLJoinSendReceiveReplies {
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm:ss z");

    public static class AMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split("\\s");
            String from = tokens[0];
            String replyData = tokens[1];
            context.write(new Text(from), new Text(replyData));
        }
    }

    public static class AReducer extends Reducer<Text, Text, IntWritable, IntWritable> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuffer buf = new StringBuffer("[");

            try {
                int sendReplyCount = 0;
                int receiveReplyCount = 0;
                for (Text val : values) {
                    String strVal = val.toString();
                    buf.append(strVal).append(",");
                    if (strVal.contains("#")) {
                        String[] tokens = strVal.split("#");
                        int repliesOnThisThread = Integer.parseInt(tokens[0]);
                        int selfRepliesOnThisThread = Integer.parseInt(tokens[1]);
                        receiveReplyCount = receiveReplyCount + repliesOnThisThread;
                        sendReplyCount = sendReplyCount - selfRepliesOnThisThread;
                    } else {
                        sendReplyCount = sendReplyCount + Integer.parseInt(strVal);
                    }
                }
                context.write(new IntWritable(sendReplyCount), new IntWritable(receiveReplyCount));
                buf.append("]");
                System.out.println(key + "=" + buf + " " + sendReplyCount + " " + receiveReplyCount);
            } catch (NumberFormatException e) {
                System.out.println("ERROR " + e.getMessage() + " " + buf);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: <in> <out>");
            System.exit(2);
        }

        Job job = new Job(conf, "LogProcessingHitsByLink");
        job.setJarByClass(MLJoinSendReceiveReplies.class);
        job.setMapperClass(AMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);
        job.setReducerClass(AReducer.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
