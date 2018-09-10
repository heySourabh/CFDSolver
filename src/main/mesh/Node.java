package main.mesh;

import main.geom.Point;

import java.util.ArrayList;

public class Node {
    public final double x, y, z;
    public final ArrayList<Cell> neighbors;
    public final double[] U;

    public Node(Point p, int numVars) {
        this(p.x, p.y, p.z, numVars);
    }

    public Node(double x, double y, double z, int numVars) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.neighbors = new ArrayList<>();
        this.U = new double[numVars];
    }

    public Point location() {
        return new Point(x, y, z);
    }

    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
