package main;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class Chudonovsky {

    static private boolean quietMode = false;
    static private int numberOfThreads = 1;
    static private int precision = 100;
    static private int granularity = 1;
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
                case "-g":
                    try {
                        granularity = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        granularity = 1;
                    }
                    break;
            }
        }
        long start = System.currentTimeMillis();
        Apfloat pi = calculatePiBS(precision, numberOfThreads, quietMode, granularity);
        long finish = System.currentTimeMillis();
        try {
            FileWriter myWriter = new FileWriter(outputFile);
            myWriter.write(String.valueOf(pi));
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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

    public static Apfloat calculatePiBS(long precision, int numberOfThreads, boolean quietMode, int granularity) {
        //Apfloat C = new Apfloat(640320L);
        //Apfloat C3_OVER_24 = new Apfloat( 10939058860032000L); //C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));

        Apfloat DIGITS_PER_TERM = new Apfloat(1.41816474627254e1); //ApfloatMath.log(new Apfloat(151931373056000L, 15), new Apfloat(10L));
        long N = (new Apfloat(precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE)).longValue();

        List<Range> ranges = Chudonovsky.calculateTermRangesBS(numberOfThreads * granularity, N);
        int rangesSize = ranges.size();
        //System.out.println("N: " + N);
        //System.out.println("ranges actual: " + ranges.size() + " theoretical:" + (numberOfThreads * granularity));
        List<Pair<TupleApfloat, Integer>> toSum = Collections.synchronizedList(new ArrayList<>());
        Thread[] tr = new Thread[numberOfThreads];
        long start = System.currentTimeMillis();
        for(int iter = 0; iter < granularity; iter++) {
            for (int index = 0; index < numberOfThreads; index++) {
                //System.out.println("iter * numOfThr + index = " + (iter * numberOfThreads + index));
                if(iter*numberOfThreads + index >= rangesSize) break;
                ChudonovskyBSRunnable r = new ChudonovskyBSRunnable(ranges.get( iter*numberOfThreads + index ), precision, toSum, DIGITS_PER_TERM, iter*numberOfThreads + index, quietMode, ranges);
                Thread t = new Thread(r);
                tr[index] = t;
                t.start();
            }
            for (int i = 0; i < numberOfThreads; i++) {
                try {
                    if(iter*numberOfThreads + i >= rangesSize) break;
                    tr[i].join();
                } catch (InterruptedException e) {
                    System.out.println("Something went wrong.");
                }
            }
        }
        System.out.println("Threads: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        Apfloat finalP = Apfloat.ONE;
        Apfloat finalQ = Apfloat.ONE;
        Apfloat finalT = Apfloat.ONE;
        for (int i = 0; i < toSum.size(); i++){
            if(toSum.get(i).getRight() == 0)
            {
                finalP = toSum.get(i).getLeft().getP();
                finalQ = toSum.get(i).getLeft().getQ();
                finalT = toSum.get(i).getLeft().getT();
                break;
            }
        }
        for(int i = 1; i < toSum.size(); i++) {
            for (int j = 0; j < toSum.size(); j++){
                Apfloat currentP;
                Apfloat currentQ;
                Apfloat currentT;
                if(toSum.get(j).getRight() == i){
                    currentP = toSum.get(j).getLeft().getP();
                    currentQ = toSum.get(j).getLeft().getQ();
                    currentT = toSum.get(j).getLeft().getT();
                    finalT = currentQ.multiply(finalT).add(finalP.multiply(currentT));
                    finalP = finalP.multiply(currentP);
                    finalQ = finalQ.multiply(currentQ);
                    break;
                }
            }
        }
        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision + 1));
        System.out.println("Merge: " + (System.currentTimeMillis() - start));
        return (finalQ).multiply(new Apfloat(426880L)).multiply(sqrtTenThousandAndFive).divide(finalT);
    }
    public static Apfloat calculatePiBS(long precision, boolean quietMode) {
        //Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = new Apfloat( 10939058860032000L); //C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
        Apfloat DIGITS_PER_TERM = ApfloatMath.log(C3_OVER_24.divide(new Apfloat(72, precision)), new Apfloat(10L));
        long N = (new Apfloat(precision).divide(DIGITS_PER_TERM).add(Apfloat.ONE)).longValue();

        TupleApfloat PQT = BS(0, N, true);

        Apfloat sqrtTenThousandAndFive = ApfloatMath.sqrt(new Apfloat(10005L, precision + 1));
        return (PQT.getQ()).multiply(new Apfloat(426880L)).multiply(sqrtTenThousandAndFive).divide(PQT.getT());
    }

    private static TupleApfloat BS(long a, long b, boolean flag) {
        //Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = new Apfloat( 10939058860032000L); //C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
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
            TupleApfloat am = BS(a, m, false);
            TupleApfloat mb = BS(m, b, false);
            if(flag){
                System.out.println(am.getP() + " " + am.getQ() + " " + am.getT());
                System.out.println(mb.getP() + " " + mb.getQ() + " " + mb.getT());
            }
            Pab = am.getP().multiply(mb.getP());
            Qab = am.getQ().multiply(mb.getQ());
            Tab = mb.getQ().multiply(am.getT()).add(am.getP().multiply(mb.getT()));
        }
        return new TupleApfloat(Pab, Qab, Tab);
    }

    public static List<Range> calculateTermRanges(long numberOfRanges, long precision) {

        long start = System.currentTimeMillis();
        if (numberOfRanges <= 0) {
            throw new IllegalArgumentException("Number of ranges should be positive.");
        }

        List<Range> ranges = new ArrayList<>();

        //Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = new Apfloat( 10939058860032000L); //C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));
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

    public static List<Range> calculateTermRangesBS(long numberOfRanges, long iterations) {
        if (numberOfRanges <= 0) {
            throw new IllegalArgumentException("Number of ranges should be positive.");
        }

        List<Range> ranges = new ArrayList<>();
        long rangeSize = (int) Math.ceil((double)iterations / numberOfRanges);
        for (int i = 0; i < iterations; i += rangeSize)
        {
            long lower = i;
            long upper = i + rangeSize;
            lower = Math.min(lower, iterations);
            upper = Math.min(upper, iterations);
            ranges.add(new Range(lower, upper));
            //System.out.print("l: " + lower + "-" + upper + " ");
        }
        //System.out.println("ranges: " + ranges.size());
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