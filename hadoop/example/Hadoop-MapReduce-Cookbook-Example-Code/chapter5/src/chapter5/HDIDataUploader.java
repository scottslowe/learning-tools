package chapter5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import utils.CSVLineParser;

/**
 * This class read the data file from resources/chapter5/hdi-data.csv and upload the data to HBase running in the
 * local machine. 
 * @author srinath
 *
 */
public class HDIDataUploader {

    private static final String TABLE_NAME = "HDI";

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        Configuration conf = HBaseConfiguration.create();
        //change here if you want to change the HBase installation. 
        conf.set("hbase.master", "localhost:60000");

        Configuration config = HBaseConfiguration.create();
        HTable table = new HTable(config, TABLE_NAME);

        //Change here if you want to change the input file. 
        BufferedReader reader = new BufferedReader(new FileReader("resources/chapter5/hdi-data.csv"));

        try {
            String line = null;
            // skip first line
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    // line = line.replaceAll("\"(.*),(.*)\"", "$1 $2");

                    String[] tokens = CSVLineParser.tokenizeCSV(line).toArray(new String[0]);
                    String country = tokens[1];
                    double lifeExpectacny = Double.parseDouble(tokens[3].replaceAll(",", ""));
                    double meanYearsOfSchooling = Double.parseDouble(tokens[4].replaceAll(",", ""));
                    double gnip = Double.parseDouble(tokens[6].replaceAll(",", ""));

                    Put put = new Put(Bytes.toBytes(country));
                    put.add("ByCountry".getBytes(), Bytes.toBytes("lifeExpectacny"), Bytes.toBytes(lifeExpectacny));
                    put.add("ByCountry".getBytes(), Bytes.toBytes("meanYearsOfSchooling"),
                            Bytes.toBytes(meanYearsOfSchooling));
                    put.add("ByCountry".getBytes(), Bytes.toBytes("gnip"), Bytes.toBytes(gnip));
                    table.put(put);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error processing " + line + " caused by " + e.getMessage());
                }
            }
        } catch (IOException e) {
            try {
                reader.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        //Following print back the results
        Scan s = new Scan();
        s.addFamily(Bytes.toBytes("ByCountry"));
        ResultScanner results = table.getScanner(s);

        try {
            for (Result result : results) {
                KeyValue[] keyValuePairs = result.raw();
                System.out.println(new String(result.getRow()));
                for (KeyValue keyValue : keyValuePairs) {
                    System.out.println(new String(keyValue.getFamily()) + " " + new String(keyValue.getQualifier())
                            + "=" + Bytes.toDouble(keyValue.getValue()));
                }
            }
        } finally {
            results.close();
        }
    }

}
