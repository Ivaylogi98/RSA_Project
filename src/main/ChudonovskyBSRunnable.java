package main;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import java.util.List;

public class ChudonovskyBSRunnable implements Runnable {

    Range range
    private Apfloat sum;
    private final int threadIndex;
    private final boolean quietMode;

    public ChudonovskyBSRunnable(Range range, List<Pair<Apfloat, Apfloat>> sum, int index, boolean quietMode) {
        this.range = range;
        this.termSums = termSums;
        this.threadIndex = index;
        this.quietMode = quietMode;
    }

    public void run() {

        if (!quietMode) System.out.println("Thread-" + threadIndex + " started.");
        long start = System.currentTimeMillis();



        long finish = System.currentTimeMillis();
        if (!quietMode) {
            System.out.println("Thread-" + threadIndex + " stopped.");
            System.out.println("Thread-" + threadIndex + " execution time was(millis): " + (finish - start));
        }
    }
}