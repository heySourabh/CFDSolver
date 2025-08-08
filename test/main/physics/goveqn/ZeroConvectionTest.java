package main.physics.goveqn;

import main.geom.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZeroConvectionTest {

    @Test
    public void flux_always_returns_zeros() {
        double[] conservativeVars = {2.8, -9.7, 8.2, 7.8, 3.47};
        Vector unitNormal = new Vector(-5.9, -3, -0).unit();

        assertArrayEquals(new double[]{0, 0, 0, 0, 0},
                new ZeroConvection(5).flux(conservativeVars, unitNormal),
                1e-15);
    }

    @Test
    public void sortedEigenvalues_always_returns_zeros() {
        double[] conservativeVars = {8.2, 7.8, 3.47};
        Vector unitNormal = new Vector(-5.9, -3, -0).unit();

        assertArrayEquals(new double[]{0, 0, 0},
                new ZeroConvection(3).sortedEigenvalues(conservativeVars, unitNormal),
                1e-15);
    }

    @Test
    public void maxAbsEigenvalues_always_returns_zero() {
        double[] conservativeVars = {8.2, 7.8, 3.47};
        Vector unitNormal = new Vector(-5.9, -3, -0).unit();

        assertEquals(0, new ZeroConvection(3).maxAbsEigenvalues(conservativeVars, unitNormal),
                1e-15);
    }
}
