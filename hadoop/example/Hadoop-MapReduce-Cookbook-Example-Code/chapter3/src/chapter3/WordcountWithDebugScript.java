package chapter3;

import java.net.URI;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordcountWithDebugScript {
    private static final String scriptFileLocation = "resources/chapter3/debugscript";
    private static final String HDFS_ROOT = "/debug";

    public static void setupFailedTaskScript(JobConf conf) throws Exception {

      // create a directory on HDFS where we'll upload the fail scripts
      FileSystem fs = FileSystem.get(conf);
      //Path debugDir = new Path("/debug");
      Path debugDir = new Path(HDFS_ROOT);

      // who knows what's already in this directory; let's just clear it.
      if (fs.exists(debugDir)) {
        fs.delete(debugDir, true);
      }

      // ...and then make sure it exists again
      fs.mkdirs(debugDir);

      // upload the local scripts into HDFS
      fs.copyFromLocalFile(new Path(scriptFileLocation), new Path(HDFS_ROOT+"/fail-script"));
      
      FileStatus[] list = fs.listStatus(new Path(HDFS_ROOT));
      if(list == null || list.length == 0){
          System.out.println("No File found");
      }else{
          for(FileStatus f:list){
              System.out.println("File found " +f.getPath());
          }
      }
      
      conf.setMapDebugScript("./fail-script");
      conf.setReduceDebugScript("./fail-script");
      //this create a simlink from the job directory to cache directory of the mapper node
      DistributedCache.createSymlink(conf);

      URI fsUri = fs.getUri();

      String mapUriStr = fsUri.toString() + HDFS_ROOT + "/fail-script#fail-script"; 
      System.out.println("added "+ mapUriStr + "to distributed cache 1");
      URI mapUri = new URI(mapUriStr);
      //Following copy the map uri to the cache directory of the job node
      DistributedCache.addCacheFile(mapUri, conf);
    }
    
    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf();
        setupFailedTaskScript(conf); 
        Job job = new Job(conf, "word count");
        
        job.setJarByClass(FaultyWordCount.class);
        job.setMapperClass(FaultyWordCount.TokenizerMapper.class);
        job.setReducerClass(FaultyWordCount.IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);
    }

}
