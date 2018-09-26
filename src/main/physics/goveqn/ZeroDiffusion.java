package main.physics.goveqn;

import main.geom.Vector;

public class ZeroDiffusion implements Diffusion {
    private final double[] zeroFlux;

    public ZeroDiffusion(int numVars) {
        zeroFlux = new double[numVars];
    }

    @Override
    public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
        return zeroFlux;
    }

    @Override
    public double maxAbsDiffusivity(double[] conservativeVars) {
        return 0;
    }
}
