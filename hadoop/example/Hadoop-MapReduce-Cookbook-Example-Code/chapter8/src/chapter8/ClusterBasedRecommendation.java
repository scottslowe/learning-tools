package chapter8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
import chapter8.AmazonCustomer.SortableItemData;

public class ClusterBasedRecommendation {
    /**
     * We make recommendations for each users by looking at the items brought by the other users in the same 
     * cluster as this user.
     */
    public static class AMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            AmazonCustomer amazonCustomer = new AmazonCustomer(value.toString().replaceAll("[0-9]+\\s+", ""));
            context.write(new Text(amazonCustomer.clusterID), new Text(amazonCustomer.toString()));
        }
    }

    public static class AReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            List<AmazonCustomer> customerList = new ArrayList<AmazonCustomer>();
            TreeSet<AmazonCustomer.SortableItemData> highestRated1000Items = new TreeSet<AmazonCustomer.SortableItemData>();
            for (Text value : values) {
                AmazonCustomer customer = new AmazonCustomer(value.toString());
                for (ItemData itemData : customer.itemsBrought) {
                    highestRated1000Items.add(customer.new SortableItemData(itemData));
                    if (highestRated1000Items.size() > 1000) {
                        highestRated1000Items.remove(highestRated1000Items.last());
                    }
                }
                customerList.add(customer);
            }

            for (AmazonCustomer amazonCustomer : customerList) {
                List<ItemData> recemndationList = new ArrayList<AmazonCustomer.ItemData>();
                for (SortableItemData sortableItemData : highestRated1000Items) {
                    if (!amazonCustomer.itemsBrought.contains(sortableItemData.itemData)) {
                        recemndationList.add(sortableItemData.itemData);
                    }
                }
                ArrayList<ItemData> finalRecomendations = new ArrayList<ItemData>();
                for (int i = 0; i < 10; i++) {
                    finalRecomendations.add(recemndationList.get(i));
                }

                context.write(new Text(amazonCustomer.customerID), new Text(finalRecomendations.toString()));
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
        job.setJarByClass(ClusterBasedRecommendation.class);
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
