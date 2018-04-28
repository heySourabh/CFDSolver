package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Tetra implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double volume;

    public Tetra(Point p0, Point p1, Point p2, Point p3) {
        this.points = new Point[]{p0, p1, p2, p3};
        this.vtkType = VTKType.VTK_TETRA;

        // using the formula from wikipedia, which is given as:
        // For points a, b, c, d = (p0, p1, p2, p3)
        // Volume = (| (a - d) . ((b - d) x (c - d)) |) / 6
        // Tested for accuracy

        Vector v30 = new Vector(p3, p0);
        Vector v31 = new Vector(p3, p1);
        Vector v32 = new Vector(p3, p2);

        volume = Math.abs(v30.dot(v31.cross(v32))) / 6.0;
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
        throw new ArithmeticException("Cannot calculate length of a tetra.");
    }

    @Override
    public double area() {
        throw new ArithmeticException("Cannot calculate area of a tetra.");
    }

    @Override
    public double volume() {
        return volume;
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
