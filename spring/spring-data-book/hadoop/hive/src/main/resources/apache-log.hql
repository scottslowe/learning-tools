-- set local mode
-- see https://cwiki.apache.org/Hive/gettingstarted.html#GettingStarted-Hive%252CMapReduceandLocalMode

-- SET mapred.job.tracker=local;
-- SET mapred.local.dir=/tmp/hive;

ADD JAR /home/mpollack/software/hive-0.8.1-bin/lib/hive-contrib-0.8.1.jar;

DROP TABLE IF EXISTS apachelog;
CREATE TABLE apachelog(remoteHost STRING, remoteLogname STRING, user STRING, time STRING, method STRING, uri STRING, proto STRING, status STRING, bytes STRING, referer STRING, userAgent STRING) ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe' WITH SERDEPROPERTIES ("input.regex" = "^([^ ]*) +([^ ]*) +([^ ]*) +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) ([^ ]*)\\\" ([^ ]*) ([^ ]*) (?:\\\"-\\\")*\\\"(.*)\\\" (.*)$", "output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s") STORED AS TEXTFILE;

LOAD DATA LOCAL INPATH "./data/apache.log" INTO TABLE apachelog;

-- basic filtering
-- SELECT a.uri FROM apachelog a WHERE a.method='GET' AND a.status='200';

-- determine popular URLs (for caching purposes)

INSERT OVERWRITE LOCAL DIRECTORY 'hive_uri_hits' SELECT a.uri, "\t", COUNT(*) FROM apachelog a GROUP BY a.uri ORDER BY uri;

INSERT OVERWRITE LOCAL DIRECTORY 'hive_uris_to_cache_report' SELECT t1.uri, t1.total FROM (SELECT uri, count(1) AS total from apachelog GROUP by uri) t1 where t1.total>5;

CREATE TABLE IF NOT EXISTS histogram (hits STRING, uris INT);
INSERT OVERWRITE TABLE histogram SELECT hits, COUNT(*) FROM (SELECT COUNT(a.uri) AS hits FROM apachelog a GROUP BY a.uri) t3 GROUP BY hits;
INSERT OVERWRITE LOCAL DIRECTORY 'hive_historgram' SELECT * from histogram;

INSERT OVERWRITE LOCAL DIRECTORY 'hive_low_hits' SELECT h.uris FROM histogram h WHERE h.hits == 1;
INSERT OVERWRITE LOCAL DIRECTORY 'hive_high_hits' SELECT SUM(h.hits * h.uris) FROM histogram h WHERE h.hits > 1;
INSERT OVERWRITE LOCAL DIRECTORY 'hive_uris_to_cache' SELECT SUM(h.uris) FROM histogram h WHERE h.hits > 1;
