package main.solver.time;

import main.solver.Norm;

public interface TimeIntegrator {
    void updateCellAverages();

    void setCourantNum(double courantNum);

    void setTimeDiscretization(TimeDiscretization timeDiscretization);

    double[] currentTotalResidual(Norm norm);
}
