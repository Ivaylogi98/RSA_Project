package main;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApintMath;

import javax.swing.plaf.basic.BasicPasswordFieldUI;
import java.util.ArrayList;
import java.util.List;

public class ChudonovskyBSRunnable implements Runnable {

    private final Range range;
    private List<Pair<TupleApfloat, Integer>> toSum;
    private final long precision;
    private final int threadIndex;
    private final boolean quietMode;
    private final Apfloat DIGITS_PER_TERM;

    public ChudonovskyBSRunnable(Range range, long precision, List<Pair<TupleApfloat, Integer>> toSum, Apfloat DIGITS_PER_TERM, int index, boolean quietMode) {
        this.range = range;
        this.toSum = toSum;
        this.threadIndex = index;
        this.quietMode = quietMode;
        this.precision = precision;
        this.DIGITS_PER_TERM = DIGITS_PER_TERM;
    }

    @Override
    public void run() {

        if (!this.quietMode) System.out.println("Thread-" + this.threadIndex + " started.");
        long start = System.currentTimeMillis();

        TupleApfloat PQT = BS(this.range.start, this.range.end);

        Pair<TupleApfloat, Integer> toAdd = new ImmutablePair<>(PQT, threadIndex);
        this.toSum.add(toAdd);

        long finish = System.currentTimeMillis();
        if (!this.quietMode) {
            System.out.println("Thread-" + this.threadIndex + " stopped.");
            System.out.println("Thread-" + this.threadIndex + " execution time was(millis): " + (finish - start));
        }
    }
    private TupleApfloat BS(long a, long b) {
        Apfloat C = new Apfloat(640320L);
        Apfloat C3_OVER_24 = (C.multiply(C).multiply(C)).divide(new Apfloat(24, this.precision));
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
            Tab = mb.getQ().multiply(am.getT()).add(am.getP().multiply(mb.getT()));
        }
        return new TupleApfloat(Pab, Qab, Tab);
    }
}