package main.mesh;

import main.geom.Point;

import java.util.ArrayList;

public class Node {
    public final double x, y, z;
    public final ArrayList<Cell> neighbors;

    public Node(Point p) {
        this(p.x, p.y, p.z);
    }

    public Node(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        neighbors = new ArrayList<>();
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
