package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

public class Wedge implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double volume;

    public Wedge(Point p0, Point p1, Point p2, Point p3, Point p4, Point p5) {
        this.points = new Point[]{p0, p1, p2, p3, p4, p5};
        vtkType = VTKType.VTK_WEDGE;

        volume = volume(new Triangle[]{
                new Triangle(p0, p1, p2),
                new Triangle(p3, p5, p4),
                new Triangle(p0, p2, p5),
                new Triangle(p5, p3, p0),
                new Triangle(p1, p4, p5),
                new Triangle(p5, p2, p1),
                new Triangle(p0, p3, p4),
                new Triangle(p4, p1, p0)
        });
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
        throw new ArithmeticException("Cannot calculate length of a wedge.");
    }

    @Override
    public double area() {
        throw new ArithmeticException("Cannot calculate area of a wedge.");
    }

    @Override
    public double volume() {
        return volume;
    }
}
