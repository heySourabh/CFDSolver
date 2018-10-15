package main.physics.goveqn;

import main.geom.Vector;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ArtificialCompressibilityVOFEquationsTest {

    @Test
    public void rho() {
        // With rho1 < rho2
        double rho1 = 100;
        double rho2 = 200;

        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                rho1, 0, rho2, 0, new Vector(0, 0, 0));

        double C = 1.0;
        assertEquals(100, govEqn.rho(C), 1e-15);

        C = 0.0;
        assertEquals(200, govEqn.rho(C), 1e-15);

        C = 0.5;
        assertEquals(150, govEqn.rho(C), 1e-15);

        C = 0.25;
        assertEquals(175, govEqn.rho(C), 1e-15);

        C = 1.1;
        assertEquals(100, govEqn.rho(C), 1e-15);

        C = -0.5;
        assertEquals(200, govEqn.rho(C), 1e-15);


        // With rho1 > rho2
        rho1 = 200;
        rho2 = 100;

        govEqn = new ArtificialCompressibilityVOFEquations(
                rho1, 0, rho2, 0, new Vector(0, 0, 0));

        C = 1.0;
        assertEquals(200, govEqn.rho(C), 1e-15);

        C = 0.0;
        assertEquals(100, govEqn.rho(C), 1e-15);

        C = 0.5;
        assertEquals(150, govEqn.rho(C), 1e-15);

        C = 0.25;
        assertEquals(125, govEqn.rho(C), 1e-15);

        C = 1.1;
        assertEquals(200, govEqn.rho(C), 1e-15);

        C = -0.5;
        assertEquals(100, govEqn.rho(C), 1e-15);
    }

    @Test
    public void mu() {
        // With mu1 < mu2
        double mu1 = 1e-2;
        double mu2 = 2e-2;

        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, mu1, 0, mu2, new Vector(0, 0, 0));

        double C = 1.0;
        assertEquals(mu1, govEqn.mu(C), 1e-15);

        C = 0.0;
        assertEquals(mu2, govEqn.mu(C), 1e-15);

        C = 0.5;
        assertEquals(0.015, govEqn.mu(C), 1e-15);

        C = 0.25;
        assertEquals(0.0175, govEqn.mu(C), 1e-15);

        C = 1.1;
        assertEquals(mu1, govEqn.mu(C), 1e-15);

        C = -0.5;
        assertEquals(mu2, govEqn.mu(C), 1e-15);


        // With rho1 > rho2
        mu1 = 2e-2;
        mu2 = 1e-2;

        govEqn = new ArtificialCompressibilityVOFEquations(
                0, mu1, 0, mu2, new Vector(0, 0, 0));

        C = 1.0;
        assertEquals(mu1, govEqn.mu(C), 1e-15);

        C = 0.0;
        assertEquals(mu2, govEqn.mu(C), 1e-15);

        C = 0.5;
        assertEquals(0.015, govEqn.mu(C), 1e-15);

        C = 0.25;
        assertEquals(0.0125, govEqn.mu(C), 1e-15);

        C = 1.1;
        assertEquals(mu1, govEqn.mu(C), 1e-15);

        C = -0.5;
        assertEquals(mu2, govEqn.mu(C), 1e-15);
    }

    @Test
    public void description() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, 0, 0, 0, null);
        assertEquals("Artificial compressibility equations " +
                        "for simulating flows with two fluids having different density and viscosity.",
                govEqn.description());
    }

    @Test
    public void numVars() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, 0, 0, 0, null);
        assertEquals(5, govEqn.numVars());
    }

    @Test
    public void conservativeVarNames() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, 0, 0, 0, null);
        assertArrayEquals(new String[]{
                "p/(rho beta)", "rho u", "rho v", "rho w", "C"
        }, govEqn.conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, 0, 0, 0, null);
        assertArrayEquals(new String[]{
                "p", "u", "v", "w", "C"
        }, govEqn.primitiveVarNames());
    }

    @Test
    public void realVarNames() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                0, 0, 0, 0, null);
        assertArrayEquals(new String[]{
                "-", "rho u", "rho v", "rho w", "C"
        }, govEqn.realVarNames());
    }

    @Test
    public void primitiveVars() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] conservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        assertArrayEquals(new double[]{p, u, v, w, C}, govEqn.primitiveVars(conservativeVars), 1e-15);
    }

    @Test
    public void conservativeVars() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] expectedConservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        assertArrayEquals(expectedConservativeVars, govEqn.conservativeVars(new double[]{p, u, v, w, C}), 1e-15);
    }

    @Test
    public void realVars() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] conservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        double[] expectedRealVars = {
                0, conservativeVars[1], conservativeVars[2], conservativeVars[3], conservativeVars[4]
        };

        assertArrayEquals(expectedRealVars, govEqn.realVars(conservativeVars), 1e-15);
    }

    @Test
    public void convection() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] conservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        Vector n = new Vector(1, -5, 7).unit();

        // flux
        double[] F = {
                u, rho * u * u + p, rho * u * v, rho * u * w, C * u
        };
        double[] G = {
                v, rho * u * v, rho * v * v + p, rho * v * w, C * v
        };
        double[] H = {
                w, rho * u * w, rho * v * w, rho * w * w + p, C * w
        };
        double[] expectedFlux = new double[5];
        for (int var = 0; var < govEqn.numVars(); var++) {
            expectedFlux[var] = F[var] * n.x + G[var] * n.y + H[var] * n.z;
        }
        assertArrayEquals(expectedFlux, govEqn.convection().flux(conservativeVars, n), 1e-8);

        // eigenvalues
        double Vp = u * n.x + v * n.y + w * n.z;
        double a = Math.sqrt(Vp * Vp + beta);
        double[] eigenvalues = {
                Vp - a, Vp, Vp, Vp, Vp + a
        };
        assertArrayEquals(eigenvalues, govEqn.convection().sortedEigenvalues(conservativeVars, n), 1e-15);

        // max abs eigenvalue
        double maxAbsEigenvalue = Arrays.stream(eigenvalues)
                .map(Math::abs)
                .max().orElseThrow();
        assertEquals(maxAbsEigenvalue, govEqn.convection().maxAbsEigenvalues(conservativeVars, n), 1e-15);
    }

    @Test
    public void diffusion() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] conservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        Vector n = new Vector(1, -5, 7).unit();

        Vector[] gradientU = {
                new Vector(8, 9, 6),
                new Vector(81, -9, 2.6),
                new Vector(-6, 9.7, -6.8),
                new Vector(0.7, 8.8, -7.6),
                new Vector(-74, 9, 6),
        };

        double dU0_dx = gradientU[0].x;
        double dU0_dy = gradientU[0].y;
        double dU0_dz = gradientU[0].z;

        double dU1_dx = gradientU[1].x;
        double dU1_dy = gradientU[1].y;
        double dU1_dz = gradientU[1].z;

        double dU2_dx = gradientU[2].x;
        double dU2_dy = gradientU[2].y;
        double dU2_dz = gradientU[2].z;

        double dU3_dx = gradientU[3].x;
        double dU3_dy = gradientU[3].y;
        double dU3_dz = gradientU[3].z;

        double dU4_dx = gradientU[4].x;
        double dU4_dy = gradientU[4].y;
        double dU4_dz = gradientU[4].z;

        double du_dU0 = 0.0;
        double du_dU1 = 1.0 / rho;
        double du_dU2 = 0.0;
        double du_dU3 = 0.0;
        double du_dU4 = -(rho1 - rho2) * u / rho;

        double dv_dU0 = 0.0;
        double dv_dU1 = 0.0;
        double dv_dU2 = 1.0 / rho;
        double dv_dU3 = 0.0;
        double dv_dU4 = -(rho1 - rho2) * v / rho;

        double dw_dU0 = 0.0;
        double dw_dU1 = 0.0;
        double dw_dU2 = 0.0;
        double dw_dU3 = 1.0 / rho;
        double dw_dU4 = -(rho1 - rho2) * w / rho;

        double du_dx = du_dU0 * dU0_dx + du_dU1 * dU1_dx + du_dU2 * dU2_dx + du_dU3 * dU3_dx + du_dU4 * dU4_dx;
        double du_dy = du_dU0 * dU0_dy + du_dU1 * dU1_dy + du_dU2 * dU2_dy + du_dU3 * dU3_dy + du_dU4 * dU4_dy;
        double du_dz = du_dU0 * dU0_dz + du_dU1 * dU1_dz + du_dU2 * dU2_dz + du_dU3 * dU3_dz + du_dU4 * dU4_dz;

        double dv_dx = dv_dU0 * dU0_dx + dv_dU1 * dU1_dx + dv_dU2 * dU2_dx + dv_dU3 * dU3_dx + dv_dU4 * dU4_dx;
        double dv_dy = dv_dU0 * dU0_dy + dv_dU1 * dU1_dy + dv_dU2 * dU2_dy + dv_dU3 * dU3_dy + dv_dU4 * dU4_dy;
        double dv_dz = dv_dU0 * dU0_dz + dv_dU1 * dU1_dz + dv_dU2 * dU2_dz + dv_dU3 * dU3_dz + dv_dU4 * dU4_dz;

        double dw_dx = dw_dU0 * dU0_dx + dw_dU1 * dU1_dx + dw_dU2 * dU2_dx + dw_dU3 * dU3_dx + dw_dU4 * dU4_dx;
        double dw_dy = dw_dU0 * dU0_dy + dw_dU1 * dU1_dy + dw_dU2 * dU2_dy + dw_dU3 * dU3_dy + dw_dU4 * dU4_dy;
        double dw_dz = dw_dU0 * dU0_dz + dw_dU1 * dU1_dz + dw_dU2 * dU2_dz + dw_dU3 * dU3_dz + dw_dU4 * dU4_dz;

        double tau_xx = mu * 2.0 * du_dx;
        double tau_yx = mu * (du_dy + dv_dx);
        double tau_zx = mu * (du_dz + dw_dx);

        double tau_xy = mu * (du_dy + dv_dx);
        double tau_yy = mu * 2.0 * dv_dy;
        double tau_zy = mu * (dv_dz + dw_dy);

        double tau_xz = mu * (du_dz + dw_dx);
        double tau_yz = mu * (dv_dz + dw_dy);
        double tau_zz = mu * 2.0 * dw_dz;

        // flux
        double[] F = {0, tau_xx, tau_xy, tau_xz, 0};
        double[] G = {0, tau_yx, tau_yy, tau_yz, 0};
        double[] H = {0, tau_zx, tau_zy, tau_zz, 0};

        double[] expectedFlux = new double[5];
        for (int var = 0; var < govEqn.numVars(); var++) {
            expectedFlux[var] = F[var] * n.x + G[var] * n.y + H[var] * n.z;
        }
        assertArrayEquals(expectedFlux, govEqn.diffusion().flux(conservativeVars, gradientU, n), 1e-15);

        // max diffusivity
        double expectedMaxDiffusivity = mu / rho;
        assertEquals(expectedMaxDiffusivity, govEqn.diffusion().maxAbsDiffusivity(conservativeVars), 1e-15);
    }

    @Test
    public void source() {
        double p = 100;
        double u = 50;
        double v = -98;
        double w = 1;
        double C = 0.8;

        double rho1 = 450;
        double rho2 = 100;

        double mu1 = 1e-3;
        double mu2 = 2.5e-3;

        double beta = 1.0;

        Vector gravity = new Vector(-1, -5, 3.0);

        ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu1, rho2, mu2, gravity);

        double rho = govEqn.rho(C);
        double mu = govEqn.mu(C);

        double[] conservativeVars = {
                p / beta / rho, rho * u, rho * v, rho * w, C
        };

        Vector n = new Vector(1, -5, 7).unit();

        Vector[] gradientU = {
                new Vector(8, 9, 6),
                new Vector(81, -9, 2.6),
                new Vector(-6, 9.7, -6.8),
                new Vector(0.7, 8.8, -7.6),
                new Vector(-74, 9, 6),
        };

        double[] expectedSource = {
                0, rho * gravity.x, rho * gravity.y, rho * gravity.z, 0
        };
        assertArrayEquals(expectedSource, govEqn.source().sourceVector(conservativeVars, gradientU), 1e-15);
    }
}