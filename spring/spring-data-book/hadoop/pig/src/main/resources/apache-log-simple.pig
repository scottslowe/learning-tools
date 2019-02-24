REGISTER $piggybanklib;
DEFINE LogLoader org.apache.pig.piggybank.storage.apachelog.CombinedLogLoader();
logs = LOAD '$inputPath' USING LogLoader as (remoteHost, remoteLogname, user, time, method, uri, proto, status, bytes, referer, userAgent);
-- logs = FILTER logs BY method == 'GET' AND status == 200;
-- logs = FOREACH logs GENERATE uri;
-- basic dump of URI matching the criteria above
-- DUMP logs;

-- determine popular URLs (for caching purposes for example)
byUri = ORDER logs BY uri;
byUri = GROUP logs BY uri;

uriHits = FOREACH byUri GENERATE group AS uri, COUNT(logs.uri) AS numHits;
-- or store into into a file
STORE uriHits into '$outputPath/pig_uri_hits';
