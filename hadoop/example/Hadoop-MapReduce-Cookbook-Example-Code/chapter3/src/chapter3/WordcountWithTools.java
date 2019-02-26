package chapter3;

import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import chapter1.WordCount;
import chapter1.WordCount.IntSumReducer;
import chapter1.WordCount.TokenizerMapper;

public class WordcountWithTools extends Configured implements Tool {

    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("chapter3.WordCountWithTools WordCount <inDir> <outDir>");
            ToolRunner.printGenericCommandUsage(System.out);
            System.out.println("");
            return -1;
        }

        System.out.println(Arrays.toString(args));

        Job job = new Job(getConf(), "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        // Uncomment this to
        // job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new WordcountWithTools(), args);
        System.exit(res);
    }

}
