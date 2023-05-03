package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import org.junit.Test;

import java.util.Random;

import static main.util.DoubleArray.copy;
import static main.util.DoubleArray.random;
import static org.junit.Assert.assertArrayEquals;

public class ExtrapolatedBCTest {
    private static final GoverningEquations govEqn = new EulerEquations(1.4);

    @Test
    public void setGhostCellValues() {
        ExtrapolatedBC extrapolatedBC = new ExtrapolatedBC(govEqn);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face testFace = new Face(null, null, null, left, right, govEqn.numVars());

        double[] expectedU = random(govEqn.numVars(), new Random(457));
        copy(expectedU, left.U);
        extrapolatedBC.setGhostCellValues(testFace);
        assertArrayEquals(expectedU, testFace.right.U, 1e-15);
    }

    @Test
    public void convectiveFlux() {
        Random rnd = new Random(457);
        ExtrapolatedBC extrapolatedBC = new ExtrapolatedBC(govEqn);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        left.setIndex(0);
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Vector unitNormal = new Vector(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()).unit();
        Surface surface = new Surface(rnd.nextDouble(), null, unitNormal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());

        double[] consVars = random(govEqn.numVars(), rnd);
        copy(consVars, left.U);
        double[] actualFlux = extrapolatedBC.convectiveFlux(testFace);
        double[] expectedFlux = govEqn.convection().flux(consVars, unitNormal);

        assertArrayEquals(expectedFlux, actualFlux, 1e-15);
    }
}