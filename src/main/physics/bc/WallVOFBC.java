package main.physics.bc;

import main.geom.Vector;
import main.mesh.Face;
import main.physics.goveqn.ArtificialCompressibilityEquations;
import main.physics.goveqn.ArtificialCompressibilityVOFEquations;
import main.util.DoubleArray;

public class WallVOFBC implements BoundaryCondition {

    private final ArtificialCompressibilityVOFEquations govEqn;
    private final Vector wallVelocity;

    public WallVOFBC(ArtificialCompressibilityVOFEquations govEqn, Vector wallVelocity) {
        this.govEqn = govEqn;
        this.wallVelocity = wallVelocity;
    }

    @Override
    public void setGhostCellValues(Face face) {
        // velocity is linearly extrapolated, pressure and volume-fraction is simply copied
        double[] insideConservativeVars = face.left.U;
        double[] insidePrimitiveVars = govEqn.primitiveVars(insideConservativeVars);
        double pi = insidePrimitiveVars[0];
        double ui = insidePrimitiveVars[1];
        double vi = insidePrimitiveVars[2];
        double wi = insidePrimitiveVars[3];
        double Ci = insidePrimitiveVars[4];
        Vector insideCellVelocity = new Vector(ui, vi, wi);
        Vector ghostCellVelocity = this.wallVelocity.mult(2.0).sub(insideCellVelocity);

        double[] ghostPrimitiveVars = {
                pi, ghostCellVelocity.x, ghostCellVelocity.y, ghostCellVelocity.z, Ci
        };
        double[] ghostConservativeVars = govEqn.conservativeVars(ghostPrimitiveVars);

        DoubleArray.copy(ghostConservativeVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        // u, v, w defined by the velocity of the wall, p and C defined by the inside cell
        double p_rho_beta = face.left.U[0];
        double u = this.wallVelocity.x;
        double v = this.wallVelocity.y;
        double w = this.wallVelocity.z;
        double C = face.left.U[4];

        double rho = govEqn.rho(C);

        double[] conservativeVars = {p_rho_beta, rho * u, rho * v, rho * w, C};

        return govEqn.convection().flux(conservativeVars, face.surface.unitNormal);
    }
}
