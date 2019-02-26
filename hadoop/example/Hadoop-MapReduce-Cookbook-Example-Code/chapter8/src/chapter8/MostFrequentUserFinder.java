package chapter8;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

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

import chapter8.AmazonCustomer.ItemData;

/**
 * Find number of owner and replies received by each thread 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class MostFrequentUserFinder {
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE dd MMM yyyy hh:mm:ss z");

    public static class AMapper extends Mapper<Object, Text, Text, Text> {

public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    //System.out.println(key + "="+ value);
    try {
    List<AmazonCustomer> customerList = AmazonCustomer.parseAItemLine(value.toString());
    for(AmazonCustomer customer: customerList){
        context.write(new Text(customer.customerID), new Text(customer.toString()));
        //System.out.println(customer.customerID + "=" + customer.toString());
    }
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error:" +e.getMessage());
    }
}
    }

    public static class AReducer extends Reducer<Text, Text, IntWritable, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            AmazonCustomer  customer = new AmazonCustomer(); 
            customer.customerID = key.toString(); 
            
            for(Text value: values){
                Set<ItemData> itemsBrought = new AmazonCustomer(value.toString()).itemsBrought; 
                for(ItemData itemData: itemsBrought){
                    customer.itemsBrought.add(itemData); 
                }
            }
            if(customer.itemsBrought.size() > 5){
                context.write(new IntWritable(customer.itemsBrought.size()), new Text(customer.toString()));
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

        Job job = new Job(conf, "MostFrequentUserFinder");
        job.setJarByClass(MostFrequentUserFinder.class);
        job.setMapperClass(AMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        // Uncomment this to
        // job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(AReducer.class);
        job.setInputFormatClass(AmazonDataFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
