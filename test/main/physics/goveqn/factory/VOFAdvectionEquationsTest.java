package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class VOFAdvectionEquationsTest {

    @Test
    public void description() {
        assertEquals("Equations for advection of volume fraction in a varying " +
                        "non-divergent velocity field with interface compression.",
                new VOFAdvectionEquations().description());
    }

    @Test
    public void numVars() {
        assertEquals(5, new VOFAdvectionEquations().numVars());
    }

    @Test
    public void conservativeVarNames() {
        assertArrayEquals(new String[]{"u", "v", "w", "C", "alpha"},
                new VOFAdvectionEquations().conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        assertArrayEquals(new String[]{"u", "v", "w", "C", "alpha"},
                new VOFAdvectionEquations().primitiveVarNames());
    }

    @Test
    public void primitiveVars() {
        double u = -78.4;
        double v = -12;
        double w = -98;
        double C = 0.5;
        double alpha = 0.52;

        double[] conservativeVars = {
                u, v, w, C, alpha
        };

        double[] expectedPrimitiveVars = {
                u, v, w, C, alpha
        };

        assertArrayEquals(expectedPrimitiveVars,
                new VOFAdvectionEquations().primitiveVars(conservativeVars), 1e-15);
    }

    @Test
    public void conservativeVars() {
        double u = 8;
        double v = 2;
        double w = 8;
        double C = 0.9;
        double alpha = 0.87;

        double[] primitiveVars = {
                u, v, w, C, alpha
        };

        double[] expectedConservativeVars = {
                u, v, w, C, alpha
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
        double alpha = 0.0;

        double[] conservativeVars = {
                u, v, w, C, alpha
        };

        Vector velocity = new Vector(u, v, w);
        Vector normal = new Vector(1, 6, 8).unit();

        double[] eigenvalues = {
                0, 0, 0, 0, velocity.dot(normal)
        };

        double maxAbsEigenvalue = Math.abs(velocity.dot(normal));

        double[] flux = {
                0, 0, 0, C * velocity.dot(normal), 0
        };

        Convection convection = new VOFAdvectionEquations().convection();

        assertArrayEquals(flux, convection.flux(conservativeVars, normal), 1e-15);

        assertArrayEquals(eigenvalues, convection.sortedEigenvalues(conservativeVars, normal), 1e-15);

        assertEquals(maxAbsEigenvalue, convection.maxAbsEigenvalues(conservativeVars, normal), 1e-15);
    }

    @Test
    public void diffusion() {
        assertArrayEquals(new double[5],
                new VOFAdvectionEquations().diffusion()
                        .flux(null, null, null), 1e-15);
        assertEquals(0,
                new VOFAdvectionEquations().diffusion().maxAbsDiffusivity(null), 1e-15);
    }

    @Test
    public void source() {
        assertArrayEquals(new double[5],
                new VOFAdvectionEquations().source()
                        .sourceVector(null, null), 1e-15);
    }
}
