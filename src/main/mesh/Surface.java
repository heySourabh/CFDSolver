package main.mesh;

import main.geom.Point;

public class Surface {
    public final double area;
    public final Point centroid;

    public Surface(double area, Point centroid) {
        this.area = area;
        this.centroid = centroid;
    }
}
