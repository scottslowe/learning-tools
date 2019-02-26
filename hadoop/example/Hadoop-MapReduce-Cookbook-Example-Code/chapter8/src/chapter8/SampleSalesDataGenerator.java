package chapter8;

import java.io.FileWriter;
import java.util.Random;

import chapter8.AmazonCustomer.ItemData;

public class SampleSalesDataGenerator {
public static void main(String[] args) throws Exception{
    FileWriter w = new FileWriter("salessata.data");
    Random random = new Random();
    for(int i =0;i<100;i++){
        AmazonCustomer customer = new AmazonCustomer();
        customer.customerID = String.valueOf(i);
        int itemcount = random.nextInt(3); 
        for(int j=0;j<itemcount;j++){
            ItemData itemData = customer.new ItemData();
            itemData.itemID = String.valueOf(random.nextInt(10));
            //itemData.rating = random.nextInt(10);
            itemData.rating = 9;
            customer.itemsBrought.add(itemData);
        }
        
        String custAsStr = customer.toString(); 
        
        new AmazonCustomer(custAsStr).checkEqual(customer);
        
        w.write(custAsStr.toString());
        w.write("\n");
    }
    w.close();

}
}
