package main.solver;

public interface TimeIntegrator {
    void updateCellAverages();

    double[] currentTotalResidual(NormType normType);
}
