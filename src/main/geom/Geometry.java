package main.geom;

public interface Geometry {

    Point[] points();

    VTKType vtkType();

    double length();

    double area();

    double volume();
}
