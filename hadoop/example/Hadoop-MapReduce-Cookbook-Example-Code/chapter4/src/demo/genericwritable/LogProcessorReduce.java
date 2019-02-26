package demo.genericwritable;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;


public class LogProcessorReduce extends
		Reducer<Text,MultiValueWritable,Text,Text> {
   private Text result = new Text();

   public void reduce(Text key, Iterable<MultiValueWritable> values, 
                      Context context) throws IOException, InterruptedException {
     int sum = 0;
     StringBuilder requests = new StringBuilder();
     for (MultiValueWritable multiValueWritable : values) {
		Writable writable = multiValueWritable.get();
		if (writable instanceof IntWritable){
			sum += ((IntWritable)writable).get();
		}else{
			requests.append(((Text)writable).toString());
			requests.append("\t");
		}
	}
     
     result.set(sum + "\t"+requests);
     context.write(key, result);
   }
}
