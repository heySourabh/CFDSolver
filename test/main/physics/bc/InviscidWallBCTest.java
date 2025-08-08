package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.EulerEquations;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static main.util.DoubleArray.copy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InviscidWallBCTest {

    @Test
    public void setGhostCellValues() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Vector normal = new Vector(1, 1, 1).unit();
        Surface surface = new Surface(1.0, null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        copy(new double[]{1.0, 0.0, 0.0, 0.0, 10.0}, left.U);
        inviscidWallBC.setGhostCellValues(testFace);
        assertEquals(1.0, right.U[0], 1e-8);
        assertEquals(0.0, right.U[1], 1e-8);
        assertEquals(0.0, right.U[2], 1e-8);
        assertEquals(0.0, right.U[3], 1e-8);
        assertEquals(10.0, right.U[4], 1e-8);

        copy(new double[]{5.0, 4.5, 4.5, 4.5, 124.0}, left.U);
        inviscidWallBC.setGhostCellValues(testFace);
        assertEquals(5.0, right.U[0], 1e-8);
        assertEquals(-4.5, right.U[1], 1e-8);
        assertEquals(-4.5, right.U[2], 1e-8);
        assertEquals(-4.5, right.U[3], 1e-8);
        assertEquals(124.0, right.U[4], 1e-8);
    }

    @Test
    public void setGhostCellValues1() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Vector normal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(1.0, null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        copy(new double[]{1.0, 5.0, 8.0, 10.2, 10.0}, left.U);
        inviscidWallBC.setGhostCellValues(testFace);
        assertEquals(1.0, right.U[0], 1e-8);
        assertEquals(-5.0, right.U[1], 1e-8);
        assertEquals(8.0, right.U[2], 1e-8);
        assertEquals(10.2, right.U[3], 1e-8);
        assertEquals(10.0, right.U[4], 1e-8);
    }

    @Test
    public void setGhostCellValues2() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Vector normal = new Vector(0, 1, 0).unit();
        Surface surface = new Surface(1.0, null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        copy(new double[]{1.0, 5.0, 8.0, 10.2, 10.0}, left.U);
        inviscidWallBC.setGhostCellValues(testFace);
        assertEquals(1.0, right.U[0], 1e-8);
        assertEquals(5.0, right.U[1], 1e-8);
        assertEquals(-8.0, right.U[2], 1e-8);
        assertEquals(10.2, right.U[3], 1e-8);
        assertEquals(10.0, right.U[4], 1e-8);
    }

    @Test
    public void setGhostCellValues3() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Vector normal = new Vector(0, 0, -1).unit();
        Surface surface = new Surface(1.0, null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        copy(new double[]{1.0, 5.0, 8.0, 10.2, 10.0}, left.U);
        inviscidWallBC.setGhostCellValues(testFace);
        assertEquals(1.0, right.U[0], 1e-8);
        assertEquals(5.0, right.U[1], 1e-8);
        assertEquals(8.0, right.U[2], 1e-8);
        assertEquals(-10.2, right.U[3], 1e-8);
        assertEquals(10.0, right.U[4], 1e-8);
    }

    @Test
    public void convectiveFlux() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Random rnd = new Random();
        Vector normal = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()).unit();
        Surface surface = new Surface(rnd.nextDouble() * rnd.nextInt(100), null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = rnd.nextDouble();

        Vector vel = new Vector(u, v, w);
        Vector normalVel = normal.mult(vel.dot(normal));
        Vector tgtVel = vel.sub(normalVel);

        double[] primVars = {1.0, tgtVel.x, tgtVel.y, tgtVel.z, 101325};
        double[] consVars = govEqn.conservativeVars(primVars);
        double[] expectedFlux = govEqn.convection().flux(consVars, normal);

        copy(consVars, testFace.left.U);
        double[] actualFlux = inviscidWallBC.convectiveFlux(testFace);

        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux1() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Random rnd = new Random();
        Vector normal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(rnd.nextDouble() * rnd.nextInt(100), null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = rnd.nextDouble();

        Vector vel = new Vector(u, v, w);
        Vector normalVel = normal.mult(vel.dot(normal));
        Vector tgtVel = vel.sub(normalVel);

        double[] primVars = {1.0, tgtVel.x, tgtVel.y, tgtVel.z, 101325};
        double[] consVars = govEqn.conservativeVars(primVars);
        double[] expectedFlux = govEqn.convection().flux(consVars, normal);

        copy(consVars, testFace.left.U);
        double[] actualFlux = inviscidWallBC.convectiveFlux(testFace);

        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux2() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Random rnd = new Random();
        Vector normal = new Vector(0, 1, 0).unit();
        Surface surface = new Surface(rnd.nextDouble() * rnd.nextInt(100), null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = rnd.nextDouble();

        Vector vel = new Vector(u, v, w);
        Vector normalVel = normal.mult(vel.dot(normal));
        Vector tgtVel = vel.sub(normalVel);

        double[] primVars = {1.0, tgtVel.x, tgtVel.y, tgtVel.z, 101325};
        double[] consVars = govEqn.conservativeVars(primVars);
        double[] expectedFlux = govEqn.convection().flux(consVars, normal);

        copy(consVars, testFace.left.U);
        double[] actualFlux = inviscidWallBC.convectiveFlux(testFace);

        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux3() {
        EulerEquations govEqn = new EulerEquations(1.4);
        InviscidWallBC inviscidWallBC = new InviscidWallBC(govEqn);
        Random rnd = new Random();
        Vector normal = new Vector(0, 0, 1).unit();
        Surface surface = new Surface(rnd.nextDouble() * rnd.nextInt(100), null, normal);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = rnd.nextDouble();

        Vector vel = new Vector(u, v, w);
        Vector normalVel = normal.mult(vel.dot(normal));
        Vector tgtVel = vel.sub(normalVel);

        double[] primVars = {1.0, tgtVel.x, tgtVel.y, tgtVel.z, 101325};
        double[] consVars = govEqn.conservativeVars(primVars);
        double[] expectedFlux = govEqn.convection().flux(consVars, normal);

        copy(consVars, testFace.left.U);
        double[] actualFlux = inviscidWallBC.convectiveFlux(testFace);

        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }
}