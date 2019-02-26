package chapter8.adwords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

/**
 * This class generate a random bid data set.
 * @author Srinath Perera <hemapani@apache.org>
 *
 */
public class AdwordsBidGenerator {
    public static void main(String[] args) throws Exception{
        if(args.length != 1){
            System.out.println("Usage: AdwordsBidGenerator <path-to-keywordsfile>");
            System.exit(-1);
        }
        
        Random random = new Random();
        int clientsCount  = 100;
        int keywordCount = 1000;
        
        String[] keywords = new String[keywordCount];
        int keywordIndex = 0;

        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(args[0]), 100 * 1024);

        while ((line = reader.readLine()) != null && keywordIndex < keywordCount) {
            String[] tokens = line.split("\\s");
            keywords[keywordIndex] = tokens[0].replace("keyword:", ""); 
            keywordIndex++; 
        }
        keywordCount = keywordIndex; 
        
        reader.close();
        FileWriter fileWriter = new FileWriter("biddata.data");
        
        for(int i = 0;i< clientsCount;i++){
            StringBuffer buffer = new StringBuffer();
            int budget = (int)Math.max(0,1000*Math.abs(random.nextGaussian()));
            buffer.append("client").append(i).append("\t");
            buffer.append("budget").append("=").append(budget).append(",");
            int bidCount = random.nextInt(9)+1;
            for(int j = 0;j< bidCount; j++){
                int bidItemIndex = (int)Math.abs(keywordCount/2 + keywordCount*random.nextGaussian()/3); 
                bidItemIndex = Math.min(keywordCount-1, Math.max(0, bidItemIndex));
                int bidvalue = (int)Math.abs(5 + 5*random.nextGaussian());
                buffer.append("bid").append("=").append(keywords[bidItemIndex]).append("|").append(bidvalue).append(",");
            }
            buffer.append("\n");
            fileWriter.write(buffer.toString());
        }
        fileWriter.close();
        System.out.println("Bids Generated at biddata.data");
    }

}
