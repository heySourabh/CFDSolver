package main.physics.bc;

import main.mesh.Face;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.util.DoubleArray;

public class PressureOutletBC implements BoundaryCondition {
    private final ArtificialCompressibilityEquations govEqn;
    private final double pressure;

    public PressureOutletBC(ArtificialCompressibilityEquations govEqn, double pressure) {
        this.govEqn = govEqn;
        this.pressure = pressure;
    }

    @Override
    public void setGhostCellValues(Face face) {
        // Velocity is simply copied, pressure is linearly extrapolated
        double[] insideConservativeVars = face.left.U;
        double[] insidePrimitiveVars = govEqn.primitiveVars(insideConservativeVars);
        double pi = insidePrimitiveVars[0];
        double ui = insidePrimitiveVars[1];
        double vi = insidePrimitiveVars[2];
        double wi = insidePrimitiveVars[3];

        double p_ghost = 2.0 * this.pressure - pi;

        double[] ghostCellPrimitiveVars = {
                p_ghost, ui, vi, wi
        };

        double[] ghostCellConservativeVars = govEqn.conservativeVars(ghostCellPrimitiveVars);

        DoubleArray.copy(ghostCellConservativeVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        // velocity is defined by inside cell, pressure is defined
        double[] insideConservativeVars = face.left.U;
        double ui = insideConservativeVars[1];
        double vi = insideConservativeVars[2];
        double wi = insideConservativeVars[3];
        double[] primitiveVars = {
                this.pressure, ui, vi, wi
        };

        double[] conservativeVars = govEqn.conservativeVars(primitiveVars);

        return govEqn.convection().flux(conservativeVars, face.surface.unitNormal());
    }
}
