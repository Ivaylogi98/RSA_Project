package main;

import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

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
        Apfloat pi = calculatePi(precision, numberOfThreads, quietMode);

        try {
            FileWriter myWriter = new FileWriter(outputFile);
            myWriter.write(String.valueOf(pi));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        if(!quietMode) System.out.println("Threads used in current run: " + numberOfThreads);

        System.out.println("Total execution time for current run was: " + (finish - start));
    }

    public static Apfloat calculatePi(final long precision, int numberOfThreads, boolean quietMode) {
        List<Range> ranges = Chudonovsky.calculateTermRanges2(numberOfThreads, precision);
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

    public static List<Range> calculateTermRanges(long numberOfRanges, long precision) {

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

}