package main.physics.goveqn.factory;

import main.geom.Point;
import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.Diffusion;
import main.physics.goveqn.Limits;
import main.physics.goveqn.Source;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PoissonEquationTest {

    @Test
    public void test_description() {
        assertEquals("Poisson Equation", new PoissonEquation(0, null).description());
    }

    @Test
    public void test_numVars() {
        assertEquals(1, new PoissonEquation(0, null).numVars());
    }

    @Test
    public void test_conservativeVarNames() {
        PoissonEquation govEqn = new PoissonEquation(0, null);
        assertArrayEquals(new String[]{"phi"}, govEqn.conservativeVarNames());

        govEqn.setVariableName("T");
        assertArrayEquals(new String[]{"T"}, govEqn.conservativeVarNames());
    }

    @Test
    public void test_primitiveVarNames() {
        PoissonEquation govEqn = new PoissonEquation(0, null);
        assertArrayEquals(new String[]{"phi"}, govEqn.primitiveVarNames());

        govEqn.setVariableName("T");
        assertArrayEquals(new String[]{"T"}, govEqn.primitiveVarNames());
    }

    @Test
    public void test_primitiveVars() {
        PoissonEquation govEqn = new PoissonEquation(0, null);
        double[] conservativeVars = {123.454};
        assertArrayEquals(new double[]{123.454}, govEqn.primitiveVars(conservativeVars), 1e-15);
    }

    @Test
    public void test_conservativeVars() {
        PoissonEquation govEqn = new PoissonEquation(0, null);
        double[] primitiveVars = {245.78};
        assertArrayEquals(new double[]{245.78}, govEqn.conservativeVars(primitiveVars), 1e-15);
    }

    @Test
    public void test_physicalLimits() {
        PoissonEquation govEqn = new PoissonEquation(0, null);
        assertArrayEquals(new Limits[]{Limits.INFINITE}, govEqn.physicalLimits());
    }

    @Test
    public void test_convection() {
        PoissonEquation govEqn = new PoissonEquation(1, null);
        Convection convection = govEqn.convection();
        assertArrayEquals(new double[]{0.0}, convection.flux(null, null), 1e-15);
        assertArrayEquals(new double[]{0.0}, convection.sortedEigenvalues(null, null), 1e-15);
        assertEquals(0.0, convection.maxAbsEigenvalues(null, null), 1e-15);
    }

    @Test
    public void test_diffusion() {
        double diffusivity = 1.578;
        PoissonEquation govEqn = new PoissonEquation(diffusivity, null);
        Diffusion diffusion = govEqn.diffusion();
        double[] conservativeVars = {1.5};
        Vector[] gradU = {new Vector(2.5, 1.2, 3.6)};
        Vector faceNormal = new Vector(1.2, 5.4, 6.8).unit();
        double expectedFlux = diffusivity * (
                gradU[0].x * faceNormal.x +
                gradU[0].y * faceNormal.y +
                gradU[0].z * faceNormal.z);
        assertArrayEquals(new double[]{expectedFlux},
                diffusion.flux(conservativeVars, gradU, faceNormal), 1e-15);

        assertEquals(diffusivity, diffusion.maxAbsDiffusivity(null), 1e-15);
    }

    @Test
    public void test_source() {
        Source testSource = (at, conservativeVars, gradConservativeVars) ->
                new double[]{at.x + at.y + at.z + conservativeVars[0] +
                             gradConservativeVars[0].x +
                             gradConservativeVars[0].y +
                             gradConservativeVars[0].z};
        PoissonEquation govEqn = new PoissonEquation(12.565, testSource);

        Source source = govEqn.source();
        Point at = new Point(1, 3.6, 7.9);
        double[] conservativeVars = {5.3};
        Vector[] gradU = {new Vector(4.8, -9.8, -7.4)};
        assertArrayEquals(new double[]{
                1 + 3.6 + 7.9 + 5.3 + 4.8 - 9.8 - 7.4
        }, source.sourceVector(at, conservativeVars, gradU), 1e-15);
    }
}