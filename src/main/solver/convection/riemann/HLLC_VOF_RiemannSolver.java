package main.solver.convection.riemann;

import main.geom.Vector;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;

public class HLLC_VOF_RiemannSolver implements RiemannSolver {

    private final ArtificialCompressibilityVOFEquations govEqn;
    private final int numVars;

    public HLLC_VOF_RiemannSolver(ArtificialCompressibilityVOFEquations govEqn) {
        this.govEqn = govEqn;
        this.numVars = govEqn.numVars();
    }

    @Override
    public double[] flux(double[] UL, double[] UR, Face face) {
        Surface surface = face.surface;
        Vector unitNormal = surface.unitNormal();
        double[] eigenvaluesL = govEqn.convection().sortedEigenvalues(UL, unitNormal);
        double[] eigenvaluesR = govEqn.convection().sortedEigenvalues(UR, unitNormal);

        double rho1_minus_rho2 = govEqn.rho1_minus_rho2();

        double SL = Math.min(eigenvaluesL[0], eigenvaluesR[0]);
        double SR = Math.max(eigenvaluesL[4], eigenvaluesR[4]);

        Vector unitTangent1 = surface.unitTangent1();
        Vector unitTangent2 = surface.unitTangent2();

        UL = rotateU(UL, unitNormal, unitTangent1, unitTangent2);
        UR = rotateU(UR, unitNormal, unitTangent1, unitTangent2);

        double[] FL = govEqn.F(UL);
        double[] FR = govEqn.F(UR);

        double CL = UL[4];
        double CR = UR[4];

        double rhoL = govEqn.rho(CL);
        double rhoR = govEqn.rho(CR);

        double SStar = (FL[1] - FR[1] - UL[1] * SL + UR[1] * SR) /
                (rho1_minus_rho2 * (FL[4] - FR[4]) + SR * rhoR - SL * rhoL);

        double CStarL = (CL * SL - FL[4]) / (SL - SStar);
        double CStarR = (CR * SR - FR[4]) / (SR - SStar);

        double rhoStarL = govEqn.rho(CStarL);
        double rhoStarR = govEqn.rho(CStarR);

        double pStar_beta = (FR[0] - FL[0] + SL * UL[0] - SR * UR[0]) /
                ((SL - SStar) / rhoStarL - (SR - SStar) / rhoStarR);

        double[] flux;
        if (SL >= 0.0) {
            flux = FL;
        } else if (SR <= 0.0) {
            flux = FR;
        } else if (SL <= 0 && SStar >= 0) {

            double rho_v_StarL = (UL[2] * SL - FL[2]) / (SL - SStar);
            double rho_w_StarL = (UL[3] * SL - FL[3]) / (SL - SStar);
            double[] UStarL = {
                    pStar_beta / rhoStarL, rhoStarL * SStar, rho_v_StarL, rho_w_StarL, CStarL
            };

            flux = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                flux[var] = FL[var] + SL * (UStarL[var] - UL[var]);
            }
        } else if (SStar <= 0 && SR >= 0) {

            double rho_v_StarR = (UR[2] * SR - FR[2]) / (SR - SStar);
            double rho_w_StarR = (UR[3] * SR - FR[3]) / (SR - SStar);
            double[] UStarR = {
                    pStar_beta / rhoStarR, rhoStarR * SStar, rho_v_StarR, rho_w_StarR, CStarR
            };

            flux = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                flux[var] = FR[var] + SR * (UStarR[var] - UR[var]);
            }
        } else {
            throw new IllegalStateException("The wave speeds are not valid.");
        }

        return rotateBackF(flux, unitNormal, unitTangent1, unitTangent2);
    }

    private double[] rotateU(double[] U, Vector unitNormal, Vector unitTangent1, Vector unitTangent2) {
        double nx = unitNormal.x;
        double ny = unitNormal.y;
        double nz = unitNormal.z;

        double t1x = unitTangent1.x;
        double t1y = unitTangent1.y;
        double t1z = unitTangent1.z;

        double t2x = unitTangent2.x;
        double t2y = unitTangent2.y;
        double t2z = unitTangent2.z;

        return new double[]{
                U[0],
                U[1] * nx + U[2] * ny + U[3] * nz,
                U[1] * t1x + U[2] * t1y + U[3] * t1z,
                U[1] * t2x + U[2] * t2y + U[3] * t2z,
                U[4]
        };
    }

    private double[] rotateBackF(double[] F, Vector unitNormal, Vector unitTangent1, Vector unitTangent2) {
        double nx = unitNormal.x;
        double ny = unitNormal.y;
        double nz = unitNormal.z;

        double t1x = unitTangent1.x;
        double t1y = unitTangent1.y;
        double t1z = unitTangent1.z;

        double t2x = unitTangent2.x;
        double t2y = unitTangent2.y;
        double t2z = unitTangent2.z;

        return new double[]{
                F[0],
                F[1] * nx + F[2] * t1x + F[3] * t2x,
                F[1] * ny + F[2] * t1y + F[3] * t2y,
                F[1] * nz + F[2] * t1z + F[3] * t2z,
                F[4]
        };
    }
}
