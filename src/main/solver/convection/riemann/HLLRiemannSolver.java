package main.solver.convection.riemann;

import main.geom.Vector;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.Convection;
import main.physics.goveqn.GoverningEquations;

import java.util.Arrays;

public class HLLRiemannSolver implements RiemannSolver {
    private final int numVars;
    private final GoverningEquations govEqn;
    private final int numVars_m1;
    private final Convection convection;

    public HLLRiemannSolver(GoverningEquations govEqn) {
        this.convection = govEqn.convection();
        this.numVars = govEqn.numVars();
        this.govEqn = govEqn;
        this.numVars_m1 = numVars - 1;
    }

    @Override
    public double[] flux(double[] UL, double[] UR, Face face) {
        Surface surface = face.surface;
        Vector unitNormal = surface.unitNormal();
        double[] leftSideEigenvalues = convection.sortedEigenvalues(UL, unitNormal);
        double[] rightSideEigenvalues = convection.sortedEigenvalues(UR, unitNormal);

        double SL = Math.min(leftSideEigenvalues[0], rightSideEigenvalues[0]);
        double SR = Math.max(leftSideEigenvalues[numVars_m1], rightSideEigenvalues[numVars_m1]);

        double[] FL = convection.flux(UL, unitNormal);
        double[] FR = convection.flux(UR, unitNormal);

        if (SL >= 0.0) {
            return FL;
        } else if (SR <= 0.0) {
            return FR;
        } else if (SL < 0 && SR > 0) {
            double[] flux = new double[numVars];
            double SL_times_SR = SL * SR;
            double SR_minus_SL = SR - SL;
            for (int var = 0; var < numVars; var++) {
                flux[var] = (SR * FL[var] - SL * FR[var] + SL_times_SR * (UR[var] - UL[var])) / SR_minus_SL;
            }
            return flux;
        } else {
            throw new IllegalStateException("The wave speeds are not valid: \n" +
                    "UL = " + Arrays.toString(UL) + ",\n" +
                    "UR = " + Arrays.toString(UR) + "\n" +
                    "VL = " + Arrays.toString(govEqn.primitiveVars(UL)) + ",\n" +
                    "VR = " + Arrays.toString(govEqn.primitiveVars(UR)));
        }
    }
}
