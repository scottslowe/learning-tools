package chapter8;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AmazonCustomer implements ClusterablePoint{
    public String customerID; 
    public String clusterID; 
    
    public Set<ItemData> itemsBrought = new TreeSet<ItemData>();; 
    public AmazonCustomer(){
        
    }
    
    public AmazonCustomer(String customerLine){
        String[] tokens = customerLine.toString().split(",");
        for(String token: tokens){
            int index = token.indexOf("=");
            if(index > 0){
                String key = token.substring(0,index).trim(); 
                String value = token.substring(index+1).trim();
                
                if(key.equals("customerID")){
                    customerID = value;  
                }else if(key.equals("clusterID")){
                    clusterID = value;
                }else if(key.equals("review")){
                    itemsBrought.add(new ItemData(value));
                }else{
                    System.out.println("Unknown token 1"+ token  +",key='"+ key + "' from the line '"+customerLine + "'" ); 
                }
            }
        }
    }

    
    public static List<AmazonCustomer> parseAItemLine(String itemLine){
        
        List<AmazonCustomer> list = new ArrayList<AmazonCustomer>();
        String[] tokens = itemLine.toString().split("#");
        
        
        String itemID = null; 
        String title = null; 
        String salesrank = null;
        String group = null;
        List<String> similarItems = new ArrayList<String>(); 
        for(String token: tokens){
            String[] keyValue = token.trim().split("=");
            if(keyValue.length < 2){
                continue;
            }
            if(keyValue[0].equals("ASIN")){
                itemID = keyValue[1]; 
            }else if(keyValue[0].equals("title")){
                title = keyValue[1];
            }else if(keyValue[0].equals("group")){
                group = keyValue[1];
            }else if(keyValue[0].equals("salesrank")){
                salesrank = keyValue[1];
            }else if(keyValue[0].equals("similar")){
                String[] items = keyValue[1].split("\\|");
                for(String item:items){
                    similarItems.add(item);     
                }
            }else if(keyValue[0].equals("review")){
                AmazonCustomer amazonCustomer = new AmazonCustomer();
                String[] items = keyValue[1].split("\\|");
                String customerID = items[0];
                String rating = items[1];
                ItemData itemData = amazonCustomer.new ItemData(); 
                itemData.itemID = itemID;
                itemData.rating= Integer.valueOf(rating); 
                itemData.title = title;
                itemData.salesrank = Integer.valueOf(salesrank);
                itemData.group = group;
                itemData.similarItems = similarItems;
                amazonCustomer.customerID = customerID;
                amazonCustomer.itemsBrought.add(itemData); 
                list.add(amazonCustomer);
            }else if(keyValue[0].equals("Id")){

            }else{
                throw new RuntimeException("Unknown token 2"+ token  +","+ keyValue + " from the line '"+itemLine + "'" ); 
            }
        }
        return list; 
    }
    

/**
 * this will return a interger between 1 and 10. If there are common items, and they have rated it similarly, 
 * distance is less, and of there are common items and they have rated it differently, distance is more.
 * If no common items, it will return 5
 */

@Override
public double getDistance(ClusterablePoint other) {
    if(other == null){
        throw new RuntimeException("Both clusters has to be not null to calculate distance");
    }
    /**
     * Jaccard Distance - That is, the Jaccard distance is 1 minus the ratio of the sizes of the 
     * intersection and union of sets x and y

     * Cosine Distance - Given two vectors x and y, the cosine of the angle between them is the dot 
     * product x.y divided by the L2-norms of x and y (i.e., their Euclidean distances from the origin). 
     * Recall that the dot product of vectors [x1, x2, . . . , xn].[y1, y2, . . . , yn] is Pn i=1 xiyi.
     * Edit Distance - This distance makes sense when points are strings. The distance between two strings x = x1x2 á á á xn 
     * and y = y1y2 á á á ym is the smallest number of insertions and deletions of single characters that will convert x to y.
     * Hamming distance between two vectors to be the number of components in which they differ.
     */
    double distance = 5; 
    AmazonCustomer customer1 = (AmazonCustomer)this;
    AmazonCustomer customer2 = (AmazonCustomer)other;
    
    
        for(ItemData item1:customer1.itemsBrought){
            for(ItemData item2:customer2.itemsBrought){
                if(item1.equals(item2)){
                    double ratingDiff = Math.abs(item1.rating - item2.rating);
                    if(ratingDiff < 5){
                        distance = distance - (5 - ratingDiff)/5;
                    }else{
                        distance = distance + (5 - ratingDiff)/5;
                    }
                }
            }
        }
        
        //System.out.println(customer1.itemsBrought + " "+ customer2.itemsBrought + "= "+ distance);
   return Math.min(10,Math.max(0.5, distance));
}



    public class ItemData implements Comparable<ItemData>{
        public String itemID; 
        public String title;
        public int salesrank; 
        public List<String> similarItems = new ArrayList<String>(); 
        public int rating;
        public String group; 
        
        
        public ItemData(){
            
        }
        
        public ItemData(String itemLine){
            String[] tokens = itemLine.toString().split("#");
            for(String token: tokens){
                String[] keyValue = token.trim().split("=");
                if(keyValue.length == 2){
                    if(keyValue[0].equals("ASIN")){
                        itemID = keyValue[1]; 
                    }else if(keyValue[0].equals("group")){
                        group = keyValue[1];
                    }else if(keyValue[0].equals("title")){
                        title = keyValue[1];
                    }else if(keyValue[0].equals("salesrank")){
                        salesrank = Integer.valueOf(keyValue[1]);
                    }else if(keyValue[0].equals("rating")){
                        rating = Integer.valueOf(keyValue[1]);
                    }else if(keyValue[0].equals("similar")){
                        String[] items = keyValue[1].split("\\|");
                        for(String item:items){
                            similarItems.add(item);     
                        }
                    }else{
                        
                    }
                }else{
                    System.out.println("Unexpected token "+ token);
                }
            }
        }

        
        @Override
        public boolean equals(Object other) {
            return this.itemID != null && this.itemID.equals(((ItemData)other).itemID);
        }

        @Override
        public String toString() {
            StringBuffer buf =  new StringBuffer().append("ASIN").append("=").append(itemID).append("#");
            
            if(title != null){
                buf.append("title").append("=").append(title).append("#"); 
            }
            
            if(salesrank > 0){
                buf.append("salesrank").append("=").append(salesrank).append("#");
            }
            
            if(group != null){
                buf.append("group").append("=").append(group).append("#");
            }
                buf.append("rating").append("=").append(rating).append("#");
            if(similarItems.size() > 0){
                buf.append("similar=");
                for(String item:similarItems){
                    buf.append(item).append("|");
                }
            }
            return buf.toString();
        }

        @Override
        public int compareTo(ItemData other) {
            return itemID.compareTo(other.itemID);
        }
        
        
    }
    
    public  class SortableItemData implements Comparable<SortableItemData>{
        ItemData itemData; 
        public SortableItemData(ItemData itemData){
            this.itemData = itemData;
        }
        @Override
        public int compareTo(SortableItemData other) {
            return this.itemData.salesrank - other.itemData.salesrank;
        }
    }

    @Override
    public String toString() {
        StringBuffer buf =  new StringBuffer().append("customerID").append("=").append(customerID).append(",");
        if(clusterID != null){
            buf.append("clusterID").append("=").append(clusterID).append(",");
        }
        for(ItemData itemData:itemsBrought){
            buf.append("review=").append(itemData.toString()).append(",");
        }
        return buf.toString();
    }
    
    @Override
    public String print() {
        return customerID;
    }

    public void checkEqual(AmazonCustomer customer){
        if(!customerID.equals(customer.customerID)){
            throw new RuntimeException("customer ID does not match"); 
        }
        if(itemsBrought.size() != customer.itemsBrought.size()){
            throw new RuntimeException("Brought items does not match");
        }
    }

}
