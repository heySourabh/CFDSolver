package main.physics.goveqn;

import main.geom.Point;
import main.geom.Vector;

public class ZeroSource implements Source {
    private final double[] zeroVector;

    public ZeroSource(int numVars) {
        zeroVector = new double[numVars];
    }

    @Override
    public double[] sourceVector(Point at, double[] conservativeVars, Vector[] gradConservativeVars) {
        return zeroVector;
    }
}
