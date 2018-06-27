package main.mesh;

import main.geom.Point;
import main.geom.Vector;

public class Surface {
    public final double area;
    public final Point centroid;
    public Vector unitNormal;

    public Surface(double area, Point centroid, Vector unitNormal) {
        this.area = area;
        this.centroid = centroid;
        this.unitNormal = unitNormal;
    }
}
