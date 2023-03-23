package main.physics.goveqn;

import main.geom.Point;
import main.geom.Vector;

public interface Source {
    double[] sourceVector(Point at, double[] conservativeVars, Vector[] gradConservativeVars);
}
