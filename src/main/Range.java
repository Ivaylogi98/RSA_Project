package main;

public class Range {
    public long start;
    public long end;

    public Range(long start, long end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("Bounds should be strictly positive.");
        }

        if (end < start) {
            throw new IllegalArgumentException("Upper bound should be greater than lower bound.");
        }

        this.start = start;
        this.end = end;
    }

}