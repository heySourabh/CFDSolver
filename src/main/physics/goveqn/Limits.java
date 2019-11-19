package main.physics.goveqn;

public class Limits {
    public final double min, max;

    public static final Limits INFINITE = new Limits(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public Limits(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Limits limits = (Limits) o;

        if (Double.compare(limits.min, min) != 0) return false;
        return Double.compare(limits.max, max) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(min);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
