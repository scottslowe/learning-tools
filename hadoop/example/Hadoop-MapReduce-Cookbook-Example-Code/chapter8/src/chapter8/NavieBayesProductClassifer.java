package chapter8;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
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
 * This class generate data to be used by Naive Bayes Classifier. 
 * @author Srinath Perera (hemapani@apache.org)
 */
public class NavieBayesProductClassifer {
    private static final Text TOTAL = new Text("total");
    private static final Text RCOUNT_GT_60 = new Text("reviewCount>60");
    private static final Text PREIVEWS_GT_30 = new Text("postiveReviews>30");
    private static final Text SIMILAR_ITEMS_GT_150 = new Text("similarItemCount>150");
    
    public static class AMapper extends Mapper<Object, Text, Text, BooleanWritable> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            List<AmazonCustomer> customerList = AmazonCustomer.parseAItemLine(value.toString());
            int salesRank = -1;
            int reviewCount = 0;
            int postiveReviews = 0;
            int similarItemCount = 0;

            for (AmazonCustomer customer : customerList) {
                ItemData itemData = customer.itemsBrought.iterator().next();
                reviewCount++;
                if (itemData.rating > 3) {
                    postiveReviews++;
                }
                similarItemCount = similarItemCount + itemData.similarItems.size();
                if (salesRank == -1) {
                    salesRank = itemData.salesrank;
                }
            }

            boolean isInFirst10k = (salesRank <= 10000);
            context.write(TOTAL, new BooleanWritable(isInFirst10k));
            if (reviewCount > 60) {
                context.write(RCOUNT_GT_60, new BooleanWritable(isInFirst10k));
            }
            if (postiveReviews > 30) {
                context.write(PREIVEWS_GT_30, new BooleanWritable(isInFirst10k));
            }
            if (similarItemCount > 150) {
                context.write(SIMILAR_ITEMS_GT_150, new BooleanWritable(isInFirst10k));
            }
        }
    }

    public static class AReducer extends Reducer<Text, BooleanWritable, Text, DoubleWritable> {

        public void reduce(Text key, Iterable<BooleanWritable> values, Context context) throws IOException,
                InterruptedException {
            int total = 0;
            int matches = 0;
            for (BooleanWritable value : values) {
                total++;
                if (value.get()) {
                    matches++;
                }
            }
            context.write(new Text(key), new DoubleWritable((double) matches / total));
        }
    }

    
public static boolean classifyItem(int similarItemCount, int reviewCount, int postiveReviews){
    double reviewCountGT60 = 0.8; 
    double postiveReviewsGT30 = 0.9; 
    double similarItemCountGT150 = 0.7; 
    double a , b, c; 
    
    if (reviewCount > 60) {
        a = reviewCountGT60; 
    }else{
        a= 1 - reviewCountGT60; 
    }
    if (postiveReviews > 30) {
        b = postiveReviewsGT30; 
    }else{
        b = 1- postiveReviewsGT30;
    }
    if (similarItemCount > 150) {
        c = similarItemCountGT150; 
    }else{
        c = 1- similarItemCountGT150;
    }
    double p = a*b*c/ (a*b*c + (1-a)*(1-b)*(1-c)); 
    return p > 0.5; 
}
    
    
    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: <in> <out>");
            System.exit(2);
        }

        Job job = new Job(conf, "NavieBayesProductClassifer");
        job.setJarByClass(NavieBayesProductClassifer.class);
        job.setMapperClass(AMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(BooleanWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        // Uncomment this to
        // job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(AReducer.class);
        job.setInputFormatClass(AmazonDataFormat.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
