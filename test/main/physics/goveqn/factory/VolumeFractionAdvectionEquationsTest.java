package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.Diffusion;
import main.physics.goveqn.Source;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class VolumeFractionAdvectionEquationsTest {

    @Test
    public void description() {
        assertEquals("Advection of volume fraction for two-phase flows with defined velocity field.",
                new VolumeFractionAdvectionEquations().description());
    }

    @Test
    public void numVars() {
        assertEquals(7, new VolumeFractionAdvectionEquations().numVars());
    }

    @Test
    public void conservativeVarNames() {
        assertArrayEquals(new String[]{"C", "u", "v", "w", "ur", "vr", "wr"},
                new VolumeFractionAdvectionEquations().conservativeVarNames()
        );
    }

    @Test
    public void primitiveVarNames() {
        assertArrayEquals(new String[]{"C", "u", "v", "w", "ur", "vr", "wr"},
                new VolumeFractionAdvectionEquations().primitiveVarNames()
        );
    }

    @Test
    public void realVarNames() {
        assertArrayEquals(new String[]{"C", "u", "v", "w", "ur", "vr", "wr"},
                new VolumeFractionAdvectionEquations().realVarNames()
        );
    }

    @Test
    public void primitiveVars() {
        double[] conservativeVars = {0.5, -4, 6, -2, 9, -4, -4};
        double[] expectedPrimitiveVars = {0.5, -4, 6, -2, 9, -4, -4};

        assertArrayEquals(expectedPrimitiveVars,
                new VolumeFractionAdvectionEquations().primitiveVars(conservativeVars),
                1e-15);
    }

    @Test
    public void conservativeVars() {
        double[] primitiveVars = {0.7, 8, -200, 1, 9, -4, -4};
        double[] expectedConservativeVars = {0.7, 8, -200, 1, 9, -4, -4};

        assertArrayEquals(expectedConservativeVars,
                new VolumeFractionAdvectionEquations().conservativeVars(primitiveVars),
                1e-15);
    }

    @Test
    public void realVars() {
        double[] conservativeVars = {0.1, 34, 6, -35, 9, -4, -4};
        double[] expectedRealVars = {0.1, 34, 6, -35, 9, -4, -4};

        assertArrayEquals(expectedRealVars,
                new VolumeFractionAdvectionEquations().realVars(conservativeVars),
                1e-15);
    }

    @Test
    public void convection_with_positive_Vn() {
        double C = 0.2;
        double u = 78;
        double v = 9.6;
        double w = -3.67;
        double ur = 2;
        double vr = 4;
        double wr = 5;
        double[] conservativeVars = {C, u, v, w, ur, vr, wr};
        Vector unitNormal = new Vector(4, 6, -1).unit();

        Vector V = new Vector(u, v, w);
        Vector Vr = new Vector(ur, vr, wr);
        double Vn = V.add(Vr).dot(unitNormal);
        double[] expectedFlux = {
                C * Vn, 0, 0, 0, 0, 0, 0
        };

        double[] expectedEigenvalues = {
                0, 0, 0, 0, 0, 0, Vn
        };

        double expectedMaxAbsEigenvalue = Math.abs(Vn);

        Convection convection = new VolumeFractionAdvectionEquations().convection();

        assertArrayEquals(expectedFlux,
                convection.flux(conservativeVars, unitNormal),
                1e-15);
        assertArrayEquals(expectedEigenvalues,
                convection.sortedEigenvalues(conservativeVars, unitNormal),
                1e-15);
        assertEquals(expectedMaxAbsEigenvalue,
                convection.maxAbsEigenvalues(conservativeVars, unitNormal),
                1e-15);
    }

    @Test
    public void convection_with_negative_Vn() {
        double C = 0.2;
        double u = -78;
        double v = 9.6;
        double w = -3.67;
        double ur = 2;
        double vr = 4;
        double wr = 5;
        double[] conservativeVars = {C, u, v, w, ur, vr, wr};
        Vector unitNormal = new Vector(4, 6, -1).unit();

        Vector V = new Vector(u, v, w);
        Vector Vr = new Vector(ur, vr, wr);
        double Vn = V.add(Vr).dot(unitNormal);
        double[] expectedFlux = {
                C * Vn, 0, 0, 0, 0, 0, 0
        };

        double[] expectedEigenvalues = {
                Vn, 0, 0, 0, 0, 0, 0
        };

        double expectedMaxAbsEigenvalue = Math.abs(Vn);

        Convection convection = new VolumeFractionAdvectionEquations().convection();

        assertArrayEquals(expectedFlux,
                convection.flux(conservativeVars, unitNormal),
                1e-15);
        assertArrayEquals(expectedEigenvalues,
                convection.sortedEigenvalues(conservativeVars, unitNormal),
                1e-15);
        assertEquals(expectedMaxAbsEigenvalue,
                convection.maxAbsEigenvalues(conservativeVars, unitNormal),
                1e-15);
    }

    @Test
    public void diffusion() {
        double C = 0.2;
        double u = -78;
        double v = 9.6;
        double w = -3.67;
        double[] conservativeVars = {C, u, v, w};
        Vector[] gradConservativeVars = {null, null, null, null};
        Vector unitNormal = new Vector(4, 6, -1).unit();

        double[] expectedFlux = {0, 0, 0, 0, 0, 0, 0};
        double expectedMaxAbsDiffusivity = 0.0;

        Diffusion diffusion = new VolumeFractionAdvectionEquations().diffusion();

        assertArrayEquals(expectedFlux,
                diffusion.flux(conservativeVars, gradConservativeVars, unitNormal),
                1e-15);
        assertEquals(expectedMaxAbsDiffusivity,
                diffusion.maxAbsDiffusivity(conservativeVars),
                1e-15);
    }

    @Test
    public void source() {
        double C = 0.2;
        double u = -78;
        double v = 9.6;
        double w = -3.67;
        double[] conservativeVars = {C, u, v, w};
        Vector[] gradConservativeVars = {null, null, null, null};

        double[] expectedSource = {0, 0, 0, 0, 0, 0, 0};

        Source source = new VolumeFractionAdvectionEquations().source();
        assertArrayEquals(expectedSource,
                source.sourceVector(conservativeVars, gradConservativeVars),
                1e-15);
    }
}