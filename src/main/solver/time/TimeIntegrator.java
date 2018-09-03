package main.solver.time;

import main.solver.Norm;

public interface TimeIntegrator {
    void updateCellAverages();

    void setCourantNum(double courantNum);

    double[] currentTotalResidual(Norm norm);
}
