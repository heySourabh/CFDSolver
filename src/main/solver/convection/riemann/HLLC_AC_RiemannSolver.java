package main.solver.convection.riemann;

import main.geom.Vector;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;

import java.util.Arrays;

public class HLLC_AC_RiemannSolver implements RiemannSolver {

    private final ArtificialCompressibilityEquations govEqn;
    private final int numVars;

    public HLLC_AC_RiemannSolver(ArtificialCompressibilityEquations govEqn) {
        this.govEqn = govEqn;
        this.numVars = govEqn.numVars();
    }

    @Override
    public double[] flux(double[] UL, double[] UR, Face face) {
        Surface surface = face.surface;
        Vector n = surface.unitNormal();
        Vector t1 = surface.unitTangent1();
        Vector t2 = surface.unitTangent2();

        UL = rotateU(UL, n, t1, t2);
        UR = rotateU(UR, n, t1, t2);

        double[] FL = govEqn.F(UL);
        double[] FR = govEqn.F(UR);

        double[] eigenvaluesL = govEqn.min_max_eigenvalues(UL);
        double[] eigenvaluesR = govEqn.min_max_eigenvalues(UR);

        double SL = Math.min(eigenvaluesL[0], eigenvaluesR[0]);
        double SR = Math.max(eigenvaluesL[1], eigenvaluesR[1]);

        double SL_minus_SR = SL - SR;

        double SStar = (FR[1] - FL[1] - SR * UR[1] + SL * UL[1]) / SL_minus_SR;

        double[] flux;
        if (SStar >= 0) {
            double p_betaStar = (UR[1] - UL[1] - SR * UR[0] + SL * UL[0]) / SL_minus_SR;
            double SL_minus_SStar = SL - SStar;
            double vStarL = (SL * UL[2] - FL[2]) / SL_minus_SStar;
            double wStarL = (SL * UL[3] - FL[3]) / SL_minus_SStar;
            double[] UStarL = {
                    p_betaStar, SStar, vStarL, wStarL
            };
            flux = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                flux[var] = FL[var] + SL * (UStarL[var] - UL[var]);
            }
        } else if (SStar <= 0) {
            double p_betaStar = (UR[1] - UL[1] - SR * UR[0] + SL * UL[0]) / SL_minus_SR;
            double SR_minus_SStar = SR - SStar;
            double vStarR = (SR * UR[2] - FR[2]) / SR_minus_SStar;
            double wStarR = (SR * UR[3] - FR[3]) / SR_minus_SStar;
            double[] UStarR = {
                    p_betaStar, SStar, vStarR, wStarR
            };
            flux = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                flux[var] = FR[var] + SR * (UStarR[var] - UR[var]);
            }
        } else {
            throw new IllegalStateException("The wave speeds are not valid." +
                    "\nRotated UL = " + Arrays.toString(UL) +
                    "\nRotated UR = " + Arrays.toString(UR));
        }

        return cartesianF(flux, n, t1, t2);
    }

    private double[] rotateU(double[] U, Vector n, Vector t1, Vector t2) {
        return new double[]{
                U[0],
                U[1] * n.x + U[2] * n.y + U[3] * n.z,
                U[1] * t1.x + U[2] * t1.y + U[3] * t1.z,
                U[1] * t2.x + U[2] * t2.y + U[3] * t2.z
        };
    }

    private double[] cartesianF(double[] F, Vector n, Vector t1, Vector t2) {
        return new double[]{
                F[0],
                F[1] * n.x + F[2] * t1.x + F[3] * t2.x,
                F[1] * n.y + F[2] * t1.y + F[3] * t2.y,
                F[1] * n.z + F[2] * t1.z + F[3] * t2.z
        };
    }
}
