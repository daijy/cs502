/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.net.URI;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCount {

  // Feature flags
  public static final boolean SEQUENCE = false;
  public static final boolean COMPRESS = true;
  public static final boolean PARTITION = false;
  public static final boolean DICTIONARY = false;

  
  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    private Map<String, String> dict = new HashMap<String, String>();
    @Override
    public void setup(Context context) throws IOException, InterruptedException {
    	FileInputStream fis = new FileInputStream("dict.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
//      BufferedReader reader = new BufferedReader(new FileReader("dict.txt"));
    	String line;
        while ((line=reader.readLine())!=null) {
            String[] items = line.split(":");
            dict.put(items[0], items[1]);
        }
        reader.close();
    }    
    
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
    	  if (DICTIONARY) {
        	  String w = itr.nextToken();
        	  if (dict.containsKey(w)) {
        		  w = dict.get(w);
        	  }
        	  word.set(w);    		  
    	  } else {
    		  word.set(itr.nextToken());
    	  }
    	  context.write(word, one);
      }
    }
  }
  
  // Custom partitioner - word starts with A and B always go to same reduce 
  public static class customPartitioner extends Partitioner<Text, Writable> {
    @Override
    public int getPartition(Text key, Writable value, int numReduceTasks) {
      String[] str = key.toString().split("\t");
      if (str[0].startsWith("A") || str[0].startsWith("B")) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: wordcount <in> [<in>...] <out>");
      System.exit(2);
    }
    
    // Initiation
    conf.setInt("multiplier", 2);
    conf.setInt("mapreduce.input.fileinputformat.split.maxsize", 10);
    conf.setInt("mreduce.job.reduces", 2);
    // Compress output file
    if (COMPRESS) {
    	conf.setBoolean("mapreduce.output.fileoutputformat.compress", true);
    	conf.set("mapreduce.output.fileoutputformat.compress.codec", 
    			"org.apache.hadoop.io.compress.GzipCodec");
    }
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    // Chinese Dictionary
     if (DICTIONARY) {    
	     job.setCacheArchives(new URI[] {new File("dict.txt").toURI()});  
     }
    // Custom partitioner 
    if (PARTITION){
    	job.setNumReduceTasks(2);
    	job.setPartitionerClass(customPartitioner.class);  	
    }
    // Store into sequence file
    if (SEQUENCE) {
    	job.setOutputFormatClass(SequenceFileOutputFormat.class);
    }

    
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}