# Week 7 - Assignment
## Debug Sqoop

## 1. Change directory

~/home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/


<br/>

## 2. Compile:

> ant jar-all

```
...
...
[ivy:resolve] 	org.apache.zookeeper#zookeeper;3.4.2 by [org.apache.zookeeper#zookeeper;3.4.5] in [hadoop200]
	---------------------------------------------------------------------
	|                  |            modules            ||   artifacts   |
	|       conf       | number| search|dwnlded|evicted|| number|dwnlded|
	---------------------------------------------------------------------
	|     hadoop200    |  193  |   33  |   0   |   35  ||  161  |   0   |
	---------------------------------------------------------------------

ivy-retrieve-hadoop:
[ivy:retrieve] :: retrieving :: com.cloudera.sqoop#sqoop [sync]
[ivy:retrieve] 	confs: [hadoop200]
[ivy:retrieve] 	161 artifacts copied, 0 already retrieved (126665kB/205ms)
[ivy:cachepath] DEPRECATED: 'ivy.conf.file' is deprecated, use 'ivy.settings.file' instead
[ivy:cachepath] :: loading settings :: file = /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/ivy/ivysettings.xml

compile:
    [mkdir] Created dir: /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build/classes
    [javac] /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build.xml:526: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
    [javac] Compiling 2 source files to /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build/classes
    [javac] warning: [options] bootstrap class path not set in conjunction with -source 1.6
    [javac] 1 warning
    [javac] /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build.xml:539: warning: 'includeantruntime' was not set, defaulting to build.sysclasspath=last; set to false for repeatable builds
    [javac] Compiling 469 source files to /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build/classes
    [javac] warning: [options] bootstrap class path not set in conjunction with -source 1.6
    [javac] Note: Some input files use or override a deprecated API.
    [javac] Note: Recompile with -Xlint:deprecation for details.
    [javac] Note: Some input files use unchecked or unsafe operations.
    [javac] Note: Recompile with -Xlint:unchecked for details.
    [javac] 1 warning

jar:
      [jar] Building jar: /home/hadoop/sqoop-1.4.6.bin__hadoop-2.0.4-alpha/build/sqoop-1.4.6.jar

BUILD SUCCESSFUL
Total time: 2 minutes 5 seconds
```

<br/>
## 3. Build eclipse project file
> ant eclipse

```
[ivy:resolve] 	org.apache.zookeeper#zookeeper;3.4.2 by [org.apache.zookeeper#zookeeper;3.4.5] in [hadoop200test]
	---------------------------------------------------------------------
	|                  |            modules            ||   artifacts   |
	|       conf       | number| search|dwnlded|evicted|| number|dwnlded|
	---------------------------------------------------------------------
	|   hadoop200test  |  196  |   35  |   2   |   34  ||  165  |   2   |
	---------------------------------------------------------------------

ivy-retrieve-hadoop-test:
[ivy:retrieve] :: retrieving :: com.cloudera.sqoop#sqoop [sync]
[ivy:retrieve] 	confs: [hadoop200test]
[ivy:retrieve] 	165 artifacts copied, 0 already retrieved (129928kB/151ms)

eclipse:
  [eclipse] There were no settings found.
  [eclipse] Writing the project definition in the mode "java".
  [eclipse] Writing the classpath definition.

BUILD SUCCESSFUL
Total time: 2 minutes 15 seconds
```

<br/>

## 4. Import Eclipse Project

1. Set JVM parameter:
> export HADOOP_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"

2. File > Import > General > Existing Project into Workspace

3. Set breakpoints

4. Run sqoop command

5. Configure Remote Java Application


<br/>

## 5. Sqoop

> sqoop import --connect jdbc:mysql://localhost/cs502 --username hadoop --password hadoop --table student --hive-import --hive-overwrite --create-hive-table --hive-table student --hive-home /home/hadoop/apache-hive-1.2.1-bin --m 2 --split-by name


* Bug: 4 Mappers were utilized while only 2 mappers were defined in sqoop command

Log:

```
17/06/10 00:37:05 WARN db.TextSplitter: Generating splits for a textual index column.
17/06/10 00:37:05 WARN db.TextSplitter: If your database sorts in a case-insensitive order, this may result in a partial import or duplicate records.
17/06/10 00:37:05 WARN db.TextSplitter: You are strongly encouraged to choose an integral split column.
```

```
17/06/10 00:40:26 INFO mapreduce.JobSubmitter: number of splits:4
```

Debug:

```
 public List<String> split(int numSplits, String minString,
      String maxString, String commonPrefix) throws SQLException {

    BigDecimal minVal = stringToBigDecimal(minString);
    BigDecimal maxVal = stringToBigDecimal(maxString);

    List<BigDecimal> splitPoints = split(
        new BigDecimal(numSplits), minVal, maxVal);
    List<String> splitStrings = new ArrayList<String>();

    // Convert the BigDecimal splitPoints into their string representations.
    for (BigDecimal bd : splitPoints) {
      System.out.printf("DebugCount = %d, splitPoints size = %d", count++, splitPoints.size());
      BigDecimal bd = splitPoints.get(i); 	
      splitStrings.add(commonPrefix + bigDecimalToString(bd));
    }

    // Make sure that our user-specified boundaries are the first and last
    // entries in the array.
    if (splitStrings.size() == 0
        || !splitStrings.get(0).equals(commonPrefix + minString)) {
      splitStrings.add(0, commonPrefix + minString);
    }
    if (splitStrings.size() == 1
        || !splitStrings.get(splitStrings.size() - 1).equals(
        commonPrefix + maxString)) {
      splitStrings.add(commonPrefix + maxString);
    }

    return splitStrings;
  }
  
```
In the split function, the iterator increases its size during the iteration, so the number of splits is higher than expectation. Instead, iterate with a fixed sized corresponding to command argument. Therefore, the Mapper number goes to 2 after fix. 
 
 
* Apply patch:
> patch -p1 < sqoop.patch

* After Patch:

```
17/06/10 01:16:10 INFO mapreduce.JobSubmitter: number of splits:2
```

## * Cheatsheet

* Set JVM parameter:
> export HADOOP_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"

* After every run:
>unset HADOOP_OPTS
>
>echo $HADOOP_OPTS
>
>hive -e "drop table student;"
>
>hive -e "describe student;"

* On failure runs:
> hadoop fs -ls
> 
> hadoop fs -ls /user/hadoop
> 
> hadoop fs -rmr student
> 
> hadoop fs -rmr /user/hadoop

* Compile/ Clean
> ant -p
> 
> ant clean
> 
> ant jar
>  
> ant eclipse

* Apply patch:
> patch -p1 < sqoop.patch
