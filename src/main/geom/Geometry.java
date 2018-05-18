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
}
