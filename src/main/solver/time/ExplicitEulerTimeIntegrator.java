package main.solver.time;

import main.mesh.Boundary;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.physics.bc.BoundaryCondition;
import main.solver.Norm;
import main.solver.ResidualCalculator;
import main.util.DoubleArray;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static main.util.DoubleArray.*;

public class ExplicitEulerTimeIntegrator implements TimeIntegrator {

    private final Mesh mesh;
    private final List<ResidualCalculator> residuals;
    private final int numVars;
    private final int numCells;
    private final double[][] U;
    private final TimeStep timeStep;
    private double courantNum = 1.0; // default

    public ExplicitEulerTimeIntegrator(Mesh mesh, List<ResidualCalculator> residuals, TimeStep timeStep, int numVars) {
        this.mesh = mesh;
        this.residuals = residuals;
        this.timeStep = timeStep;
        this.numVars = numVars;
        this.numCells = mesh.cells().size();
        this.U = new double[numCells][numVars];
    }

    @Override
    public void setCourantNum(double courantNum) {
        this.courantNum = courantNum;
    }

    @Override
    public void updateCellAverages() {
        saveCurrentAverages();
        setGhostCellValues();
        setResiduals();
        timeStep.updateCellTimeSteps(courantNum);
        calculateNewAverages();
    }

    @Override
    public double[] currentTotalResidual(Norm norm) {
        double[] zeros = new double[numVars];

        Stream<double[]> absResidualStream = mesh.cellStream()
                .map(cell -> apply(cell.U, U[cell.index], (e1, e2) -> (e1 - e2) / cell.dt))
                .map(DoubleArray::abs);

        double[] totalResidue;
        switch (norm) {
            case ONE_NORM:
                totalResidue = absResidualStream
                        .reduce(zeros, DoubleArray::add);
                totalResidue = multiply(totalResidue, 1.0 / numCells);
                break;

            case TWO_NORM:
                totalResidue = absResidualStream
                        .map(DoubleArray::sqr)
                        .reduce(zeros, DoubleArray::add);
                totalResidue = apply(totalResidue, Math::sqrt);
                totalResidue = multiply(totalResidue, 1.0 / numCells);
                break;

            case INFINITY_NORM:
                totalResidue = absResidualStream
                        .reduce(zeros, (d1, d2) -> apply(d1, d2, Math::max));
                break;

            default:
                throw new IllegalArgumentException("Norm " + norm + " is not implemented.");
        }

        // normalize
        double[] varMagnitude = mesh.cellStream()
                .map(cell -> cell.U)
                .map(DoubleArray::abs)
                .reduce(zeros, DoubleArray::add);
        varMagnitude = multiply(varMagnitude, 1.0 / numCells);
        varMagnitude = apply(varMagnitude, e -> (e < 1e-12 ? 1.0 : e)); // avoid div by zero
        for (int i = 0; i < numVars; i++) {
            totalResidue[i] /= varMagnitude[i];
        }

        return totalResidue;
    }

    private void setGhostCellValues() {
        mesh.boundaryStream().forEach(this::setGhostCellValues);
    }

    private void setGhostCellValues(Boundary boundary) {
        BoundaryCondition bc = boundary.bc().orElseThrow(
                () -> new IllegalArgumentException("Boundary condition is not defined."));
        boundary.faces.forEach(bc::setGhostCellValues);
    }

    private void setResiduals() {
        mesh.cellStream().forEach(cell -> Arrays.fill(cell.residual, 0.0));
        residuals.forEach(ResidualCalculator::updateCellResiduals);
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
