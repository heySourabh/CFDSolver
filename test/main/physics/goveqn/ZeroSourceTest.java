package main.physics.goveqn;

import org.junit.Test;

import static org.junit.Assert.*;

public class ZeroSourceTest {

    @Test
    public void sourceVector_always_returns_zeros() {
        double[] conservativeVars = {1, 8.2, -8.2, 0.01};
        double[] expectedVector = new double[conservativeVars.length];

        ZeroSource zeroSource = new ZeroSource(expectedVector.length);

        assertArrayEquals(expectedVector, zeroSource.sourceVector(conservativeVars), 1e-15);
    }
}
