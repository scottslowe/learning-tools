A = load '$INPUT' using PigStorage(',')  AS (id:int, country:chararray, hdi:float, lifeex:int, mysch:int, eysch:int, gni:int);
B = FILTER A BY gni > 2000;
C = ORDER B BY gni;
STORE C into '$OUTPUT';
