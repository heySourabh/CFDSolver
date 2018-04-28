package main.geom;

import main.geom.factory.Triangle;

import java.util.Arrays;

/**
 * An interface for defining various VTK Geometry.
 */
public interface Geometry {

    /**
     * Ordered points as defined by the VTK documentation
     *
     * @return An array of <code>Point</code> objects following the sequence as defined by the VTK geometry.
     */
    Point[] points();

    VTKType vtkType();

    double length();

    double area();

    double volume();

    Point centroid();

    Vector unitNormal();

    default double volumeUnder(Triangle tri) {
        Point[] p = tri.points();
        double xbar = (p[0].x + p[1].x + p[2].x) / 3.0;
        Vector vab = new Vector(p[0], p[1]);
        Vector vac = new Vector(p[0], p[2]);

        double ax = vab.y * vac.z - vac.y * vab.z; // x-component of cross product

        return ax * xbar * 0.5;
    }

    /**
     * Calculates the volume of a solid formed by a set of triangles.
     * The triangles must have points either all-clockwise or all-anti-clockwise, looking from outside of the solid.
     *
     * @param triangles array of triangles
     * @return non-negative volume enclosed by the set of triangles
     */
    default double volume(Triangle[] triangles) {
        double volume = 0.0;
        for (Triangle t : triangles) {
            volume += volumeUnder(t);
        }
        return Math.abs(volume);
    }

    default boolean geomEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Geometry geom = (Geometry) o;

        return Arrays.equals(points(), geom.points());
    }

    default int geomHashCode() {
        return Arrays.hashCode(points());
    }
}
