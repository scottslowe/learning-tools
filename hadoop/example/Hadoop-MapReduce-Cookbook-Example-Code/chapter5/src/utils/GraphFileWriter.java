package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphFileWriter {
    public static final Pattern numericalPattern = Pattern.compile("[0-9.]+"); 

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        if(args.length != 2 && args.length != 3){
            System.out.println("Usage: name file [maxcount]" );
        }else{
            String name = args[0];
            String file = args[1];
            
            int maxCount;
            if(args.length == 2){
                maxCount = -1;
            }else{
                maxCount = Integer.parseInt(args[2]);
            }
            
            String title = null;
             
            List<String[]> dataList = new ArrayList<String[]>(); 
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
          
          String line = null;
    
          
          //skip first line
          reader.readLine();
          int count = 0; 
          
          while ((line = reader.readLine()) != null) {
              dataList.add(line.split("\\s"));
          }
          
          String[][] data = dataList.toArray(new String[0][]);
            StringBuffer buf = new StringBuffer();
            
            buf.append("<script lang=\"javascript\" type=\"text/javascript\">");
            
            
            buf.append("var seriesData = { type: 'line', title: 'line',");
            buf.append("data:[ ");
            boolean isfirst = true; 
            int index =0;
            for(String[] dateItem: data){
                index++;
                if(count > 0 && index < (data.length - count)){
                    continue;
                }
                if(isfirst){
                    isfirst = false;
                }else{
                    buf.append(",");
                }
                Matcher matcher = numericalPattern.matcher(dateItem[0]);
                if(matcher.matches()){
                    buf.append("[").append(dateItem[0]).append(",").append(dateItem[1]).append("]");    
                }else{
                    buf.append("[\"").append(dateItem[0]).append("\",").append(dateItem[1]).append("]");
                }
            }
            buf.append("] }\n");
            
            
            
            buf.append("$(document).ready(function () { $('#jqChart1').jqChart({\n");
            buf.append("title: '"+title+ "',  legend: { title: 'Legend' }, border: { strokeStyle: '#6ba851' }, series: [seriesData]});\n");
            buf.append("});\n");
            buf.append("</script>\n");
            
            System.out.println(buf.toString());
            
            //read the file and replace @@script@@ in the file
            
        }
    }

}
