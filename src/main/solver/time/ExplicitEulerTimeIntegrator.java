package main.solver.time;

import main.mesh.Cell;
import main.mesh.Mesh;
import main.solver.Norm;
import main.solver.ResidualCalculator;

import java.util.Arrays;
import java.util.List;

import static main.util.DoubleArray.*;

public class ExplicitEulerTimeIntegrator implements TimeIntegrator {

    private final Mesh mesh;
    private final List<ResidualCalculator> residuals;
    private final double[][] U;
    private final TimeStep timeStep;
    private double courantNum = 1.0; // default

    public ExplicitEulerTimeIntegrator(Mesh mesh, List<ResidualCalculator> residuals, TimeStep timeStep, int numVars) {
        this.mesh = mesh;
        this.residuals = residuals;
        this.timeStep = timeStep;
        this.U = new double[mesh.cells().size()][numVars];
    }

    @Override
    public void setCourantNum(double courantNum) {
        this.courantNum = courantNum;
    }

    @Override
    public void updateCellAverages(double time) {
        saveCurrentAverages();
        setResiduals(time);
        timeStep.updateCellTimeSteps(courantNum);
        calculateNewAverages();
    }

    @Override
    public double[] currentTotalResidual(Norm norm) {
        return new double[0];
    }

    private void setResiduals(double time) {
        mesh.cellStream().forEach(cell -> Arrays.fill(cell.residual, 0.0));
        residuals.forEach(residual -> residual.updateCellResiduals(time));
    }

    private void saveCurrentAverages() {
        mesh.cellStream().forEach(cell -> copy(cell.U, U[cell.index]));
    }

    private void calculateNewAverages() {
        mesh.cellStream().forEach(this::calculateNewAverages);
    }

    private void calculateNewAverages(Cell cell) {
        double dt_vol = cell.dt / cell.shape.volume;
        double[] U = subtract(cell.U, multiply(cell.residual, dt_vol));
        copy(U, cell.U);
    }
}
