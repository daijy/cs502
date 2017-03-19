register datafu-pig-incubating-1.3.0-SNAPSHOT.jar
DEFINE Sessionize datafu.pig.sessions.Sessionize('30m');

rmf ooo

a = LOAD 'sample.txt' AS (ip:chararray, dt:chararray, category:chararray);
b = FILTER a BY ip IS NOT NULL;
c = FOREACH b GENERATE ToDate(dt, 'dd/MMM/yyyy:HH:mm:ss') as dt, ip, category;
d = FOREACH c GENERATE ToMilliSeconds(dt) as ts, dt, ip, category;
e = GROUP d BY (ip, category); 
f = FOREACH e {
    ordered = ORDER d BY ts;
    GENERATE FLATTEN(Sessionize(ordered)) AS (ts,dt,ip,category,sessionId);
}
g = group f by (sessionId, ip, category);
h = foreach g generate group.ip, group.category, MIN(f.dt) as start_time, COUNT(f) as session_count, ((MAX(f.ts) - MIN(f.ts))/ 1000.0/ 60.0) as session_length;
i = foreach h generate ip, category, ToString(start_time, 'dd/MMM/yyyy:HH:mm:ss') as start_time, session_count, session_length;
store i into 'ooo';
