package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Vertex implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;

    public Vertex(Point p) {
        this.points = new Point[]{p};
        this.vtkType = VTKType.VTK_VERTEX;
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
        throw new ArithmeticException("Cannot calculate length of a vertex.");
    }

    @Override
    public double area() {
        throw new ArithmeticException("Cannot calculate area of a vertex.");
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a vertex.");
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
