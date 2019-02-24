passwd = LOAD '$inputDir' USING PigStorage(':') AS (user:chararray, passwd:chararray, uid:int, gid:int, userinfo:chararray, home:chararray, shell:chararray);
grp_shell = GROUP passwd BY shell;
counts = FOREACH grp_shell GENERATE group, COUNT(passwd);
STORE counts into '$outputDir';


