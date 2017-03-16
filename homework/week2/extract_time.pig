a = LOAD 'access_logs' AS (line:chararray);
b = FOREACH a GENERATE flatten(REGEX_EXTRACT_ALL(line, '(.*?) .*?\\[(.*?)\\].*')) as (ip:chararray, dt:chararray);
c = FOREACH b GENERATE ip, ToDate(dt, 'yyyy-MM-dd HH:mm:ss.SSSSSS') as dt;
d = FOREACH c GENERATE ip, GetYear(dt), GetMonth(dt), GetDay(dt), GetHour(dt), GetMinute(dt), GetSecond(dt);
dump d;
