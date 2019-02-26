package chapter8.adwords;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * This class generates data data to be used by Adwords Balance algorithm. 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class AdwordsBalanceAlgorithmDataGenerator {

    public static class AMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] keyVal = value.toString().split("\\s");
            try {
                if (keyVal[0].startsWith("keyword:")) {
                    context.write(new Text(keyVal[0].replace("keyword:", "")), new Text(keyVal[1]));
                } else if (keyVal[0].startsWith("client")) {
                    List<String[]> bids = new ArrayList<String[]>();
                    double budget = 0;
                    String clientid = keyVal[0];
                    String[] tokens = keyVal[1].split(",");
                    for (String token : tokens) {
                        String[] kp = token.split("=");
                        if (kp[0].equals("budget")) {
                            budget = Double.parseDouble(kp[1]);
                        } else if (kp[0].equals("bid")) {
                            String[] bidData = kp[1].split("\\|");
                            bids.add(bidData);
                        } else {
                            System.out.println("Unknown token " + token + " of line " + value.toString());
                        }
                    }

                    for (String[] bid : bids) {
                        String keyword = bid[0];
                        String bidValue = bid[1];
                        context.write(new Text(keyword), new Text(new StringBuffer().append(clientid).append(",")
                                .append(budget).append(",").append(bidValue).toString()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error:" + e.getMessage());
            }
        }
    }

    public static class AReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            try {
                // we approximate click rate by adding the 1/sales rank for
                // occurance of the word
                String clientid = null;
                String budget = null;
                String bid = null;
                String clickRate = null;

                List<String> bids = new ArrayList<String>();

                for (Text val : values) {
                    if (val.toString().indexOf(",") > 0) {
                        bids.add(val.toString());
                    } else {
                        clickRate = val.toString();
                    }
                }
                if (clickRate == null) {
                    System.out.println("ignored " + bids);
                    return;
                }
                StringBuffer buf = new StringBuffer();
                for (String bidData : bids) {
                    String[] vals = bidData.split(",");
                    clientid = vals[0];
                    budget = vals[1];
                    bid = vals[2];
                    buf.append(clientid).append(",").append(budget).append(",").append(Double.valueOf(bid)).append(",")
                            .append(Math.max(1, Double.valueOf(clickRate)));
                    buf.append("|");
                }
                if (bids.size() > 0) {
                    context.write(key, new Text(buf.toString()));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
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

        Job job = new Job(conf, "AdwordsBalanceAlgorithmDataGenerator");
        job.setJarByClass(AdwordsBalanceAlgorithmDataGenerator.class);
        job.setMapperClass(AMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(AReducer.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
