package main.solver;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.Diffusion;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.Source;
import main.solver.convection.riemann.HLLRiemannSolver;
import main.util.TestHelper;
import org.junit.Test;

import java.util.Arrays;

import static main.util.DoubleArray.*;
import static org.junit.Assert.assertArrayEquals;

public class HLLRiemannSolverTest {

    @Test
    public void flux_when_SL_is_positive_is_FL() {
        double[] evs = {
                1, 2, 3
        };
        GoverningEquations govEqn = testGoverningEquation(evs);

        double[] UL = {42.5, 78.2, 98.1};
        double[] UR = {75, 7.21, 8.3};
        Vector unitNormal = new Vector(21, 87, 3).unit();

        double[] expectedFlux = resultantFlux(UL, unitNormal);

        assertArrayEquals(expectedFlux, new HLLRiemannSolver(govEqn).flux(UL, UR, unitNormal), 1e-15);
    }

    @Test
    public void flux_when_SR_is_negative_is_FR() {
        double[] evs = {
                -3, -2, -1
        };
        GoverningEquations govEqn = testGoverningEquation(evs);

        double[] UL = {42.5, 78.2, 98.1};
        double[] UR = {75, 7.21, 8.3};
        Vector unitNormal = new Vector(21, 87, 3).unit();

        double[] expectedFlux = resultantFlux(UR, unitNormal);

        assertArrayEquals(expectedFlux, new HLLRiemannSolver(govEqn).flux(UL, UR, unitNormal), 1e-15);
    }

    @Test
    public void flux_when_SL_is_negative_and_SR_is_positive_is_given_by_HLL_approximation() {
        double[] evs = {
                -3, -2, 1
        };
        GoverningEquations govEqn = testGoverningEquation(evs);

        double[] UL = {42.5, 78.2, 98.1};
        double[] UR = {75, 7.21, 8.3};
        Vector unitNormal = new Vector(21, 87, 3).unit();

        double[] expectedFlux = hll_approximation(UL, UR, resultantFlux(UL, unitNormal), resultantFlux(UR, unitNormal), -3, 1);

        assertArrayEquals(expectedFlux, new HLLRiemannSolver(govEqn).flux(UL, UR, unitNormal), 1e-15);
    }

    @Test
    public void flux_when_wave_speeds_are_invalid() {
        double[] evs = {
                -3, -2, Double.NaN
        };
        GoverningEquations govEqn = testGoverningEquation(evs);

        double[] UL = {42.5, 78.2, 98.1};
        double[] UR = {75, 7.21, 8.3};
        Vector unitNormal = new Vector(21, 87, 3).unit();

        TestHelper.assertThrows(IllegalStateException.class, () -> new HLLRiemannSolver(govEqn).flux(UL, UR, unitNormal));
    }

    private GoverningEquations testGoverningEquation(double[] evs) {
        return new GoverningEquations() {
            @Override
            public String description() {
                return null;
            }

            @Override
            public int numVars() {
                return 3;
            }

            @Override
            public String[] conservativeVarNames() {
                return new String[0];
            }

            @Override
            public String[] primitiveVarNames() {
                return new String[0];
            }

            @Override
            public double[] primitiveVars(double[] conservativeVars) {
                return new double[0];
            }

            @Override
            public double[] conservativeVars(double[] primitiveVars) {
                return new double[0];
            }

            Convection convection = new Convection() {
                @Override
                public double[] flux(double[] U, Vector n) {
                    return resultantFlux(U, n);
                }

                @Override
                public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
                    return evs;
                }

                @Override
                public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
                    return Arrays.stream(sortedEigenvalues(conservativeVars, unitNormal)).max().orElseThrow();
                }
            };

            @Override
            public Convection convection() {
                return convection;
            }

            @Override
            public Diffusion diffusion() {
                return null;
            }

            @Override
            public Source source() {
                return null;
            }
        };
    }

    private double[] F(double[] U) {
        return new double[]{
                5.0 + 3.0 * U[0],
                8.0 + 5.0 * U[1],
                9.0 * U[2],
        };
    }

    private double[] G(double[] U) {
        return new double[]{
                1.0,
                2.0 * U[0] * U[1] * U[2],
                2.0 * U[2] * U[1] * U[1]
        };
    }

    private double[] H(double[] U) {
        return new double[]{
                1.0,
                2.5,
                7.5 * U[2]};
    }

    private double[] resultantFlux(double[] U, Vector n) {
        return add(multiply(F(U), n.x), multiply(G(U), n.y), multiply(H(U), n.z));
    }

    private double[] hll_approximation(double[] UL, double[] UR, double[] FL, double[] FR, double SL, double SR) {
        return divide(add(multiply(FL, SR), multiply(FR, -SL), multiply(subtract(UR, UL), SL * SR)), SR - SL);
    }

    private double[] add(double[]... arrays) {
        int len = arrays[0].length;
        double[] newArray = new double[len];
        for (int i = 0; i < len; i++) {
            for (double[] array : arrays) {
                newArray[i] += array[i];
            }
        }

        return newArray;
    }
}