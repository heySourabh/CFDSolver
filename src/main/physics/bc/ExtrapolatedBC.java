package main.physics.bc;

import main.mesh.Cell;
import main.mesh.Face;
import main.physics.goveqn.GoverningEquations;
import main.util.DoubleArray;

public class ExtrapolatedBC implements BoundaryCondition {
    private final GoverningEquations govEqn;

    public ExtrapolatedBC(GoverningEquations govEqn) {
        this.govEqn = govEqn;
    }

    @Override
    public void setGhostCellValues(Face face) {
        Cell outerCell = face.right;
        Cell innerCell = face.left;
        DoubleArray.copy(innerCell.U, outerCell.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        double[] innerVars = face.left.U;
        return govEqn.convection().flux(innerVars, face.surface.unitNormal);
    }
}
