package main.physics;

import main.geom.Vector;

public interface Diffusion {
    double[] flux(double[] conservativeVars, double gradConservativeVars, Vector unitNormal);
}
