package main.mesh;

import main.physics.bc.BoundaryCondition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Boundary {
    public final String name;
    public final List<Face> faces;
    private BoundaryCondition bc;

    public Boundary(String name, List<Face> faces, BoundaryCondition bc) {
        this.name = Objects.requireNonNull(name, "Boundary 'name' cannot be null.");
        this.faces = Objects.requireNonNull(faces, "Boundary 'faces' cannot be null.");
        this.bc = bc;
    }

    public void setBC(BoundaryCondition bc) {
        this.bc = Objects.requireNonNull(bc);
    }

    public Optional<BoundaryCondition> bc() {
        return Optional.ofNullable(bc);
    }
}
