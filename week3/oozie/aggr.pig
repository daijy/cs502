A = load 'student' using org.apache.hive.hcatalog.pig.HCatLoader();
B = group A by name;
C = foreach B generate group as name, AVG(A.gpa) as gpa;
store C into '$OUTPUT' USING PigStorage();
