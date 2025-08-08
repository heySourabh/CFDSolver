package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.Diffusion;
import main.physics.goveqn.Source;
import main.util.DoubleArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalarDiffusionTest {

    @Test
    public void description() {
        assertEquals("Scalar Diffusion Equation", new ScalarDiffusion(0.5).description());
    }

    @Test
    public void numVars() {
        assertEquals(1, new ScalarDiffusion(2.5).numVars());
    }

    @Test
    public void conservativeVarNames() {
        assertArrayEquals(
                new String[]{"phi"},
                new ScalarDiffusion(3.1).conservativeVarNames()
        );
    }

    @Test
    public void primitiveVarNames() {
        assertArrayEquals(
                new String[]{"phi"},
                new ScalarDiffusion(3.1).primitiveVarNames()
        );
    }

    @Test
    public void primitiveVars() {
        double[] conservativeVars = {5.6};
        double[] expectedPrimitiveVars = DoubleArray.copyOf(conservativeVars);

        assertArrayEquals(expectedPrimitiveVars,
                new ScalarDiffusion(2.781).primitiveVars(conservativeVars),
                1e-15);
    }

    @Test
    public void conservativeVars() {
        double[] primitiveVars = {-8.8};
        double[] expectedConservativeVars = DoubleArray.copyOf(primitiveVars);

        assertArrayEquals(expectedConservativeVars,
                new ScalarDiffusion(84651).conservativeVars(primitiveVars),
                1e-15);
    }

    @Test
    public void convection() {
        Convection convection = new ScalarDiffusion(5.5).convection();

        assertArrayEquals(new double[]{0},
                convection.flux(new double[]{187}, new Vector(1, 2, 5).unit()),
                1e-15);
        assertArrayEquals(new double[]{0},
                convection.sortedEigenvalues(new double[]{1.3}, new Vector(1, 2, 5).unit()),
                1e-15);
        assertEquals(0,
                convection.maxAbsEigenvalues(new double[]{1.3}, new Vector(1, 2, 5).unit()),
                1e-15);
    }

    @Test
    public void diffusion() {
        Diffusion diffusion = new ScalarDiffusion(1.2).diffusion();

        double[] conservativeVars = {26.8};
        Vector[] conservativeVarsGradient = {new Vector(-5.4, 9.8, -7.5)};
        Vector unitNormal = new Vector(1, 2, 5).unit();

        double[] expectedFlux = {1.2 * (-5.4 * unitNormal.x + 9.8 * unitNormal.y + -7.5 * unitNormal.z)};
        assertArrayEquals(expectedFlux,
                diffusion.flux(conservativeVars, conservativeVarsGradient, unitNormal),
                1e-15);
        assertEquals(1.2, diffusion.maxAbsDiffusivity(conservativeVars), 1e-15);
    }

    @Test
    public void source() {
        Source source = new ScalarDiffusion(8.2).source();

        assertArrayEquals(new double[]{0.0}, source.sourceVector(null, new double[]{1.8},
                new Vector[]{
                        new Vector(1, 1, 1)
                }), 1e-15);
    }
}