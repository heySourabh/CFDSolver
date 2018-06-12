package main.solver;

import main.geom.Point;
import main.mesh.Cell;

public interface SolutionReconstructor {
    void reconstruct();

    double[] conservativeVars(Cell cell, Point atPoint);
}
