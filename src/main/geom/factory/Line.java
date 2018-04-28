package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

import java.util.Arrays;

public class Line implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double length;

    public Line(Point p0, Point p1) {
        this.points = new Point[]{p0, p1};
        this.vtkType = VTKType.VTK_LINE;
        this.length = p0.distance(p1);
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

    @Override
    public Point centroid() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
