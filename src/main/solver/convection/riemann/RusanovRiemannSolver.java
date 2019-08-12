package main.solver.convection.riemann;

import main.geom.Vector;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.Convection;
import main.physics.goveqn.GoverningEquations;

public class RusanovRiemannSolver implements RiemannSolver {
    private final GoverningEquations govEqn;

    public RusanovRiemannSolver(GoverningEquations govEqn) {
        this.govEqn = govEqn;
    }

    @Override
    public double[] flux(double[] UL, double[] UR, Face face) {
        Surface surface = face.surface;
        Vector unitNormal = surface.unitNormal();
        Convection convection = govEqn.convection();
        double[] FL = convection.flux(UL, unitNormal);
        double[] FR = convection.flux(UR, unitNormal);

        double maxAbsEigenvalueL = convection.maxAbsEigenvalues(UL, unitNormal);
        double maxAbsEigenvalueR = convection.maxAbsEigenvalues(UR, unitNormal);
        double ev = Math.max(maxAbsEigenvalueL, maxAbsEigenvalueR);

        int numVars = govEqn.numVars();
        double[] flux = new double[numVars];
        for (int i = 0; i < numVars; i++) {
            flux[i] = 0.5 * (FL[i] + FR[i] - ev * (UR[i] - UL[i]));
        }

        return flux;
    }
}
