package chapter6;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Find number of owner and replies received by each thread 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class MLReceiveReplyProcessor {
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE dd MMM yyyy hh:mm:ss z");

    public static class AMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split("#");
            String from = tokens[0];
            String subject = tokens[1];
            String date = tokens[2].replaceAll(",", "");
            subject = subject.replaceAll("Re:", "");
            context.write(new Text(subject), new Text(date + "#" + from));
        }
    }

    public static class AReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            try {
                TreeMap<Long, String> replyData = new TreeMap<Long, String>();
                for (Text val : values) {
                    String[] tokens = val.toString().split("#");
                    if (tokens.length != 2) {
                        throw new IOException("Unexpected token " + val.toString());
                    }
                    String from = tokens[1];
                    Date date = dateFormatter.parse(tokens[0]);
                    replyData.put(date.getTime(), from);
                }
                String owner = replyData.get(replyData.firstKey());
                int replyCount = replyData.size();
                int selfReplies = 0;
                for (String from : replyData.values()) {
                    if (owner.equals(from)) {
                        selfReplies++;
                    }
                }
                replyCount = replyCount - selfReplies;

                context.write(new Text(owner), new Text(replyCount + "#" + selfReplies));
            } catch (Exception e) {
                System.out.println("ERROR:" + e.getMessage());
                return;
                // throw new IOException(e);
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

        Job job = new Job(conf, "MLReceiveReplyProcessor");
        job.setJarByClass(MLReceiveReplyProcessor.class);
        job.setMapperClass(AMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        // Uncomment this to
        // job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(AReducer.class);
        job.setInputFormatClass(MboxFileFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
