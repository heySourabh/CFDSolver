package main.physics;

import main.mesh.Face;

public interface BoundaryCondition {
    void setGhostCellValues(Face face);

    double[] convectiveFlux(Face face);
}
