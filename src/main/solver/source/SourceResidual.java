package main.solver.source;

import main.mesh.Cell;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.solver.ResidualCalculator;

import static main.util.DoubleArray.decrement;
import static main.util.DoubleArray.multiply;

public class SourceResidual implements ResidualCalculator {

    private final Mesh mesh;
    private final GoverningEquations govEqn;

    public SourceResidual(Mesh mesh, GoverningEquations govEqn) {
        this.mesh = mesh;
        this.govEqn = govEqn;
    }

    @Override
    public void updateCellResiduals() {
        mesh.cellStream().forEach(this::updateResidual);
    }

    private void updateResidual(Cell cell) {
        double[] residual = multiply(govEqn.source().sourceVector(cell.U, cell.gradientU), cell.shape.volume);
        decrement(cell.residual, residual);
    }
}
