package main.physics.goveqn;

import main.geom.Vector;

public interface Source {
    double[] sourceVector(double[] conservativeVars, Vector[] gradConservativeVars);
}
