package chapter8;

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

import chapter8.AmazonCustomer.ItemData;

public class ContentBasedRecommendation {
    /**
     * We make recommendations for a user by looking at the items similar to the items that has brought by the user. 
     */

    public static class AMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            AmazonCustomer amazonCustomer = new AmazonCustomer(value.toString().replaceAll("[0-9]+\\s+", ""));

            List<String> recemndations = new ArrayList<String>();
            for (ItemData itemData : amazonCustomer.itemsBrought) {
                recemndations.addAll(itemData.similarItems);
            }

            for (ItemData itemData : amazonCustomer.itemsBrought) {
                recemndations.remove(itemData.itemID);
            }

            ArrayList<String> finalRecemndations = new ArrayList<String>();
            for (int i = 0; i < Math.min(10, recemndations.size()); i++) {
                finalRecemndations.add(recemndations.get(i));
            }
            context.write(new Text(amazonCustomer.customerID), new Text(finalRecemndations.toString()));
        }
    }

    public static class AReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value : values) {
                context.write(key, value);
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

        Job job = new Job(conf, "ClusterBasedRecommendation");
        job.setJarByClass(ContentBasedRecommendation.class);
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
