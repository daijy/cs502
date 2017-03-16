package com.example.pig;

import java.io.IOException;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.pig.builtin.PigStorage;
import org.apache.pig.data.Tuple;

public class BootstrapSampleLoader extends PigStorage
{
    PoissonDistribution pd = new PoissonDistribution(1);
    Tuple originalTuple;
    int remaining = 0;
    @Override
    public Tuple getNext() throws IOException {
        if (remaining > 0) {
            remaining --;
            return originalTuple;
        }

        do {
            remaining = pd.sample();
            originalTuple = super.getNext();
        } while (originalTuple!=null && remaining == 0);
        remaining--;
        return originalTuple;
    }
}
