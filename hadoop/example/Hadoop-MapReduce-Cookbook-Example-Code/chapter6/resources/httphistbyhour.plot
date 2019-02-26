set terminal png
set output "hitsbyHour.png"


set title "Hits by Hour of Day";
set ylabel "Number of Hits";
set xlabel "Hour";
set key left top
#set log y

plot "3.data" using 1:2 title "2 Node" with linespoints



