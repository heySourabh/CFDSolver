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

    public ExplicitEulerTimeIntegrator(Mesh mesh, List<ResidualCalculator> residuals, int numVars) {
        this.mesh = mesh;
        this.residuals = residuals;
        this.U = new double[mesh.cells().size()][numVars];
    }

    @Override
    public void updateCellAverages(double time) {
        saveCurrentAverages();
        setResiduals(time);
        calculateNewAverages();
    }

    @Override
    public double[] currentTotalResidual(Norm norm) {
        return new double[0];
    }

    private void setResiduals(double time) {
        mesh.cellStream().forEach(cell -> Arrays.fill(cell.residual, 0.0));
        residuals.forEach(r -> r.updateCellResiduals(time));
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
