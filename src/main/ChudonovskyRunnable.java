package main;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import java.util.List;

public class ChudonovskyRunnable implements Runnable {

    private final Range range;
    private long precision;
    private List<Pair<Apfloat, Apfloat>> termSums;
    private final int threadIndex;
    private final boolean quietMode;

    public ChudonovskyRunnable(Range range, long precision, List<Pair<Apfloat, Apfloat>> termSums, int index, boolean quietMode) {
        this.range = range;
        this.precision = precision;
        this.termSums = termSums;
        this.threadIndex = index;
        this.quietMode = quietMode;
    }

    @Override
    public void run() {

        if (!quietMode) System.out.println("Thread-" + threadIndex + " started.");
        long start = System.currentTimeMillis();

        // need one extra place for the 3, and one extra place for some rounding issues
        precision = precision + 2;
        Apfloat negativeOne = new Apfloat(-1L);

        Apfloat two = new Apfloat(2L);
        Apfloat three = new Apfloat(3L);
        Apfloat five = new Apfloat(5L);
        Apfloat six = new Apfloat(6L);
        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, precision));

        // find the first term in the series
        Apfloat k = new Apfloat(range.start);
        // NOTE: need to push out the precision in this term by a bit for the division to work properly.  8% is probably too high, but should be a safe estimate
        Apfloat a_k = ((k.longValue() % 2 == 0) ? Apfloat.ONE : negativeOne).multiply(ApintMath.factorial(6 * k.longValue())).precision((long) (precision * 1.08));
        Apfloat kFactorial = ApintMath.factorial(k.longValue());
        a_k = a_k.divide(ApintMath.factorial(three.multiply(k).longValue()).multiply(kFactorial.multiply(kFactorial).multiply(kFactorial)).multiply(ApfloatMath.pow(C, k.longValue() * 3)));

        Apfloat a_sum = new Apfloat(0L).add(a_k);
        Apfloat b_sum = new Apfloat(0L).add(k.multiply(a_k));
        k = k.add(Apfloat.ONE);

        for (long i = range.start + 1; i < range.end; i++) {
            a_k = a_k.multiply(negativeOne.multiply((six.multiply(k).subtract(five)).multiply(two.multiply(k).subtract(Apfloat.ONE)).multiply(six.multiply(k).subtract(Apfloat.ONE))));
            a_k = a_k.divide(k.multiply(k).multiply(k).multiply(C3_OVER_24));
            a_sum = a_sum.add(a_k);
            b_sum = b_sum.add(k.multiply(a_k));
            k = k.add(Apfloat.ONE);
        }

        if (range.start == range.end) {
            a_sum = new Apfloat(0L);
            b_sum = new Apfloat(0L);
        }

        this.termSums.add(new ImmutablePair<>(a_sum, b_sum));

        long finish = System.currentTimeMillis();
        if (!quietMode) {
            System.out.println("Thread-" + threadIndex + " stopped.");
            System.out.println("Thread-" + threadIndex + " execution time was(millis): " + (finish - start));
        }
    }
}