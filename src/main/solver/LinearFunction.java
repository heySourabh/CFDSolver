package main.solver;

import main.geom.Point;
import main.geom.Vector;

public class LinearFunction {
    private final Point point;
    private final double value;
    private final Vector gradient;

    public LinearFunction(Point point, double value, Vector gradient) {
        this.point = point;
        this.value = value;
        this.gradient = gradient;
    }

    public Vector gradient() {
        return gradient;
    }

    public double valueAt(Point p) {
        return value + new Vector(point, p).dot(gradient);
    }
}
