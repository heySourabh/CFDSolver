package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Triangle implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;
    private final Point centroid;
    private final Vector unitNormal;

    public Triangle(Point p0, Point p1, Point p2) {
        this.points = new Point[]{p0, p1, p2};
        this.vtkType = VTKType.VTK_TRIANGLE;
        Vector v1 = new Vector(p0, p1);
        Vector v2 = new Vector(p0, p2);
        Vector areaVector = v1.cross(v2);
        this.area = areaVector.mag() * 0.5;
        this.centroid = new Point(
                (p0.x + p1.x + p2.x) / 3.0,
                (p0.y + p1.y + p2.y) / 3.0,
                (p0.z + p1.z + p2.z) / 3.0
        );
        this.unitNormal = areaVector.unit();
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
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        return unitNormal;
    }
}
