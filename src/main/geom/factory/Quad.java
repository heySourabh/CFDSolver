package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Quad implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;
    private final Point centroid;
    private final Vector unitNormal;

    public Quad(Point p0, Point p1, Point p2, Point p3) {
        this.points = new Point[]{p0, p1, p2, p3};
        this.vtkType = VTKType.VTK_QUAD;

        Polygon poly = new Polygon(new Point[]{p0, p1, p2, p3});

        this.area = poly.area();
        this.centroid = poly.centroid();
        this.unitNormal = poly.unitNormal();
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
        throw new ArithmeticException("Cannot calculate length of a quad.");
    }

    @Override
    public double area() {
        return area;
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a quad.");
    }

    @Override
    public Point centroid() {
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        return unitNormal;
    }
}
