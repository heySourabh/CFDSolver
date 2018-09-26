package main.physics.goveqn;

import main.geom.Vector;

public interface Diffusion {
    double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal);

    double maxAbsDiffusivity(double[] conservativeVars);
}
