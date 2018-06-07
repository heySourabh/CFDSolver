package main.solver;

import main.geom.Point;
import main.mesh.Cell;
import main.util.DoubleArray;

public class PiecewiseConstantSolutionReconstructor implements SolutionReconstructor {
    @Override
    public double[] conservativeVars(Cell cell, Point atPoint) {
        return DoubleArray.copyOf(cell.U);
    }
}
