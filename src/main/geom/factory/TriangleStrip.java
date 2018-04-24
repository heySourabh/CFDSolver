package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;

import java.util.stream.IntStream;

public class TriangleStrip implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double area;

    public TriangleStrip(Point... points) {
        this.points = points;
        this.vtkType = VTKType.VTK_TRIANGLE_STRIP;

        this.area = IntStream.range(0, points.length - 2)
                .mapToObj(i -> new Triangle(points[i], points[i + 1], points[i + 2]))
                .mapToDouble(Triangle::area)
                .sum();
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
        return area;
    }

    @Override
    public double volume() {
        throw new ArithmeticException("Cannot calculate volume of a triangle_strip.");
    }
}
