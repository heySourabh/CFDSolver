package main.geom;

public class Vector {
    private final static double EPS = 1e-12;

    public final double x, y, z;

    public final static Vector ZERO = new Vector(0, 0, 0);

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(Point from, Point to) {
        this(to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    public Point toPoint() {
        return new Point(x, y, z);
    }

    public Vector add(Vector other) {
        return new Vector(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z);
    }

    public Vector sub(Vector other) {
        return new Vector(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z);
    }

    public Vector mult(double scalar) {
        return new Vector(x * scalar, y * scalar, z * scalar);
    }

    public double magSqr() {
        return x * x + y * y + z * z;
    }

    public double mag() {
        return Math.sqrt(magSqr());
    }

    public Vector unit() {
        double mag = mag();
        if (mag < EPS) {
            throw new ArithmeticException("Divide by zero.");
        }

        return new Vector(x / mag, y / mag, z / mag);
    }

    public double dot(Vector other) {
        return (this.x * other.x + this.y * other.y + this.z * other.z);
    }

    public Vector cross(Vector other) {
        double xComp = this.y * other.z - other.y * this.z;
        double yComp = other.x * this.z - this.x * other.z;
        double zComp = this.x * other.y - other.x * this.y;

        return new Vector(xComp, yComp, zComp);
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
