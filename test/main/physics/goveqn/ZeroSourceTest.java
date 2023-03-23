package main.physics.goveqn;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ZeroSourceTest {

    @Test
    public void sourceVector_always_returns_zeros() {
        double[] expectedVector = new double[4];
        ZeroSource zeroSource = new ZeroSource(expectedVector.length);
        assertArrayEquals(expectedVector, zeroSource.sourceVector(null, null, null), 1e-15);
    }
}
