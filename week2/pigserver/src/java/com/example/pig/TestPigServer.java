package com.example.pig;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.PropertyConfigurator;
import org.apache.pig.PigServer;
import org.apache.pig.data.Tuple;

public class TestPigServer {
    static public void main(String[] args) throws IOException {

    	PropertyConfigurator.configure("log4j.properties");

        PigServer pigServer = new PigServer("local");

        pigServer.registerQuery("a = load 'studenttab10k' using PigStorage() as (name:chararray, age:int, gpa:double);");

        pigServer.registerQuery("a1 = filter a by age > 18;");

        pigServer.registerQuery("a2 = foreach a1 generate name, ROUND(gpa) as gpa;");

        pigServer.registerQuery("b = load 'votertab10k' using PigStorage() as (name:chararray, age:int, registration, contributions:double);");

        pigServer.registerQuery("c = join a2 by name, b by name;");

        pigServer.registerQuery("d = group c by registration;");

        pigServer.registerQuery("e = foreach d generate group, AVG(c.gpa) as gpa;");

        pigServer.registerQuery("f = order e by gpa desc;");

        Iterator<Tuple> iter = pigServer.openIterator("f");

        while (iter.hasNext()) {
            System.out.println(iter.next());
        }

    }
}
