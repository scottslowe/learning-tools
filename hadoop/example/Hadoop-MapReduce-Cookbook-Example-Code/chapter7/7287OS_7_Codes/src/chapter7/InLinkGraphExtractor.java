package chapter7;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;

/**
 * Generate the InLinks graph from the Nutch 2.1 HBase data base.
 */

public class InLinkGraphExtractor {

	static class Mapper extends
			TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {

		@Override
		public void map(ImmutableBytesWritable sourceWebPage, Result values,
				Context context) throws IOException {
			List<KeyValue> results = values.list();			
			for (KeyValue keyValue : results) {
				ImmutableBytesWritable outLink = new ImmutableBytesWritable(
						keyValue.getQualifier());
				try {
					context.write(outLink, sourceWebPage);
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}			
		}
	}


	public static class Reducer
			extends
			TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {

		public void reduce(ImmutableBytesWritable key,
				Iterable<ImmutableBytesWritable> values, Context context)
				throws IOException, InterruptedException {
			Put put = new Put(key.get());
			for (ImmutableBytesWritable immutableBytesWritable : values) {
				put.add(Bytes.toBytes("il"),immutableBytesWritable.get(),Bytes.toBytes("link"));
			}

			context.write(key, put);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = HBaseConfiguration.create();
		Job job = new Job(conf, "InLinkGraphExtractor");
		job.setJarByClass(InLinkGraphExtractor.class);
		Scan scan = new Scan();
		scan.addFamily("ol".getBytes());
		// scan.setStopRow(...);
		TableMapReduceUtil
				.initTableMapperJob("webpage", scan, Mapper.class,
						ImmutableBytesWritable.class,
						ImmutableBytesWritable.class, job);
		TableMapReduceUtil.initTableReducerJob("linkdata", Reducer.class, job);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
