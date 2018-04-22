package main.geom;

public class Point {
    public final double x, y, z;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distance(Point other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point other = (Point) o;

        return distance(other) < 1e-15;
    }

    @Override
    public int hashCode() {
        return (int) (Math.round(x) + Math.round(y) + Math.round(z));
    }
}
