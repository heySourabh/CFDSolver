package main.solver;

import main.geom.Point;
import main.mesh.Cell;

public interface SolutionReconstructor {
    double[] conservativeVars(Cell cell, Point atPoint);
}
