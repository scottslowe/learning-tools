package chapter5;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;

/**
 * Calculate the average of Gross National Income (GNI) per capita by country.
 * Dataset can be found from http://hdr.undp.org/en/statistics/data/.
 */

public class AverageGINByCountryCalcualtor {

    static class Mapper extends TableMapper<ImmutableBytesWritable, DoubleWritable> {

        private int numRecords = 0;

        @Override
        public void map(ImmutableBytesWritable row, Result values, Context context) throws IOException {
            byte[] results = values.getValue("ByCountry".getBytes(), "gnip".getBytes());

            // extract userKey from the compositeKey (userId + counter)
            ImmutableBytesWritable userKey = new ImmutableBytesWritable("ginp".getBytes());
            try {
                context.write(userKey, new DoubleWritable(Bytes.toDouble(results)));
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            numRecords++;
            if ((numRecords % 50) == 0) {
                context.setStatus("mapper processed " + numRecords + " records so far");
            }
        }
    }

    public static class Reducer extends TableReducer<ImmutableBytesWritable, DoubleWritable, ImmutableBytesWritable> {

        public void reduce(ImmutableBytesWritable key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            double sum = 0;
            int count = 0;
            for (DoubleWritable val : values) {
                sum += val.get();
                count++;
            }

            Put put = new Put(key.get());
            put.add(Bytes.toBytes("data"), Bytes.toBytes("average"), Bytes.toBytes(sum / count));
            System.out.println("Processed " + count + " values and avergae =" + sum / count);
            context.write(key, put);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Job job = new Job(conf, "AverageGINByCountryCalcualtor");
        job.setJarByClass(AverageGINByCountryCalcualtor.class);
        Scan scan = new Scan();
        scan.addFamily("ByCountry".getBytes());
        scan.setFilter(new FirstKeyOnlyFilter());
        TableMapReduceUtil.initTableMapperJob("HDI", scan, Mapper.class, ImmutableBytesWritable.class,
                DoubleWritable.class, job);
        TableMapReduceUtil.initTableReducerJob("HDIResult", Reducer.class, job);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
