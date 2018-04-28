package main.mesh;

import main.geom.Point;

public class Shape {
    public final double volume;
    public final Point centroid;

    public Shape(double volume, Point centroid) {
        this.volume = volume;
        this.centroid = centroid;
    }
}
