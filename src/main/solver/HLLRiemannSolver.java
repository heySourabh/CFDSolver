package main.solver;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.GoverningEquations;

import java.util.Arrays;

public class HLLRiemannSolver implements RiemannSolver {
    private final int numVars;
    private final int numVars_m1;
    private final Convection convection;

    public HLLRiemannSolver(GoverningEquations govEqn) {
        this.convection = govEqn.convection();
        this.numVars = govEqn.numVars();
        this.numVars_m1 = numVars - 1;
    }

    @Override
    public double[] flux(double[] UL, double[] UR, Vector unitNormal) {
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
                    "UR = " + Arrays.toString(UR));
        }
    }
}
