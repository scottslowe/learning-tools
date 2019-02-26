package chapter9;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class TextOutInvertedIndexer {

	/*
	 * Map Function receives a chunk of an input document as the input and
	 * outputs the term and <docid, 1> pair for each word. We can use a combiner
	 * to optimize the intermediate data communication.
	 */
	public static class IndexingMapper extends
			Mapper<Object, Text, Text, TermFrequencyWritable> {

		private TermFrequencyWritable docFrequency = new TermFrequencyWritable();
		private Text term = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String valString = value.toString().replaceAll("[^a-zA-Z0-9]+"," ");
			StringTokenizer itr = new StringTokenizer(valString);
			FileSplit fileSplit = (FileSplit) context.getInputSplit();
			String fileName = fileSplit.getPath().getName();
			while (itr.hasMoreTokens()) {
				term.set(itr.nextToken());
				docFrequency.set(fileName, 1);
				context.write(term, docFrequency);
			}
		}
	}

	public static class IndexingCombiner extends
			Reducer<Text, TermFrequencyWritable, Text, TermFrequencyWritable> {

		public void reduce(Text key, Iterable<TermFrequencyWritable> values,
				Context context) throws IOException, InterruptedException {

			int count = 0;
			String id = "";
			for (TermFrequencyWritable val : values) {
				count++;
				if (count == 1) {
					id = val.getDocumentID().toString();
				}
			}

			TermFrequencyWritable writable = new TermFrequencyWritable();
			writable.set(id, count);
			context.write(key, writable);
		}
	}

	/**
	 * <p>
	 * Reduce function receives IDs and frequencies of all the documents that
	 * contains the term (Key) as the input. Reduce function outputs the term
	 * and a list of document IDs and the number of occurrences of the term in
	 * each document as the output.
	 * </p>
	 */
	public static class IndexingReducer extends
			Reducer<Text, TermFrequencyWritable, Text, Text> {

		public void reduce(Text key, Iterable<TermFrequencyWritable> values,
				Context context) throws IOException, InterruptedException {

			HashMap<Text, IntWritable> map = new HashMap<Text, IntWritable>();
			for (TermFrequencyWritable val : values) {
				Text docID = new Text(val.getDocumentID());
				int freq = val.getFreq().get();
				if (map.get(docID) != null) {
					map.put(docID, new IntWritable(map.get(docID).get() + freq));
				} else {
					map.put(docID, new IntWritable(freq));
				}
			}

			Iterator<Entry<Text, IntWritable>> it = map.entrySet().iterator();
			StringBuilder strBuilder = new StringBuilder();
			while (it.hasNext()) {
				Entry<Text, IntWritable> pair = it.next();
				strBuilder.append(pair.getKey() + ":" + pair.getValue() + ",");
				it.remove(); // avoids a ConcurrentModificationException }
			}
			context.write(key, new Text(strBuilder.toString()));
		}
	}

	/**
	 * <p>
	 * As input this program takes a set of text files. Create a folder called
	 * input in HDFS (or in local directory if you are running this locally)
	 * <ol>
	 * <li>Option1: You can compile the sample by ant from sample directory. To
	 * do this, you need to have Apache Ant installed in your system. Otherwise,
	 * you can use the complied jar included with the source code. Change
	 * directory to HADOOP_HOME, and copy the hadoop-cookbook.jar to the
	 * HADOOP_HOME. Then run the command > bin/hadoop jar hadoop-cookbook.jar
	 * chapter1.WordCount input output.</li>
	 * <li>As an optional step, copy the "input" directory to the top level of
	 * the IDE based project (eclipse project) that you created for samples. Now
	 * you can run the WordCount class directly from your IDE passing "input
	 * output" as arguments. This will run the sample same as before. Running
	 * MapReduce Jobs from IDE in this manner is very useful for debugging your
	 * MapReduce Jobs.</li>
	 * </ol>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: TextOutInvertedIndexer <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "Inverted Indexer");
		job.setJarByClass(TextOutInvertedIndexer.class);
		job.setMapperClass(IndexingMapper.class);
		job.setReducerClass(IndexingReducer.class);
		job.setCombinerClass(IndexingCombiner.class);
		job.setOutputKeyClass(Text.class);
		job.setMapOutputValueClass(TermFrequencyWritable.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
