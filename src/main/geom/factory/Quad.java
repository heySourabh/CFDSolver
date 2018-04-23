package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Quad implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;

    public Quad(Point p0, Point p1, Point p2, Point p3) {
        this.points = new Point[]{p0, p1, p2, p3};
        this.vtkType = VTKType.VTK_QUAD;

        Vector a1 = new Vector(p1, p2).cross(new Vector(p1, p0));
        Vector a2 = new Vector(p3, p0).cross(new Vector(p3, p2));

        this.area = 0.5 * a1.add(a2).mag();
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
    public boolean equals(Object o) {
        return geomEquals(o);
    }

    @Override
    public int hashCode() {
        return geomHashCode();
    }
}
