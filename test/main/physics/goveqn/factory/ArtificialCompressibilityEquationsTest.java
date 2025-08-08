package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.GoverningEquations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArtificialCompressibilityEquationsTest {

    @Test
    public void description() {
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(5.0,
                1e-5, new Vector(1, -8, 7));
        assertEquals("Artificial compressibility equations for simulating flows with uniform density and viscosity.", govEqn.description());
    }

    @Test
    public void numVars() {
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(5.0,
                1e-5, new Vector(1, -8, 7));
        assertEquals(4, govEqn.numVars());
    }

    @Test
    public void conservativeVarNames() {
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(5.0,
                1e-5, new Vector(1, -8, 7));
        assertArrayEquals(
                new String[]{
                        "p/beta", "u", "v", "w"
                },
                govEqn.conservativeVarNames()
        );
    }

    @Test
    public void primitiveVarNames() {
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(5.0,
                1e-5, new Vector(1, -8, 7));
        assertArrayEquals(
                new String[]{
                        "p", "u", "v", "w"
                },
                govEqn.primitiveVarNames()
        );
    }

    @Test
    public void primitiveVars() {
        double rho = 5.0;
        double mu = 1e-5;
        Vector gravity = new Vector(1, -8, 7);
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double p = 120;
        double u = 0;
        double v = 1.2;
        double w = -8.9;

        double beta = 1.0;

        double[] conservativeVars = {p / beta, u, v, w};

        assertArrayEquals(new double[]{p, u, v, w}, govEqn.primitiveVars(conservativeVars), 1e-15);
    }

    @Test
    public void conservativeVars() {
        double rho = 5.0;
        double mu = 1e-5;
        Vector gravity = new Vector(1, -8, 7);
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double p = 120;
        double u = 0;
        double v = 1.2;
        double w = -8.9;

        double beta = 1.0;

        double[] conservativeVars = {p / beta, u, v, w};

        assertArrayEquals(conservativeVars, govEqn.conservativeVars(new double[]{p, u, v, w}), 1e-15);
    }

    @Test
    public void convection() {
        double rho = 5.0;
        double mu = 1e-5;
        Vector gravity = new Vector(1, -8, 7);
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double p = 120;
        double u = 0;
        double v = 1.2;
        double w = -8.9;

        double beta = 1.0;

        double[] conservativeVars = {p / beta, u, v, w};

        Vector dir = new Vector(-5, -2, 7).unit();

        double[] F = new double[]{
                u, u * u + p / rho, u * v, u * w
        };

        double[] G = new double[]{
                v, u * v, v * v + p / rho, v * w
        };
        double[] H = new double[]{
                w, u * w, v * w, w * w + p / rho
        };

        double[] expectedFlux = {
                F[0] * dir.x + G[0] * dir.y + H[0] * dir.z,
                F[1] * dir.x + G[1] * dir.y + H[1] * dir.z,
                F[2] * dir.x + G[2] * dir.y + H[2] * dir.z,
                F[3] * dir.x + G[3] * dir.y + H[3] * dir.z
        };

        assertArrayEquals(expectedFlux, govEqn.convection().flux(conservativeVars, dir), 1e-12);

        double vel = u * dir.x + v * dir.y + w * dir.z;
        double a = Math.sqrt(vel * vel + beta / rho);
        assertArrayEquals(new double[]{vel - a, vel, vel, vel + a},
                govEqn.convection().sortedEigenvalues(conservativeVars, dir), 1e-15);

        assertEquals(Math.abs(vel) + a,
                govEqn.convection().maxAbsEigenvalues(conservativeVars, dir), 1e-15);
    }

    @Test
    public void F() {
        double rho = 45;
        double mu = 55;
        Vector gravity = new Vector(-654, 54, 13);

        double p = 90;
        double u = 65;
        double v = -90.67;
        double w = -45.456;

        double beta = 1;

        double[] U = {p / beta, u, v, w};
        double[] expectedF = {
                u, u * u + p / rho, u * v, u * w
        };

        ArtificialCompressibilityEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double[] actualF = govEqn.F(U);

        assertArrayEquals(expectedF, actualF, 1e-15);
    }

    @Test
    public void minMaxEigenvalues() {
        double rho = 45;
        double mu = 55;
        Vector gravity = new Vector(-654, 54, 13);

        double p = 90;
        double u = 65;
        double v = -90.67;
        double w = -45.456;

        double beta = 1;

        double[] U = {p / beta, u, v, w};

        double evMin = u - Math.sqrt(u * u + beta / rho);
        double evMax = u + Math.sqrt(u * u + beta / rho);

        ArtificialCompressibilityEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);

        assertArrayEquals(new double[]{evMin, evMax}, govEqn.min_max_eigenvalues(U), 1e-15);
    }

    @Test
    public void diffusion() {
        double rho = 5.0;
        double mu = 1e-5;
        double nu = mu / rho;
        Vector gravity = new Vector(1, -8, 7);
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double p = 120;
        double u = 0;
        double v = 1.2;
        double w = -8.9;

        double[] conservativeVars = {p / 3.0, u, v, w};
        Vector[] gradConservativeVars = {
                new Vector(45, 5, 54),
                new Vector(23, 9, 3),
                new Vector(-9, 0, 4),
                new Vector(1.2, -68, 8)
        };

        Vector dir = new Vector(-5, -2, 7).unit();

        double du_dx = gradConservativeVars[1].x;
        double du_dy = gradConservativeVars[1].y;
        double du_dz = gradConservativeVars[1].z;
        double dv_dx = gradConservativeVars[2].x;
        double dv_dy = gradConservativeVars[2].y;
        double dv_dz = gradConservativeVars[2].z;
        double dw_dx = gradConservativeVars[3].x;
        double dw_dy = gradConservativeVars[3].y;
        double dw_dz = gradConservativeVars[3].z;

        double tau_xx = nu * 2.0 * du_dx;
        double tau_xy = nu * (du_dy + dv_dx);
        double tau_xz = nu * (du_dz + dw_dx);
        double tau_yy = nu * 2.0 * (dv_dy);
        double tau_yz = nu * (dv_dz + dw_dy);
        double tau_zz = nu * 2.0 * dw_dz;

        double[] F = new double[]{
                0, tau_xx, tau_xy, tau_xz
        };

        double[] G = new double[]{
                0, tau_xy, tau_yy, tau_yz
        };
        double[] H = new double[]{
                0, tau_xz, tau_yz, tau_zz
        };

        double[] expectedFlux = {
                F[0] * dir.x + G[0] * dir.y + H[0] * dir.z,
                F[1] * dir.x + G[1] * dir.y + H[1] * dir.z,
                F[2] * dir.x + G[2] * dir.y + H[2] * dir.z,
                F[3] * dir.x + G[3] * dir.y + H[3] * dir.z
        };

        assertArrayEquals(expectedFlux, govEqn.diffusion().flux(conservativeVars, gradConservativeVars, dir), 1e-15);

        assertEquals(nu, govEqn.diffusion().maxAbsDiffusivity(conservativeVars), 1e-15);
    }

    @Test
    public void source() {
        double rho = 5.0;
        double mu = 1e-5;
        Vector gravity = new Vector(1, -8, 7);
        GoverningEquations govEqn = new ArtificialCompressibilityEquations(rho, mu, gravity);
        double p = 120;
        double u = 0;
        double v = 1.2;
        double w = -8.9;

        double[] conservativeVars = {p / 3.0, u, v, w};
        Vector[] gradConservativeVars = {
                new Vector(45, 5, 54),
                new Vector(23, 9, 3),
                new Vector(-9, 0, 4),
                new Vector(1.2, -68, 8)
        };

        assertArrayEquals(new double[]{0, gravity.x, gravity.y, gravity.z},
                govEqn.source().sourceVector(null, conservativeVars, gradConservativeVars), 1e-15);
    }
}
