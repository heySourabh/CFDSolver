package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class PolyLine implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double length;

    public PolyLine(Point[] points) {
        this.points = points;
        this.vtkType = VTKType.VTK_POLY_LINE;

        double l = 0.0;
        for (int i = 0; i < points.length - 1; i++) {
            l += points[i].distance(points[i + 1]);
        }
        this.length = l;
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
        throw new ArithmeticException("Cannot calculate area of a polyline.");
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a polyline.");
    }

    @Override
    public Point centroid() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Vector unitNormal() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
