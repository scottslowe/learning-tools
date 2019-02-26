package chapter6;

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
 * Parse each mail line by line from MBox stream 
 * @author Srinath Perera (hemapani@apache.org)
 */

public class MBoxFileReader extends RecordReader<Text, Text> {
    private static Pattern pattern1 = Pattern.compile("From .*tomcat.apache.org@tomcat.apache.org.*");
    private BufferedReader reader;
    private int count = 0;
    private Text key;
    private Text value;
    private StringBuffer email = new StringBuffer();
    String line = null;

    public MBoxFileReader() {
    }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext attempt) throws IOException, InterruptedException {
        Path path = ((FileSplit) inputSplit).getPath();

        FileSystem fs = FileSystem.get(attempt.getConfiguration());
        FSDataInputStream fsStream = fs.open(path);
        reader = new BufferedReader(new InputStreamReader(fsStream));

        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern1.matcher(line);
            if (matcher.matches()) {
                email.append(line).append("\n");
                break;
            }
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (email == null) {
            return false;
        }
        count++;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern1.matcher(line);
            if (!matcher.matches()) {
                email.append(line).append("\n");
            } else {
                parseEmail(email.toString());
                email = new StringBuffer();
                email.append(line).append("\n");
                return true;
            }
        }
        parseEmail(email.toString());
        email = null;
        return true;
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

    public void parseEmail(String email) {
        String[] tokens = email.split("\n");
        String from = null;
        String subject = null;
        String date = null;

        for (String token : tokens) {
            if (token.contains(":")) {
                if (token.startsWith("From:")) {
                    from = token.substring(5).replaceAll("<.*>|\\\"|,|=[0-9]*", "").replaceAll("\\[.*?\\]", "")
                            .replaceAll("\\s", "_").trim();
                } else if (token.startsWith("Subject:")) {
                    subject = token.substring(8).trim();
                } else if (token.startsWith("Date:")) {
                    date = token.substring(5).trim();
                }
            }
        }

        key = new Text(String.valueOf((from + subject + date).hashCode()));
        value = new Text(from + "#" + subject + "#" + date);
    }
}
