package main.physics.goveqn;

import main.geom.Vector;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZeroSourceTest {

    @Test
    public void sourceVector_always_returns_zeros() {
        double[] conservativeVars = {1, 8.2, -8.2, 0.01};
        double[] expectedVector = new double[conservativeVars.length];

        Vector[] gradConservativeVars = {
                new Vector(1, 1, 1),
                new Vector(1, 5, 1),
                new Vector(-1, 3, 8)
        };

        ZeroSource zeroSource = new ZeroSource(expectedVector.length);

        assertArrayEquals(expectedVector, zeroSource.sourceVector(conservativeVars, gradConservativeVars), 1e-15);
    }
}
