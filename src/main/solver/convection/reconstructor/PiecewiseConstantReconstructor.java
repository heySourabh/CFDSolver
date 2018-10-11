package main.solver.convection.reconstructor;

import main.geom.Point;
import main.mesh.Cell;
import main.util.DoubleArray;

public class PiecewiseConstantReconstructor implements SolutionReconstructor {
    @Override
    public void reconstruct() {
        // nothing needs to be done for piecewise constant reconstructor
    }

    @Override
    public double[] conservativeVars(Cell cell, Point atPoint) {
        return DoubleArray.copyOf(cell.U);
    }
}
