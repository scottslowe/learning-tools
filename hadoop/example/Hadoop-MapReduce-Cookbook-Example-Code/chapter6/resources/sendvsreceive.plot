set terminal png
set output "sendreceive.png"


set title "Replies Sent vs. Replies Received";
set ylabel "Replies Received";
set xlabel "Replies Sent (without self replies)";
set key left top
set pointsize 2
set log x 
set log y

plot "8.data" using 1:2 title "Scatter" with points pointtype 6




