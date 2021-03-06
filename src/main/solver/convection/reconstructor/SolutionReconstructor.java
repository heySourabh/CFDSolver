package main.solver.convection.reconstructor;

import main.geom.Point;
import main.mesh.Cell;

public interface SolutionReconstructor {
    void reconstruct();

    double[] conservativeVars(Cell cell, Point atPoint);
}
