package main.physics.goveqn.factory;

import main.geom.Vector;
import org.junit.Test;

import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EulerEquationsTest {

    private final static int rndSeedEulerEquationsTest = new Random().nextInt();

    static {
        System.out.println("Euler Equations test (Random seed) : " + rndSeedEulerEquationsTest);
    }

    @Test
    public void mach() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        Vector velocity = new Vector(23, 83, 93);
        Vector dir = new Vector(1, 0, 0);

        double p = rnd.nextDouble() * 101325;
        double rho = rnd.nextDouble() * 5;

        double expectedMach = 23.0 / sqrt(gamma * p / rho);
        double actualMach = eulerEquations.mach(new double[]{rho, velocity.x, velocity.y, velocity.z, p}, dir);

        assertEquals(expectedMach, actualMach, 1e-12);

        dir = new Vector(0, 1, 0);
        actualMach = eulerEquations.mach(new double[]{rho, velocity.x, velocity.y, velocity.z, p}, dir);
        expectedMach = 83.0 / sqrt(gamma * p / rho);
        assertEquals(expectedMach, actualMach, 1e-12);

        dir = new Vector(0, 0, 1);
        actualMach = eulerEquations.mach(new double[]{rho, velocity.x, velocity.y, velocity.z, p}, dir);
        expectedMach = 93.0 / sqrt(gamma * p / rho);
        assertEquals(expectedMach, actualMach, 1e-12);

        dir = new Vector(78, 42, 51).unit();
        actualMach = eulerEquations.mach(new double[]{rho, velocity.x, velocity.y, velocity.z, p}, dir);
        Vector projection = new Vector(74.81998277347114, 40.28768303186908, 48.92075796726959);
        expectedMach = projection.mag() / sqrt(gamma * p / rho);
        assertEquals(expectedMach, actualMach, 1e-12);
    }

    @Test
    public void description() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        assertEquals("Euler Equations (Inviscid fluid flow governing equations)", eulerEquations.description());
    }

    @Test
    public void numVars() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        assertEquals(5, eulerEquations.numVars());
    }

    @Test
    public void conservativeVarNames() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        assertArrayEquals(new String[]{"rho", "rho u", "rho v", "rho w", "rho E"},
                eulerEquations.conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        assertArrayEquals(new String[]{"rho", "u", "v", "w", "p"},
                eulerEquations.primitiveVarNames());
    }

    @Test
    public void primitiveVars() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        double rho = rnd.nextDouble() * 5;
        double u = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double v = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double w = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double p = rnd.nextDouble() * 1013250;

        double cv = R / (gamma - 1);
        double T = p / R / rho;
        Vector velocity = new Vector(u, v, w);
        double[] conservativeVars = new double[]{
                rho, rho * u, rho * v, rho * w, rho * (cv * T + velocity.magSqr() / 2.0)
        };

        double expectedPrimVars[] = {rho, u, v, w, p};
        double[] actualPrimitiveVars = eulerEquations.primitiveVars(conservativeVars);

        assertDoubleArrayEquals(expectedPrimVars, actualPrimitiveVars);
    }

    @Test
    public void conservativeVars() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        double rho = rnd.nextDouble() * 5;
        double u = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double v = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double w = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double p = rnd.nextDouble() * 1013250;

        double cv = R / (gamma - 1);
        double T = p / R / rho;
        Vector velocity = new Vector(u, v, w);
        double primitiveVars[] = {rho, u, v, w, p};
        double[] expectedConservativeVars = new double[]{
                rho, rho * u, rho * v, rho * w, rho * (cv * T + velocity.magSqr() / 2.0)
        };

        double[] actualConservativeVars = eulerEquations.conservativeVars(primitiveVars);

        assertDoubleArrayEquals(expectedConservativeVars, actualConservativeVars);
    }

    @Test
    public void convection() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        double rho = rnd.nextDouble() * 5;
        double u = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double v = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double w = rnd.nextDouble() * 50 * (rnd.nextBoolean() ? 1 : -1);
        double p = rnd.nextDouble() * 1013250;

        double cv = R / (gamma - 1);
        double T = p / R / rho;
        Vector velocity = new Vector(u, v, w);
        double E = cv * T + velocity.magSqr() / 2.0;

        double[] conservativeVars = new double[]{
                rho, rho * u, rho * v, rho * w, rho * E
        };

        double[] F = {rho * u, rho * u * u + p, rho * u * v, rho * u * w, u * (rho * E + p)};
        double[] G = {rho * v, rho * v * u, rho * v * v + p, rho * v * w, v * (rho * E + p)};
        double[] H = {rho * w, rho * w * u, rho * w * v, rho * w * w + p, w * (rho * E + p)};
        double a = sqrt(gamma * p / rho);

        Vector unitDir = new Vector(1, 0, 0);
        double[] expectedFlux = F;
        double[] expectedEV = new double[]{u - a, u, u, u, u + a};
        double expectedMaxAbsEV = abs(u) + a;

        assertDoubleArrayEquals(expectedFlux, eulerEquations.convection().flux(conservativeVars, unitDir));
        assertDoubleArrayEquals(expectedEV, eulerEquations.convection().sortedEigenvalues(conservativeVars, unitDir));
        assertEquals(expectedMaxAbsEV, eulerEquations.convection().maxAbsEigenvalues(conservativeVars, unitDir), 1e-8);

        unitDir = new Vector(0, 1, 0);
        expectedFlux = G;
        expectedEV = new double[]{v - a, v, v, v, v + a};
        expectedMaxAbsEV = abs(v) + a;

        assertDoubleArrayEquals(expectedFlux, eulerEquations.convection().flux(conservativeVars, unitDir));
        assertDoubleArrayEquals(expectedEV, eulerEquations.convection().sortedEigenvalues(conservativeVars, unitDir));
        assertEquals(expectedMaxAbsEV, eulerEquations.convection().maxAbsEigenvalues(conservativeVars, unitDir), 1e-8);

        unitDir = new Vector(0, 0, 1);
        expectedFlux = H;
        expectedEV = new double[]{w - a, w, w, w, w + a};
        expectedMaxAbsEV = abs(w) + a;

        assertDoubleArrayEquals(expectedFlux, eulerEquations.convection().flux(conservativeVars, unitDir));
        assertDoubleArrayEquals(expectedEV, eulerEquations.convection().sortedEigenvalues(conservativeVars, unitDir));
        assertEquals(expectedMaxAbsEV, eulerEquations.convection().maxAbsEigenvalues(conservativeVars, unitDir), 1e-8);
    }

    @Test
    public void diffusion() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        double[] consVars = new double[eulerEquations.numVars()];
        Vector[] gradConsVars = new Vector[eulerEquations.numVars()];
        Vector unitNormal = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble());

        assertDoubleArrayEquals(new double[]{0, 0, 0, 0, 0}, eulerEquations.diffusion().flux(consVars, gradConsVars, unitNormal));
    }

    @Test
    public void source() {
        Random rnd = new Random(rndSeedEulerEquationsTest);
        double gamma = 1 + rnd.nextDouble();
        double R = 8314.0 / (10 + rnd.nextDouble() + rnd.nextInt(89));
        EulerEquations eulerEquations = new EulerEquations(gamma, R);

        double[] consVars = new double[eulerEquations.numVars()];

        assertDoubleArrayEquals(new double[]{0, 0, 0, 0, 0},
                eulerEquations.source().sourceVector(consVars,
                        new Vector[]{
                                new Vector(1, 1, 1),
                                new Vector(1, 5, 1),
                                new Vector(-1, 3, 8),
                                new Vector(-1, 4, 8)
                        }));
    }

    private void assertDoubleArrayEquals(double[] expected, double[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            double scale = (-1 < expected[i] && expected[i] < 1) ? 1.0 : Math.abs(expected[i]);
            assertEquals(expected[i] / scale, actual[i] / scale, 1.0e-12);
        }
    }
}