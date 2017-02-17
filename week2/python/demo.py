#!/usr/bin/python 

# explicitly import Pig class 
from org.apache.pig.scripting import Pig 

# COMPILE: compile method returns a Pig object that represents the pipeline
P = Pig.compile("""a = load '$input' using PigStorage() as (name:chararray, age:int, gpa:double);
    a1 = filter a by age > 18;
    a2 = foreach a1 generate name, ROUND(gpa) as gpa;
    b = load 'votertab10k' using PigStorage() as (name:chararray, age:int, registration:chararray, contributions:double);
    c = join a2 by name, b by name;
    d = group c by registration;
    e = foreach d generate group, AVG(c.gpa) as gpa;
    f = order e by gpa desc;
    store f into '$output';
""")

results = P.bind({'input':'studenttab10k', 'output':'output'}).runSingle()

if results.isSuccessful() == "FAILED":
    raise "Pig job failed"
iter = results.result("f").iterator()
while iter.hasNext():
    tuple = iter.next()
    print tuple
