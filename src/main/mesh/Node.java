package main.mesh;

import java.util.ArrayList;

public class Node {
    public final double x, y, z;
    public final ArrayList<Cell> neighbors;

    public Node(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        neighbors = new ArrayList<>();
    }
}
