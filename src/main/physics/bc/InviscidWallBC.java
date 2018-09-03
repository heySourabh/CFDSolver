package main.physics.bc;

import main.geom.Vector;
import main.mesh.Face;
import main.physics.goveqn.factory.EulerEquations;

import static main.util.DoubleArray.copy;

public class InviscidWallBC implements BoundaryCondition {
    private final EulerEquations govEqn;

    public InviscidWallBC(EulerEquations govEqn) {
        this.govEqn = govEqn;
    }

    @Override
    public void setGhostCellValues(Face face) {
        // extrapolate pressure and density
        // mirror velocity vector
        double[] insidePrimVars = govEqn.primitiveVars(face.left.U);
        double rho = insidePrimVars[0];
        double u = insidePrimVars[1];
        double v = insidePrimVars[2];
        double w = insidePrimVars[3];
        double p = insidePrimVars[4];

        Vector insideVelocity = new Vector(u, v, w);
        Vector normalVelocity = face.surface.unitNormal
                .mult(insideVelocity.dot(face.surface.unitNormal));
        Vector tangentVelocity = insideVelocity.sub(normalVelocity);
        Vector mirroredVelocity = normalVelocity
                .mult(-1)
                .add(tangentVelocity);
        double[] ghostCellPrimVars = new double[]{
                rho, mirroredVelocity.x, mirroredVelocity.y, mirroredVelocity.z, p
        };
        double[] ghostCellConsVars = govEqn.conservativeVars(ghostCellPrimVars);

        copy(ghostCellConsVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        Vector n = face.surface.unitNormal;
        double[] insidePrimVars = govEqn.primitiveVars(face.left.U);
        double p = insidePrimVars[4];
        return new double[]{
                0, p * n.x, p * n.y, p * n.z, 0
        };
    }
}
