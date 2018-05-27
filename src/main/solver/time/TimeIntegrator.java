package main.solver.time;

import main.solver.NormType;

public interface TimeIntegrator {
    void updateCellAverages();

    double[] currentTotalResidual(NormType normType);
}
