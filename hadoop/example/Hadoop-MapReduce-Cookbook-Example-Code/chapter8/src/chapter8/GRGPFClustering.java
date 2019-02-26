package chapter8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * This class implement the GRGPF Clustering Algorihtm
 */

public class GRGPFClustering {
    private static Pattern pattern = Pattern.compile("([^=]+)=(.*)");
    private static List<AmazonCustomer> clusterCentrodis = new ArrayList<AmazonCustomer>();

    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("clusters.data"), 100 * 1024);
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    AmazonCustomer customer = new AmazonCustomer(matcher.group(2));
                    clusterCentrodis.add(customer);
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Use the initial cluster using that clusters as the tree, assign users to
     * clustersat the reduce verify the results and merge clusters if needed
     */

    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE dd MMM yyyy hh:mm:ss z");

    public static class AMapper extends Mapper<Object, Text, Text, Text> {

public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    AmazonCustomer amazonCustomer = new AmazonCustomer(value.toString().replaceAll("[0-9]+\\s+", ""));
    double mindistance = Double.MAX_VALUE;
    AmazonCustomer closestCluster = null;
    for (AmazonCustomer centriod : clusterCentrodis) {
        double distance = amazonCustomer.getDistance(centriod);
        if (distance < 5) {
            System.out.println(amazonCustomer.customerID + " dist =" + distance);
        }
        if (distance < mindistance) {
            mindistance = distance;
            closestCluster = centriod;
        }
    }
    amazonCustomer.clusterID = closestCluster.clusterID;
    if (Integer.parseInt(amazonCustomer.clusterID.trim()) != 0) {
        System.out.println(amazonCustomer.customerID + " cluster =" + closestCluster.clusterID);
    }
    context.write(new Text(closestCluster.clusterID), new Text(amazonCustomer.toString()));
}
    }

    public static class AReducer extends Reducer<Text, Text, Text, Text> {

public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    // TODO for better results, we can recalculate the centriods, merge
    // or split clusters based on the distance from centroid, and rerun
    // the algorithm again.
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

        Job job = new Job(conf, "GRGPFClustering");
        job.setJarByClass(GRGPFClustering.class);
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
