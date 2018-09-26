package main.physics.goveqn;

import main.geom.Vector;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZeroDiffusionTest {

    @Test
    public void flux_is_always_zero() {
        double[] conservativeVars = {1, 6, 4};
        Vector[] gradients = {
                new Vector(9, 8, 0),
                new Vector(9, -8, -4),
                new Vector(-19, 0, 10)
        };
        Vector unitNormal = new Vector(1, 2, 3).unit();
        double[] actualFlux = new ZeroDiffusion(conservativeVars.length)
                .flux(conservativeVars, gradients, unitNormal);

        assertArrayEquals(new double[conservativeVars.length], actualFlux, 1e-15);
    }

    @Test
    public void maxAbsDiffusivity_is_always_zero() {
        double[] conservativeVars = {1, 6, 4};

        double actualDiffusivity = new ZeroDiffusion(conservativeVars.length)
                .maxAbsDiffusivity(conservativeVars);

        assertEquals(0, actualDiffusivity, 1e-15);
    }
}
