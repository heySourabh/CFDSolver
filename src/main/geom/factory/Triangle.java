package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Triangle implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;

    public Triangle(Point p0, Point p1, Point p2) {
        this.points = new Point[]{p0, p1, p2};
        this.vtkType = VTKType.VTK_TRIANGLE;
        Vector v1 = new Vector(p0, p1);
        Vector v2 = new Vector(p0, p2);
        area = v1.cross(v2).mag() * 0.5;
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
        throw new ArithmeticException("Cannot calculate length of a triangle.");
    }

    @Override
    public double area() {
        return area;
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a triangle.");
    }

    @Override
    public Point centroid() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
