package main.mesh;

import main.physics.BoundaryCondition;

import java.util.List;

public class Boundary {
    public final String name;
    public final List<Face> faces;
    public final BoundaryCondition bc;

    public Boundary(String name, List<Face> faces, BoundaryCondition bc) {
        this.name = name;
        this.faces = faces;
        this.bc = bc;
    }
}
