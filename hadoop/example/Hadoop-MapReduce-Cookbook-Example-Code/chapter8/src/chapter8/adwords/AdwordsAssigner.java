package chapter8.adwords;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdwordsAssigner {
    public static class BidderData{
        String clientID; 
        double intialBudget;
        double currentBudget; 
        double bid; 
        double clickRate;
    }
    
    
    private static Map<String, BidderData[]>  map = new HashMap<String, AdwordsAssigner.BidderData[]>();
    
    public static String findBest(String keyword){
        BidderData[] bidderData = map.get(keyword);
        double bestMeasure = 0; 
        BidderData bestBidder = null;
        
        for(BidderData bidder: bidderData){
            double val = Math.exp(-1*bidder.currentBudget/bidder.intialBudget); 
            double measure = bidder.bid * bidder.clickRate * (1 - val);
            if(bestMeasure == 0){
                bestMeasure = measure; 
                bestBidder = bidder; 
            }
            if(measure >= bestMeasure){
                bestMeasure = measure; 
                bestBidder = bidder; 
            }
        }
        
        if(bestBidder != null){
            bestBidder.currentBudget = bestBidder.currentBudget - bestBidder.bid; 
        }else{
            System.out.println();
        }
        
        return bestBidder.clientID; 
    }
    
    

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        
        BufferedReader reader = new BufferedReader(new FileReader("target/output16/part-r-00000"), 100 * 1024);
        
        String line; 
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            String keyword = tokens[0];
            String[] bids = tokens[1].split("\\|");
            
            List<BidderData> list = new ArrayList<AdwordsAssigner.BidderData>();
            for(String bid: bids){
                if(bid.trim().length() == 0){
                    continue;
                }
                String[] bidData = bid.split(",");
                String clientID = bidData[0];
                double budget = Double.parseDouble(bidData[1]);
                double normalizedBid = Double.parseDouble(bidData[2]);
                
                
                BidderData bidBean = new BidderData(); 
                bidBean.clientID = clientID; 
                bidBean.currentBudget = budget; 
                bidBean.intialBudget = budget; 
                bidBean.bid = normalizedBid; 
                bidBean.clickRate = Double.parseDouble(bidData[3]);
                list.add(bidBean); 
            }
            map.put(keyword, list.toArray(new BidderData[0]));
            
            List<String> keywordList = new ArrayList<String>(map.keySet());
            
            Random random = new  Random();
            for(int i = 0;i<1000;i++){
                System.out.println(findBest(keywordList.get(random.nextInt(keywordList.size()))));
            }
        }

    }

}
