package main.solver.time;

import main.mesh.Cell;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.util.DoubleArray;

public class TwoPointTimeDiscretization implements TimeDiscretization {
    private final Mesh mesh;
    private final GoverningEquations govEqn;
    private final double real_dt;

    public TwoPointTimeDiscretization(Mesh mesh, GoverningEquations govEqn, double real_dt) {
        this.mesh = mesh;
        this.govEqn = govEqn;
        this.real_dt = real_dt;
    }

    @Override
    public void updateCellResiduals() {
        mesh.cellStream().forEach(this::updateResidual);
    }

    @Override
    public double dt() {
        return real_dt;
    }

    @Override
    public void shiftSolution() {
        mesh.cellStream().forEach(this::shiftSolution);
    }

    private void shiftSolution(Cell cell) {
        DoubleArray.copy(cell.U, cell.Wn);
    }

    private void updateResidual(Cell cell) {
        double[] Wnp1 = govEqn.realVars(cell.U);
        double vol_dt = cell.shape.volume / real_dt;
        int numVars = govEqn.numVars();

        double[] residual = new double[numVars];
        for (int var = 0; var < numVars; var++) {
            residual[var] = vol_dt * (Wnp1[var] - cell.Wn[var]);
        }

        DoubleArray.increment(cell.residual, residual);
    }
}
