package chapter8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/** 
 * @author Srinath Perera (hemapani@apache.org)
 */

public class AmazonDataReader extends RecordReader<Text, Text> {
    private static Pattern pattern1 = Pattern.compile("\\s+([^\\s]+)\\s+cutomer:\\s+([^\\s]+)\\s+rating:\\s+([^\\s]+)\\s+votes:\\s+([^\\s]+)\\s+helpful:\\s+([^\\s]+).*");
    private BufferedReader reader;
    private int count = 0;
    private Text key;
    private Text value;
    private StringBuffer currentLineData = new StringBuffer();
    String line = null;

    public AmazonDataReader() {
    }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext attempt) throws IOException, InterruptedException {
        Path path = ((FileSplit) inputSplit).getPath();

        FileSystem fs = FileSystem.get(attempt.getConfiguration());
        FSDataInputStream fsStream = fs.open(path);
        reader = new BufferedReader(new InputStreamReader(fsStream), 1024*100);
        
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("Id:")){
                break;
            }
        }

    }
    
    
    

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        currentLineData = new StringBuffer();
        count++;
        boolean readingreview = false;
        while ((line = reader.readLine()) != null) {
            if(line.trim().length() == 0){
                value = new Text(currentLineData.toString());
                return true; 
            }else{
                if(readingreview){
                    Matcher matcher = pattern1.matcher(line);
                    if(matcher.matches()){
                        //customer, voting, rates, helpful
                        currentLineData.append("review=").append(matcher.group(2)).append("|")
                            .append(matcher.group(3)).append("|")
                            .append(matcher.group(4)).append("|")
                            .append(matcher.group(5)).append("#");
                    }else{
                        System.out.println("review "+ line + "does not match");
                    }
                }else{
                    int indexOf = line.indexOf(":");
                    if(indexOf > 0){
                        String key = line.substring(0,indexOf).trim();
                        String value = line.substring(indexOf+1).trim();
                        if(value == null || value.length() == 0){
                            continue;
                        }
                        if(value.indexOf("#") > 0){
                            value = value.replaceAll("#", "_");
                        }
                        
                        if(key.equals("ASIN") || key.equals("Id") || key.equals("title") || key.equals("group") || key.equals("salesrank")){
                            if(key.equals("ASIN")){
                                this.key = new Text(value);
                            }
                            currentLineData.append(key).append("=").append(value.replaceAll(",", "")).append("#");
                        }else  if(key.equals("similar")){
                            String[] tokens = value.split("\\s+");
                            //yes we skip the first one
                            if(tokens.length >= 2){
                                currentLineData.append(key).append("=");
                                for(int i=1;i<tokens.length;i++){
                                    currentLineData.append(tokens[i].trim()).append("|");
                                }
                                currentLineData.append("#");
                            }
                        }else  if( key.equals("reviews")){
                            readingreview = true; 
                        }
                    }
                }
            }
        }
        System.out.println("Line processing ends halfway through");
        return false;
    }

    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return count;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
