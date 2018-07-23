package main.mesh;

import main.physics.bc.BoundaryCondition;

import java.util.List;

public class Boundary {
    public final String name;
    public final List<Face> faces;
    public BoundaryCondition bc;

    public Boundary(String name, List<Face> faces, BoundaryCondition bc) {
        this.name = name;
        this.faces = faces;
        this.bc = bc;
    }

    public void setBC(BoundaryCondition bc) {
        this.bc = bc;
    }
}
