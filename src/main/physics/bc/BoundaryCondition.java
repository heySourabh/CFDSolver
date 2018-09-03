package main.physics.bc;

import main.mesh.Face;

public interface BoundaryCondition {
    void setGhostCellValues(Face face);

    double[] convectiveFlux(Face face);
}
