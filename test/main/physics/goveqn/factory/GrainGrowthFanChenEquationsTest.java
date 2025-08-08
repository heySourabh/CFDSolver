package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Limits;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrainGrowthFanChenEquationsTest {

    @Test
    public void description() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});
        assertEquals("Grain Growth using a Continuum Field Model - Fan & Chen.", govEqn.description());
    }

    @Test
    public void numVars() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});
        assertEquals(6, govEqn.numVars());
    }

    @Test
    public void conservativeVarNames() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        assertArrayEquals(new String[]{
                "eta_1", "eta_2", "eta_3", "eta_4", "eta_5", "eta_6"
        }, govEqn.conservativeVarNames());
    }

    @Test
    public void primitiveVarNames() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        assertArrayEquals(new String[]{
                "eta_1", "eta_2", "eta_3", "eta_4", "eta_5", "eta_6"
        }, govEqn.primitiveVarNames());
    }

    @Test
    public void primitiveVars() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        double[] U = {
                0.8, 0.7, 1.0, 0.0, 0.1, 0.005
        };

        assertArrayEquals(U, govEqn.primitiveVars(U), 1e-15);
    }

    @Test
    public void conservativeVars() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        double[] V = {
                0.8, 0.7, 1.0, 0.0, 0.1, 0.005
        };

        assertArrayEquals(V, govEqn.conservativeVars(V), 1e-15);
    }

    @Test
    public void physicalLimits() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        assertArrayEquals(new Limits[]{
                new Limits(0, 1),
                new Limits(0, 1),
                new Limits(0, 1),
                new Limits(0, 1),
                new Limits(0, 1),
                new Limits(0, 1)
        }, govEqn.physicalLimits());
    }

    @Test
    public void convection() {
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8,
                new double[]{1.4, 2.3, -8.1, 8, 7, 4.2},
                new double[]{56.7, -92.3, 7.5, -8, -9, 5});

        assertEquals(0.0, govEqn.convection().maxAbsEigenvalues(null, null), 1e-15);
        assertArrayEquals(new double[6], govEqn.convection().flux(null, null), 1e-15);
        assertArrayEquals(new double[6], govEqn.convection().sortedEigenvalues(null, null), 1e-15);
    }

    @Test
    public void diffusion() {
        double[] kappa = new double[]{1.4, 2.3, -8.1, 8, 7, 4.2};
        double[] L = new double[]{56.7, -92.3, 7.5, -8, -9, 5};
        var govEqn = new GrainGrowthFanChenEquations(6,
                1.5, 2.8, 5.8, kappa, L);

        Vector[] gradientsEta = {
                new Vector(0.40, 0.76, 0.95),
                new Vector(0.34, 0.83, 0.43),
                new Vector(0.20, 0.40, 0.12),
                new Vector(0.99, 0.09, 0.30),
                new Vector(0.31, 0.73, 0.57),
                new Vector(0.85, 0.52, 0.59)
        };

        Vector faceNormal = new Vector(0.45, 0.43, 0.38).unit();

        Vector[] gradientsEta_timesKappaL = IntStream.range(0, 6)
                .mapToObj(i -> gradientsEta[i].mult(kappa[i] * L[i]))
                .toArray(Vector[]::new);

        double[] flux = Arrays.stream(gradientsEta_timesKappaL)
                .mapToDouble(v -> v.dot(faceNormal))
                .toArray();

        assertEquals(92.3 * 2.3, govEqn.diffusion().maxAbsDiffusivity(null), 1e-15);
        assertArrayEquals(flux, govEqn.diffusion().flux(null, gradientsEta, faceNormal), 1e-12);
    }

    @Test
    public void source() {
        double[] kappa = new double[]{1.4, 2.3, -8.1, 8, 7, 4.2};
        double[] L = new double[]{56.7, -92.3, 7.5, -8, -9, 5};
        double alpha = 1.5;
        double beta = 2.8;
        double gamma = 5.8;
        var govEqn = new GrainGrowthFanChenEquations(6,
                alpha, beta, gamma, kappa, L);
        double[] U = {
                0.8, 0.7, 1.0, 0.0, 0.1, 0.005
        };

        assertArrayEquals(sourceTerm(U, alpha, beta, gamma, L),
                govEqn.source().sourceVector(null, U, null), 1e-12);
    }

    private double[] sourceTerm(double[] eta, double alpha, double beta, double gamma, double[] L) {
        double[] sourceTerm = new double[eta.length];
        for (int i = 0; i < eta.length; i++) {
            var ii = i;
            sourceTerm[i] = -L[i] * (
                    -alpha * eta[i] + beta * eta[i] * eta[i] * eta[i]
                    + 2 * gamma * eta[i]
                      * IntStream.range(0, eta.length)
                              .filter(j -> j != ii)
                              .mapToDouble(j -> eta[j] * eta[j])
                              .sum()
            );
        }

        return sourceTerm;
    }
}