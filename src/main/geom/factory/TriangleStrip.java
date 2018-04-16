package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

public class TriangleStrip implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double area;

    TriangleStrip(Point[] points) {
        this.points = points;
        this.vtkType = VTKType.VTK_TRIANGLE_STRIP;
        this.area = Double.NaN;
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
        throw new ArithmeticException("Cannot calculate length of a triangle_strip.");
    }

    @Override
    public double area() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a triangle_strip.");
    }
}
