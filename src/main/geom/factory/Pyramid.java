package main.geom.factory;

import main.geom.*;

import main.geom.GeometryHelper.TriGeom;

public class Pyramid implements Geometry {

    private final Point[] points;
    private final VTKType vtkType;
    private final double volume;
    private final Point centroid;

    public Pyramid(Point p0, Point p1, Point p2, Point p3, Point p4) {
        this.points = new Point[]{p0, p1, p2, p3, p4};
        this.vtkType = VTKType.VTK_PYRAMID;

        TriGeom[] surfaceTriangles = {
                new TriGeom(p0, p2, p1),
                new TriGeom(p0, p3, p2),
                new TriGeom(p0, p1, p4),
                new TriGeom(p1, p2, p4),
                new TriGeom(p2, p3, p4),
                new TriGeom(p3, p0, p4)};

        this.volume = GeometryHelper.volume(surfaceTriangles);
        this.centroid = GeometryHelper.centroid(surfaceTriangles);

        // The algorithm below may fail in case of non-convex geometry:
//        double vol1 = new Tetra(p0, p1, p2, p4).volume();
//        double vol2 = new Tetra(p0, p2, p3, p4).volume();
//        this.volume = vol1 + vol2;
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

    @Override
    public Point centroid() {
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        throw new ArithmeticException("Cannot calculate normal of a pyramid.");
    }
}
