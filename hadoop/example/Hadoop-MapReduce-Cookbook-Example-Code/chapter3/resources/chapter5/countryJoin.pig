A = load 'hdi-data.csv' using PigStorage(',')  AS (id:int, country:chararray, hdi:float, lifeex:int, mysch:int, eysch:int, gni:int);
B = FILTER A BY gni > 2000;
C = ORDER B BY gni;

D = load 'export-data.csv' using PigStorage(',')  AS (country:chararray, expct:float); 

E = JOIN C BY country, D by country;

dump E; 
