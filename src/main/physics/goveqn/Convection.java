package main.physics.goveqn;

import main.geom.Vector;

public interface Convection {
    double[] flux(double[] conservativeVars, Vector unitNormal);

    double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal);

    double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal);
}
