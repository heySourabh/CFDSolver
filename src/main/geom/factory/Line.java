package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

import java.util.Arrays;

public class Line implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double length;
    private final Point centroid;

    public Line(Point p0, Point p1) {
        this.points = new Point[]{p0, p1};
        this.vtkType = VTKType.VTK_LINE;
        this.length = p0.distance(p1);
        this.centroid = new Point(
                (p0.x + p1.x) * 0.5,
                (p0.y + p1.y) * 0.5,
                (p0.z + p1.z) * 0.5);
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
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        throw new ArithmeticException("Cannot calculate a unique normal of a line in 3D.");
    }
}