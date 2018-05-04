package main.geom.factory;

import main.geom.*;
import main.geom.GeometryHelper.TriGeom;

public class Wedge implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double volume;
    private final Point centroid;

    public Wedge(Point p0, Point p1, Point p2, Point p3, Point p4, Point p5) {
        this.points = new Point[]{p0, p1, p2, p3, p4, p5};
        this.vtkType = VTKType.VTK_WEDGE;

        TriGeom[] surfaceTriangles = {
                new TriGeom(p0, p1, p2),
                new TriGeom(p3, p5, p4),
                new TriGeom(p0, p2, p5),
                new TriGeom(p5, p3, p0),
                new TriGeom(p1, p4, p5),
                new TriGeom(p5, p2, p1),
                new TriGeom(p0, p3, p4),
                new TriGeom(p4, p1, p0)
        };

        this.volume = GeometryHelper.volume(surfaceTriangles);
        this.centroid = GeometryHelper.centroid(surfaceTriangles);
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

    @Override
    public Point centroid() {
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        throw new ArithmeticException("Cannot calculate normal of a wedge.");
    }
}
