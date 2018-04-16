package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Polygon implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;

    public Polygon(Point[] points) {
        this.points = points;
        this.vtkType = VTKType.VTK_POLYGON;

        Vector a = new Vector(0.0, 0.0, 0.0);
        for (int i = 2; i < points.length; i++) {
            Vector v1 = new Vector(points[i - 1], points[0]);
            Vector v2 = new Vector(points[i - 1], points[i]);
            a = a.add(v1.cross(v2));
        }

        this.area = 0.5 * a.mag();
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
        throw new ArithmeticException("Cannot calculate length of a polygon.");
    }

    @Override
    public double area() {
        return area;
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a polygon.");
    }
}
