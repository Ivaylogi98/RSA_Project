package main;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Chudonovsky {

    static private boolean quietMode = false;
    static private int numberOfThreads = 1;
    static private int precision = 100;
    static private String outputFile = "pi.txt";

    public static void main (String[] args) {

        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "-p":
                    try {
                        precision = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        precision = 10;
                    }
                    break;
                case "-t":
                case "--tasks":
                    try {
                        numberOfThreads = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        numberOfThreads = 1;
                    }
                    break;
                case "-o":
                    outputFile = args[i + 1];
                    try {
                        File pi = new File(outputFile);
                        if (pi.createNewFile()) {
                            System.out.println("File created: " + pi.getName());
                        } else {
                            System.out.println("File already exists.");
                        }
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                    break;
                case "-q":
                    quietMode = true;
                    break;
            }
        }
        long start = System.currentTimeMillis();
        Apfloat pi = calculatePiBS(precision, numberOfThreads, quietMode);
        try {
            FileWriter myWriter = new FileWriter(outputFile);
            myWriter.write(String.valueOf(pi));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        if (!quietMode) System.out.println("Threads used in current run: " + numberOfThreads);

        System.out.println("Total execution time for current run was: " + (finish - start));

    }

    public static Apfloat calculatePi(final long precision, int numberOfThreads, boolean quietMode) {
        List<Range> ranges = Chudonovsky.calculateTermRanges(numberOfThreads, precision);
        List<Pair<Apfloat, Apfloat>> termSums = new ArrayList<>(ranges.size());
        Thread[] tr = new Thread[numberOfThreads];

        for (int index = 0; index < numberOfThreads; index++) {
            ChudonovskyRunnable r = new ChudonovskyRunnable(ranges.get(index), precision, termSums, index, quietMode);
            Thread t = new Thread(r);
            tr[index] = t;
            t.start();
        }
        for(int i = 0; i < numberOfThreads; i++) {

            try {
                tr[i].join();
            } catch (InterruptedException e) {
                System.out.println("Something went wrong.");
            }

        }

        return Chudonovsky.merge(termSums, precision);
    }
    public static Apfloat calculatePi(long precision) {
        // need one extra place for the 3, and one extra place for some rounding issues
        precision = precision + 2;
        Apfloat negativeOne = new Apfloat(-1L);

        Apfloat two = new Apfloat(2L);
        Apfloat five = new Apfloat(5L);
        Apfloat six = new Apfloat(6L);
        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10L));

        // find the first term in the series
        Apfloat k = new Apfloat(0L);
        Apfloat a_k = new Apfloat(1L, precision);

        Apfloat a_sum = new Apfloat(1L);
        Apfloat b_sum = new Apfloat(0L);
        k = k.add(Apfloat.ONE);

        long numberOfLoopsToRun = new Apfloat(precision, precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE).longValue();

        while (k.longValue() < numberOfLoopsToRun) {
            a_k = a_k.multiply(negativeOne.multiply((six.multiply(k).subtract(five)).multiply(two.multiply(k).subtract(Apfloat.ONE)).multiply(six.multiply(k).subtract(Apfloat.ONE))));
            a_k = a_k.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24));
            a_sum = a_sum.add(a_k);
            b_sum = b_sum.add(k.multiply(a_k));
            k = k.add(Apfloat.ONE);
        }

        Apfloat total = new Apfloat(13591409L).multiply(a_sum).add(new Apfloat(545140134L).multiply(b_sum));

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision));
        Apfloat pi = (new Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total)).precision(precision - 1);

        return pi;
    }

    public static Apfloat calculatePiBS(long precision, int numberOfThreads, boolean quietMode) {
        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10L));
        long N = (new Apfloat(precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE)).longValue();

        List<Range> ranges = Chudonovsky.calculateTermRanges(numberOfThreads, precision);
        Thread[] tr = new Thread[numberOfThreads];
        for (int index = 0; index < numberOfThreads; index++) {
            ChudonovskyRunnable r = new ChudonovskyBSRunnable(ranges.get(index),sum , index, quietMode);
            Thread t = new Thread(r);
            tr[index] = t;
            t.start();
        }
        for(int i = 0; i < numberOfThreads; i++) {

            try {
                tr[i].join();
            } catch (InterruptedException e) {
                System.out.println("Something went wrong.");
            }

        }


        TupleApfloat PQT = BS(0, N);

        Apfloat one = ApfloatMath.pow(new Apfloat(10), DIGITS_PER_TERM);

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision + 1));
        return (PQT.getQ()).multiply(new Apfloat(426880L)).multiply(sqrtTenThousandAndFive).divide(PQT.getT());
    }

    private static TupleApfloat BS(long a, long b) {
        Apfloat two = new Apfloat(2L);
        Apfloat five = new Apfloat(5L);
        Apfloat six = new Apfloat(6L);
        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat Pab, Qab, Tab;
        if((b - a) == 1)
        {
            if(a == 0) {
                Pab = new Apfloat(1L);
                Qab = new Apfloat(1L);
            }
            else {
                Pab = new Apfloat(6 * a - 5).multiply(new Apfloat(2 * a - 1)).multiply(new Apfloat(6 * a - 1));
                Qab = new Apfloat(a * a * a).multiply(C3_OVER_24);
            }
            Tab = Pab.multiply(new Apfloat(13591409L).add(new Apfloat(545140134L).multiply(new Apfloat(a))));
            if((a&1) == 1){
                Tab = Tab.multiply(new Apfloat(-1L));
            }
        }
        else{
            long m = (a+b)/2;
            TupleApfloat am = BS(a, m);
            TupleApfloat mb = BS(m, b);
            Pab = am.getP().multiply(mb.getP());
            Qab = am.getQ().multiply(mb.getQ());
            Tab = am.getT().multiply(mb.getT());
        }
        return new TupleApfloat(Pab, Qab, Tab);
    }

    public static List<Range> calculateTermRanges(long numberOfRanges, long precision) {

        long start = System.currentTimeMillis();
        if (numberOfRanges <= 0) {
            throw new IllegalArgumentException("Number of ranges should be positive.");
        }

        List<Range> ranges = new ArrayList<>();

        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10L));

        long numberOfTerms = (new Apfloat(precision, precision)).divide(DIGITS_PER_TERM).ceil().longValue();

        double rangeSize = (double) numberOfTerms / Long.valueOf(numberOfRanges).doubleValue();

        for (double i = 0.0; i < numberOfTerms; i += rangeSize) {
            double f = (i + rangeSize);

            long il = (long) i;
            long fl = (long) f;

            il = Math.min(il, numberOfTerms);
            fl = Math.min(fl, numberOfTerms);
            ranges.add(new Range(il, fl));
        }
        System.out.println( "Digits_per_term:" + (System.currentTimeMillis() - start));
        return ranges;
    }

    public static List<Range> calculateTermRanges2(long numberOfRanges, long iterations) {
        if (numberOfRanges <= 0) {
            throw new IllegalArgumentException("Number of ranges should be positive.");
        }

        List<Range> ranges = new ArrayList<>();
        long rangeSize = iterations / numberOfRanges;
        for (int i = 0; i < iterations; i += rangeSize)
        {
            long lower = i;
            long upper = i + rangeSize;
            lower = Math.min(lower, iterations);
            upper = Math.min(upper, iterations);
            ranges.add(new Range(lower, upper));
        }
        return ranges;
    }

    public static Apfloat merge(List<Pair<Apfloat, Apfloat>> termSums, long precision) {

        Apfloat a_sum = new Apfloat(0L);
        Apfloat b_sum = new Apfloat(0L);

        for (Pair<Apfloat, Apfloat> termSum : termSums) {
            a_sum = a_sum.add(termSum.getLeft());
            b_sum = b_sum.add(termSum.getRight());
        }

        precision++;
        Apfloat total = new Apfloat(13591409L).multiply(a_sum).add(new Apfloat(545140134L).multiply(b_sum));

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision + 1));

        return (new Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total)).precision(precision);
    }
    public static Apfloat mergeBS(List<Pair<Apfloat, Apfloat>> termSums, long precision) {

        Apfloat a_sum = new Apfloat(0L);
        Apfloat b_sum = new Apfloat(0L);

        for (Pair<Apfloat, Apfloat> termSum : termSums) {
            a_sum = a_sum.add(termSum.getLeft());
            b_sum = b_sum.add(termSum.getRight());
        }

        precision++;
        Apfloat total = new Apfloat(13591409L).multiply(a_sum).add(new Apfloat(545140134L).multiply(b_sum));

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision + 1));

        return (new Apfloat(426880L).multiply(sqrtTenThousandAndFive).divide(total)).precision(precision);
    }
}