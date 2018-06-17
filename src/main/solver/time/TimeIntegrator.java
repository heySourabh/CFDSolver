package main.solver.time;

import main.solver.Norm;

public interface TimeIntegrator {
    void updateCellAverages(double time);

    void setCourantNum(double courantNum);

    double[] currentTotalResidual(Norm norm);
}
