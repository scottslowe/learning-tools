-- set local mode
-- see https://cwiki.apache.org/Hive/gettingstarted.html#GettingStarted-Hive%252CMapReduceandLocalMode

-- SET mapred.job.tracker=local;
-- SET mapred.local.dir=/tmp/hive;

ADD JAR ${hiveconf:hiveContribJar};

DROP TABLE apachelog;

CREATE TABLE apachelog(remoteHost STRING, remoteLogname STRING, user STRING, time STRING, method STRING, uri STRING, proto STRING, status STRING, bytes STRING, referer STRING,  userAgent STRING) ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe' WITH SERDEPROPERTIES (  "input.regex" = "^([^ ]*) +([^ ]*) +([^ ]*) +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) ([^ ]*)\\\" ([^ ]*) ([^ ]*) (?:\\\"-\\\")*\\\"(.*)\\\" (.*)$", "output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s" ) STORED AS TEXTFILE;

-- If using variables and executing from HiveTemplate (vs HiveRunner), need to put quotes around the variable name.
LOAD DATA LOCAL INPATH ${hiveconf:localInPath} INTO TABLE apachelog;

-- basic filtering
-- SELECT a.uri FROM apachelog a WHERE a.method='GET' AND a.status='200';

-- determine popular URLs (for caching purposes)

INSERT OVERWRITE LOCAL DIRECTORY 'hive_uri_hits' SELECT a.uri, "\t", COUNT(*) FROM apachelog a GROUP BY a.uri ORDER BY uri;

--DROP TABLE IF EXISTS hive_uri_hits;
--CREATE TABLE hive_uri_hits (uri STRING, count STRING);
--INSERT OVERWRITE TABLE hive_uri_hits SELECT a.uri, COUNT(*) FROM apachelog a GROUP BY a.uri ORDER BY uri;
