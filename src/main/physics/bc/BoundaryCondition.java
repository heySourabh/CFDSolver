package main.physics.bc;

import main.mesh.Face;

public interface BoundaryCondition {
    void setGhostCellValues(Face face, double time);

    double[] convectiveFlux(Face face, double time);
}
