package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

public class Line implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double length;

    Line(Point p1, Point p2) {
        this.points = new Point[]{p1, p2};
        this.vtkType = VTKType.VTK_LINE;
        this.length = p1.distance(p2);
    }

    @Override
    public Point[] points() {
        return points;
    }

    @Override
    public VTKType vtkType() {
        return vtkType;
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public double area() {
        throw new ArithmeticException("Cannot calculate area of a line.");
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a line.");
    }
}
