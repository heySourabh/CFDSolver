package main.physics.goveqn;

import main.physics.goveqn.factory.EulerEquations;
import org.junit.Test;

import static org.junit.Assert.*;

public class GoverningEquationsTest {

    @Test
    public void test_default_behavior_of_realVarNames() {
        EulerEquations govEqn = new EulerEquations(1.4);
        assertArrayEquals(govEqn.conservativeVarNames(), govEqn.realVarNames());
    }

    @Test
    public void test_default_behavior_of_realVars() {
        EulerEquations govEqn = new EulerEquations(1.4);
        double[] conservativeVars = {1, 2, 3, 4, 5};
        assertArrayEquals(conservativeVars, govEqn.realVars(conservativeVars), 1e-15);
    }
}