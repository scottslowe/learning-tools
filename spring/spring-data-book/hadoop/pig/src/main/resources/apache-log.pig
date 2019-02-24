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
STORE uriHits into 'pig_uri_hits';

byCount = GROUP uriHits BY numHits;
histogram = FOREACH byCount GENERATE group AS numHits, COUNT(uriHits) AS numUris;

-- isolate singular hits
lowHits = FILTER histogram BY numHits == 1;
lowHits = FOREACH lowHits GENERATE numUris AS num;
STORE lowHits INTO 'pig_low_hits';

highHits = FILTER histogram BY numHits > 1;
highHitsNumber = FOREACH highHits GENERATE (numHits * numUris) AS num;
highHitsNumber = GROUP highHitsNumber ALL;
highHitsNumber = FOREACH highHitsNumber GENERATE SUM(highHitsNumber.num);
STORE highHitsNumber INTO 'pig_high_hits';
-- URIs that benefit from caching
uriToCache = GROUP highHits ALL;
uriToCache = FOREACH uriToCache GENERATE SUM(highHits.numUris);

STORE uriToCache INTO 'pig_uris_to_cache';
