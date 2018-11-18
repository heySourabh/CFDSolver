package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class VOFAdvectionEquationsTest {

    @Test
    public void description() {
        assertEquals("Equations for advection of volume fraction in a varying non-divergent velocity field.",
                new VOFAdvectionEquations().description());
    }

    @Test
    public void numVars() {
        assertEquals(4, new VOFAdvectionEquations().numVars());
    }

    @Test
    public void conservativeVarNames() {
        assertArrayEquals(new String[]{"u", "v", "w", "C"},
                new VOFAdvectionEquations().conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        assertArrayEquals(new String[]{"u", "v", "w", "C"},
                new VOFAdvectionEquations().primitiveVarNames());
    }

    @Test
    public void primitiveVars() {
        double C = 0.5;
        double u = -78.4;
        double v = -12;
        double w = -98;

        double[] conservativeVars = {
                C, u, v, w
        };

        double[] expectedPrimitiveVars = {
                C, u, v, w
        };

        assertArrayEquals(expectedPrimitiveVars,
                new VOFAdvectionEquations().primitiveVars(conservativeVars), 1e-15);
    }

    @Test
    public void conservativeVars() {
        double C = 0.9;
        double u = 8;
        double v = 2;
        double w = 8;

        double[] primitiveVars = {
                C, u, v, w
        };

        double[] expectedConservativeVars = {
                C, u, v, w
        };

        assertArrayEquals(expectedConservativeVars,
                new VOFAdvectionEquations().conservativeVars(primitiveVars), 1e-15);
    }

    @Test
    public void convection() {
        double u = -89;
        double v = 78.7;
        double w = -9;
        double C = 0.1;

        double[] conservativeVars = {
                u, v, w, C
        };

        Vector velocity = new Vector(u, v, w);
        Vector normal = new Vector(1, 6, 8).unit();

        double[] eigenvalues = {
                0, 0, 0, velocity.dot(normal)
        };

        double maxAbsEigenvalue = Math.abs(velocity.dot(normal));

        double[] flux = {
                0, 0, 0, C * velocity.dot(normal)
        };

        Convection convection = new VOFAdvectionEquations().convection();

        assertArrayEquals(flux, convection.flux(conservativeVars, normal), 1e-15);

        assertArrayEquals(eigenvalues, convection.sortedEigenvalues(conservativeVars, normal), 1e-15);

        assertEquals(maxAbsEigenvalue, convection.maxAbsEigenvalues(conservativeVars, normal), 1e-15);
    }

    @Test
    public void diffusion() {
        assertArrayEquals(new double[4],
                new VOFAdvectionEquations().diffusion()
                        .flux(null, null, null), 1e-15);
        assertEquals(0,
                new VOFAdvectionEquations().diffusion().maxAbsDiffusivity(null), 1e-15);
    }

    @Test
    public void source() {
        assertArrayEquals(new double[4],
                new VOFAdvectionEquations().source()
                        .sourceVector(null, null), 1e-15);
    }
}