set terminal png
set output "freqdist.png"

set title "Frequnecy Distribution of Hits by Url";
set ylabel "Number of Hits";
set xlabel "Urls (Sorted by hits)";
set key left top
set log y
set log x

plot "2.data" using 2 title "Frequency" with linespoints



