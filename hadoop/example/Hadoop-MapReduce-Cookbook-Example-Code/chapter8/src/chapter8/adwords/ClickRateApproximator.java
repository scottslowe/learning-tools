package chapter8.adwords;

import java.io.IOException;
import java.util.List;

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

import chapter8.AmazonCustomer;
import chapter8.AmazonCustomer.ItemData;
import chapter8.AmazonDataFormat;
import chapter8.MostFrequentUserFinder;

/**
 * This class approximates the click through rate for each keyword using the keyword's sale's ranks
 * @author Srinath Perera (hemapani@apache.org)
 */
public class ClickRateApproximator {
    public static class AMapper extends Mapper<Object, Text, Text, IntWritable> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            try {
                ItemData itemData = null; 
                List<AmazonCustomer> customerList = AmazonCustomer.parseAItemLine(value.toString());
                if(customerList.size() == 0){
                    return;
                }
                for (AmazonCustomer customer : customerList) {
                    itemData = customer.itemsBrought.iterator().next();
                    break;
                }
                
                if(itemData.title == null || itemData.salesrank == 0){
                    System.out.println("Igonred "+ value.toString() + "salesrank="+ itemData.salesrank);
                    return;
                }
                String[] tokens = itemData.title.split("\\s");
                for(String token: tokens){
                    if(token.length() > 3){
                        context.write(new Text(token), new IntWritable(itemData.salesrank));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error:" + e.getMessage());
            }
        }
    }

    public static class AReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            double clickrate = 0; 
            // we approximate click rate by adding the 1/sales rank for occurance of the word
            for(IntWritable val: values){
                if(val.get() > 1){
                    clickrate = clickrate + 1000/Math.log(val.get());
                }else{
                    clickrate = clickrate + 1000;
                }
            }
            context.write(new Text("keyword:" +key.toString()), new IntWritable((int)clickrate)); 
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: <in> <out>");
            System.exit(2);
        }

        Job job = new Job(conf, "ClickRateApproximator");
        job.setJarByClass(MostFrequentUserFinder.class);
        job.setMapperClass(AMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setReducerClass(AReducer.class);
        job.setInputFormatClass(AmazonDataFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
