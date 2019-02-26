import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/* This sample demostrate use of HDFS Java API
 * This sample is loosely based on the 
 * http://wiki.apache.org/hadoop/HadoopDfsReadWriteExample
*/

public class HDFSJavaAPIDemo {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path(
				"/u/hadoop-1.0.2/conf/core-site.xml"));
		conf.addResource(new Path(
				"/u/hadoop-1.0.2/conf/hdfs-site.xml"));

		FileSystem fileSystem = FileSystem.get(conf);
		System.out.println(fileSystem.getUri());

		Path file = new Path("demo.txt");
		if (fileSystem.exists(file)) {
			System.out.println("File exists.");
		} else {
			// Writing to file
			FSDataOutputStream outStream = fileSystem.create(file);
			outStream.writeUTF("Welcome to HDFS Java API!!!");
			outStream.close();
		}

		// Reading from file
		FSDataInputStream inStream = fileSystem.open(file);
		String data = inStream.readUTF();
		System.out.println(data);
		inStream.close();

		// deleting the file. Non-recursively.
		// fileSystem.delete(file, false);

		fileSystem.close();
	}
}
