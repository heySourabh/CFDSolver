package main.physics.goveqn;

import main.geom.Vector;

public class ZeroDiffusion implements Diffusion {
    @Override
    public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
        return new double[conservativeVars.length];
    }

    @Override
    public double maxAbsDiffusivity(double[] conservativeVars) {
        return 0;
    }
}
