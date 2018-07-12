package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TriangleStrip implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double area;
    private final Point centroid;
    private final Vector unitNormal;

    public TriangleStrip(Point... points) {
        this.points = points;
        this.vtkType = VTKType.VTK_TRIANGLE_STRIP;

        List<Triangle> triangles = IntStream.range(0, points.length - 2)
                .mapToObj(i -> new Triangle(points[i], points[i + 1], points[i + 2]))
                .collect(Collectors.toList());
        this.area = triangles.stream()
                .mapToDouble(Triangle::area)
                .sum();
        this.centroid = triangles.stream()
                .map(t -> t.centroid().toVector().mult(t.area()))
                .reduce(new Vector(0, 0, 0), Vector::add)
                .mult(1.0 / this.area)
                .toPoint();
        this.unitNormal = IntStream.range(0, triangles.size())
                .mapToObj(i -> scaledNormal(i, triangles.get(i)))
                .reduce(new Vector(0, 0, 0), Vector::add)
                .mult(1.0 / this.area)
                .unit();
    }

    private Vector scaledNormal(int i, Triangle triangle) {
        return triangle.unitNormal()
                .mult(i % 2 == 0 ? 1 : -1)
                .mult(triangle.area());
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

    @Override
    public Point centroid() {
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        return unitNormal;
    }
}
