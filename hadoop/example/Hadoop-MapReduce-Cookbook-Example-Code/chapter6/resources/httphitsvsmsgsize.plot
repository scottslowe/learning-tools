set terminal png
set output "hitsbymsgSize.png"


set title "Hits by Size of the Message";
set ylabel "Number of Hits";
set xlabel "Size of the Message (X1000) bytes";
set key left top
set log y
set log x

plot "5.data" using 1:2 title "2 Node" with points



