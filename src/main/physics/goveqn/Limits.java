package main.physics.goveqn;

public class Limits {
    public final double min, max;

    public static final Limits INFINITE = new Limits(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public Limits(double min, double max) {
        this.min = min;
        this.max = max;
    }
}
