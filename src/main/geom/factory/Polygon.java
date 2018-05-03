package main.geom.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Polygon implements Geometry {
    private final Point[] points;
    private final VTKType vtkType;
    private final double area;
    private final Point centroid;
    private final Vector unitNormal;

    public Polygon(Point[] points) {
        this.points = points;
        this.vtkType = VTKType.VTK_POLYGON;

        Vector areaVector = new Vector(0.0, 0.0, 0.0);
        Point p0 = points[0];

        Vector cx = new Vector(0, 0, 0);
        Vector cy = new Vector(0, 0, 0);
        Vector cz = new Vector(0, 0, 0);

        for (int i = 2; i < points.length; i++) {
            Point pa = points[i - 1];
            Point pb = points[i];
            Vector v1 = new Vector(pa, pb);
            Vector v2 = new Vector(pa, p0);

            Vector a = v1.cross(v2);
            areaVector = areaVector.add(a);

            // divided by 3 later
            cx = cx.add(a.mult(p0.x + pa.x + pb.x));
            cy = cy.add(a.mult(p0.y + pa.y + pb.y));
            cz = cz.add(a.mult(p0.z + pa.z + pb.z));
        }

        this.area = 0.5 * areaVector.mag();
        this.unitNormal = areaVector.unit();
        centroid = new Point(
                cx.dot(unitNormal) / (6.0 * area),
                cy.dot(unitNormal) / (6.0 * area),
                cz.dot(unitNormal) / (6.0 * area));
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

    @Override
    public Point centroid() {
        return centroid;
    }

    @Override
    public Vector unitNormal() {
        return unitNormal;
    }
}