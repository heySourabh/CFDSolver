package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

public class Pyramid implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double volume;

    public Pyramid(Point p0, Point p1, Point p2, Point p3, Point p4) {
        this.points = new Point[]{p0, p1, p2, p3, p4};
        this.vtkType = VTKType.VTK_PYRAMID;

        double vol1 = new Tetra(p0, p1, p2, p4).volume();
        double vol2 = new Tetra(p0, p2, p3, p4).volume();

        this.volume = vol1 + vol2;
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
        throw new ArithmeticException("Cannot calculate length of a pyramid.");
    }

    @Override
    public double area() {
        throw new ArithmeticException("Cannot calculate area of a pyramid.");
    }

    @Override
    public double volume() {
        return volume;
    }
}
