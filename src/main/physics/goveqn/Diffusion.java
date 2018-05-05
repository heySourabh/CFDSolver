package main.physics.goveqn;

import main.geom.Vector;

public interface Diffusion {
    double[] flux(double[] conservativeVars, double gradConservativeVars, Vector unitNormal);
}
