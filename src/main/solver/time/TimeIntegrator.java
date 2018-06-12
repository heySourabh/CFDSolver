package main.solver.time;

import main.solver.Norm;

public interface TimeIntegrator {
    void updateCellAverages(double time);

    double[] currentTotalResidual(Norm norm);
}
