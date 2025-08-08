package main.solver.convection.riemann;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static main.util.DoubleArray.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HLLC_AC_RiemannSolverTest {

    @Test
    public void flux_with_same_states() {
        double[] U = {1.5, -204, -4, 5};
        double[] UL = copyOf(U);
        double[] UR = copyOf(U);

        double rho = 61.7;
        double mu = 545;
        Vector gravity = new Vector(2, 6, -19);
        double beta = 1.0;

        HLLC_AC_RiemannSolver solver
                = new HLLC_AC_RiemannSolver(
                new ArtificialCompressibilityEquations(rho, mu, gravity));

        // x-split
        Vector unitNormal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(2.5, null, unitNormal);
        Cell leftCell = new Cell(null, null, null, UL.length);
        Face face = new Face(null, null, surface, leftCell, null, UL.length);
        double[] actualFlux = solver.flux(UL, UR, face);

        double[] expectedFlux = F(U, rho, beta);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);

        // y-split
        unitNormal = new Vector(0, 1, 0).unit();
        surface = new Surface(2.5, null, unitNormal);
        leftCell = new Cell(null, null, null, UL.length);
        face = new Face(null, null, surface, leftCell, null, UL.length);
        actualFlux = solver.flux(UL, UR, face);

        expectedFlux = G(U, rho, beta);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);

        // z-split
        unitNormal = new Vector(0, 0, 1).unit();
        surface = new Surface(2.5, null, unitNormal);
        leftCell = new Cell(null, null, null, UL.length);
        face = new Face(null, null, surface, leftCell, null, UL.length);
        actualFlux = solver.flux(UL, UR, face);

        expectedFlux = H(U, rho, beta);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);
    }

    @Test
    public void flux_conservation_with_reversed_normal() {
        double rho = 45.5;
        double mu = 12.78;
        Vector gravity = new Vector(-56, 78, 9);

        HLLC_AC_RiemannSolver solver = new HLLC_AC_RiemannSolver(new ArtificialCompressibilityEquations(rho, mu, gravity));

        double[] UL = random(4, new Random(78), -100, 100);
        double[] UR = random(4, new Random(-89), -100, 100);

        Vector normal = new Vector(3, 6, -12).unit();
        Surface surface1 = new Surface(1.2, null, normal);
        Cell leftCell = new Cell(null, null, null, UL.length);
        Face face = new Face(null, null, surface1, leftCell, null, UL.length);
        double[] flux1 = solver.flux(UL, UR, face);

        Vector flippedNormal = normal.mult(-1);
        Surface surface2 = new Surface(1.2, null, flippedNormal);
        leftCell = new Cell(null, null, null, UL.length);
        face = new Face(null, null, surface2, leftCell, null, UL.length);
        double[] flux2 = solver.flux(UR, UL, face);

        assertArrayEquals(flux1, multiply(flux2, -1), 1e-15);
    }

    @Test
    public void flux_resulting_from_StarL_xsplit() {
        double rho = 28.5;
        double mu = 13.76;
        Vector gravity = new Vector(-9.6, 56.4, 23.4);

        double beta = 1.0;

        HLLC_AC_RiemannSolver solver
                = new HLLC_AC_RiemannSolver(
                new ArtificialCompressibilityEquations(rho, mu, gravity));

        double pL = 457;
        double uL = 67;
        double vL = -76.8;
        double wL = 23.7;

        double pR = 2.7;
        double uR = 76;
        double vR = 1;
        double wR = 20.7;

        double[] UL = {pL / beta, uL, vL, wL};
        double[] UR = {pR / beta, uR, vR, wR};

        double SL = Math.min(minEigenvalue(uL, beta, rho), minEigenvalue(uR, beta, rho));
        double SR = Math.max(maxEigenvalue(uL, beta, rho), maxEigenvalue(uR, beta, rho));

        double SStar = ((uR * uR + pR / rho) - (uL * uL + pL / rho) - SR * uR + SL * uL) / (SL - SR);

        assertTrue(SStar > 0);
        double p_betaStar = (uR - uL - SR * pR / beta + SL * pL / beta) / (SL - SR);
        double vStarL = (SL * vL - uL * vL) / (SL - SStar);
        double wStarL = (SL * wL - uL * wL) / (SL - SStar);

        Vector unitNormal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(2.5, null, unitNormal);
        Cell leftCell = new Cell(null, null, null, UL.length);
        Face face = new Face(null, null, surface, leftCell, null, UL.length);
        double[] actualFlux = solver.flux(UL, UR, face);

        double[] FL = F(UL, rho, beta);

        double[] UStarL = new double[]{p_betaStar, SStar, vStarL, wStarL};
        double[] FStarL = add(FL, multiply(subtract(UStarL, UL), SL));

        assertArrayEquals(FStarL, actualFlux, 1e-15);
    }

    @Test
    public void flux_resulting_from_StarR_xsplit() {
        double rho = 28.5;
        double mu = 13.76;
        Vector gravity = new Vector(-9.6, 56.4, 23.4);

        double beta = 1.0;

        HLLC_AC_RiemannSolver solver
                = new HLLC_AC_RiemannSolver(
                new ArtificialCompressibilityEquations(rho, mu, gravity));

        double pL = 457;
        double uL = -67;
        double vL = -76.8;
        double wL = 23.7;

        double pR = 2.7;
        double uR = -76;
        double vR = 1;
        double wR = 20.7;

        double[] UL = {pL / beta, uL, vL, wL};
        double[] UR = {pR / beta, uR, vR, wR};

        double SL = Math.min(minEigenvalue(uL, beta, rho), minEigenvalue(uR, beta, rho));
        double SR = Math.max(maxEigenvalue(uL, beta, rho), maxEigenvalue(uR, beta, rho));

        double SStar = ((uR * uR + pR / rho) - (uL * uL + pL / rho) - SR * uR + SL * uL) / (SL - SR);

        assertTrue(SStar < 0);
        double p_betaStar = (uR - uL - SR * pR / beta + SL * pL / beta) / (SL - SR);
        double vStarR = (SR * vR - uR * vR) / (SR - SStar);
        double wStarR = (SR * wR - uR * wR) / (SR - SStar);

        Vector unitNormal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(2.5, null, unitNormal);
        Cell leftCell = new Cell(null, null, null, UL.length);
        Face face = new Face(null, null, surface, leftCell, null, UL.length);
        double[] actualFlux = solver.flux(UL, UR, face);

        double[] FR = F(UR, rho, beta);

        double[] UStarR = new double[]{p_betaStar, SStar, vStarR, wStarR};
        double[] FStarR = add(FR, multiply(subtract(UStarR, UR), SR));

        assertArrayEquals(FStarR, actualFlux, 1e-15);
    }

    private double[] F(double[] U, double rho, double beta) {
        double p_beta = U[0];
        double u = U[1];
        double v = U[2];
        double w = U[3];

        double p = p_beta * beta;

        return new double[]{
                u, u * u + p / rho, u * v, u * w
        };
    }

    private double[] G(double[] U, double rho, double beta) {
        double p_beta = U[0];
        double u = U[1];
        double v = U[2];
        double w = U[3];

        double p = p_beta * beta;

        return new double[]{
                v, u * v, v * v + p / rho, v * w
        };
    }

    private double[] H(double[] U, double rho, double beta) {
        double p_beta = U[0];
        double u = U[1];
        double v = U[2];
        double w = U[3];

        double p = p_beta * beta;

        return new double[]{
                w, u * w, v * w, w * w + p / rho
        };
    }

    private double minEigenvalue(double vel, double beta, double rho) {
        return vel - Math.sqrt(vel * vel + beta / rho);
    }

    private double maxEigenvalue(double vel, double beta, double rho) {
        return vel + Math.sqrt(vel * vel + beta / rho);
    }
}