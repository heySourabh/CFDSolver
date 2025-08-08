package main.physics.goveqn.factory;

import main.geom.Vector;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalarAdvectionTest {

    private static final int rndSeed = new Random().nextInt();

    static {
        System.out.println("Scalar Advection test (Random seed) : " + rndSeed);
    }

    @Test
    public void description() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        assertEquals("Scalar Advection Equation", govEqn.description());
    }

    @Test
    public void numVars() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        assertEquals(1, govEqn.numVars());
    }

    @Test
    public void conservativeVarNames() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        assertArrayEquals(new String[]{"phi"}, govEqn.conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        assertArrayEquals(new String[]{"phi"}, govEqn.primitiveVarNames());
    }

    @Test
    public void primitiveVars() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        double phi = rnd.nextDouble() * rnd.nextInt(100) - 50;

        assertArrayEquals(new double[]{phi}, govEqn.primitiveVars(new double[]{phi}), 1e-15);
    }

    @Test
    public void conservativeVars() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        double phi = rnd.nextDouble() * rnd.nextInt(100) - 50;

        assertArrayEquals(new double[]{phi}, govEqn.conservativeVars(new double[]{phi}), 1e-15);
    }

    @Test
    public void convection() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        double phi = rnd.nextDouble() * rnd.nextInt(100) - 50;

        Vector faceUnitNormal = new Vector(1, 0, 0);
        double[] flux = new double[]{ax * phi};
        double[] EVs = new double[]{ax};
        double maxAbsEV = Math.abs(ax);

        assertArrayEquals(flux, govEqn.convection().flux(new double[]{phi}, faceUnitNormal), 1e-12);
        assertArrayEquals(EVs, govEqn.convection().sortedEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);
        assertEquals(maxAbsEV, govEqn.convection().maxAbsEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);

        faceUnitNormal = new Vector(0, 1, 0);
        flux = new double[]{ay * phi};
        EVs = new double[]{ay};
        maxAbsEV = Math.abs(ay);

        assertArrayEquals(flux, govEqn.convection().flux(new double[]{phi}, faceUnitNormal), 1e-12);
        assertArrayEquals(EVs, govEqn.convection().sortedEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);
        assertEquals(maxAbsEV, govEqn.convection().maxAbsEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);

        faceUnitNormal = new Vector(0, 0, 1);
        flux = new double[]{az * phi};
        EVs = new double[]{az};
        maxAbsEV = Math.abs(az);

        assertArrayEquals(flux, govEqn.convection().flux(new double[]{phi}, faceUnitNormal), 1e-12);
        assertArrayEquals(EVs, govEqn.convection().sortedEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);
        assertEquals(maxAbsEV, govEqn.convection().maxAbsEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);

        faceUnitNormal = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()).unit();
        double nx = faceUnitNormal.x, ny = faceUnitNormal.y, nz = faceUnitNormal.z;
        flux = new double[]{ax * phi * nx + ay * phi * ny + az * phi * nz};
        EVs = new double[]{ax * nx + ay * ny + az * nz};
        maxAbsEV = Math.abs(ax * nx + ay * ny + az * nz);

        assertArrayEquals(flux, govEqn.convection().flux(new double[]{phi}, faceUnitNormal), 1e-12);
        assertArrayEquals(EVs, govEqn.convection().sortedEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);
        assertEquals(maxAbsEV, govEqn.convection().maxAbsEigenvalues(new double[]{phi}, faceUnitNormal), 1e-12);
    }

    @Test
    public void diffusion() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        double phi = rnd.nextDouble() * rnd.nextInt(100) - 50;
        Vector gradPhi = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()).mult(rnd.nextInt(100));
        Vector faceUnitNormal = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()).unit();

        assertArrayEquals(new double[]{0},
                govEqn.diffusion().flux(new double[]{phi}, new Vector[]{gradPhi}, faceUnitNormal), 1e-12);
    }

    @Test
    public void source() {
        Random rnd = new Random(rndSeed);
        double ax = (rnd.nextDouble() - 0.5) * 5;
        double ay = (rnd.nextDouble() - 0.5) * 5;
        double az = (rnd.nextDouble() - 0.5) * 5;
        ScalarAdvection govEqn = new ScalarAdvection(ax, ay, az);

        double phi = rnd.nextDouble() * rnd.nextInt(100) - 50;

        assertArrayEquals(new double[]{0}, govEqn.source().sourceVector(null, new double[]{phi},
                        new Vector[]{
                                new Vector(1, 1, 1)
                        }),
                1e-12);
    }
}