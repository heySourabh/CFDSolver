package main.physics.goveqn;

import main.geom.Vector;

public class ZeroConvection implements Convection {

    private final double[] flux;
    private final double[] ev;

    public ZeroConvection(int numVars) {
        flux = new double[numVars];
        ev = new double[numVars];
    }

    @Override
    public double[] flux(double[] conservativeVars, Vector unitNormal) {
        return flux;
    }

    @Override
    public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
        return ev;
    }

    @Override
    public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
        return 0;
    }
}
