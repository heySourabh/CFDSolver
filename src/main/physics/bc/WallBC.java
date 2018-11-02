package main.physics.bc;

import main.geom.Vector;
import main.mesh.Face;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.util.DoubleArray;

public class WallBC implements BoundaryCondition {

    private final ArtificialCompressibilityEquations govEqn;
    private final Vector wallVelocity;

    public WallBC(ArtificialCompressibilityEquations govEqn, Vector wallVelocity) {
        this.govEqn = govEqn;
        this.wallVelocity = wallVelocity;
    }

    @Override
    public void setGhostCellValues(Face face) {
        // velocity is linearly extrapolated, pressure is simply copied
        double[] insideConservativeVars = face.left.U;
        double ui = insideConservativeVars[1];
        double vi = insideConservativeVars[2];
        double wi = insideConservativeVars[3];
        Vector insideCellVelocity = new Vector(ui, vi, wi);
        Vector ghostCellVelocity = this.wallVelocity.mult(2.0).sub(insideCellVelocity);

        double[] ghostConservativeVars = {
                insideConservativeVars[0],
                ghostCellVelocity.x,
                ghostCellVelocity.y,
                ghostCellVelocity.z
        };

        DoubleArray.copy(ghostConservativeVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        // u, v, w defined by the velocity of the wall, p defined by the inside cell
        double p_beta = face.left.U[0];
        double u = this.wallVelocity.x;
        double v = this.wallVelocity.y;
        double w = this.wallVelocity.z;

        double[] conservativeVars = {p_beta, u, v, w};

        return govEqn.convection().flux(conservativeVars, face.surface.unitNormal());
    }
}
