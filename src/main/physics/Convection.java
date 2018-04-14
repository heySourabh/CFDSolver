package main.physics;

import main.geom.Vector;

public interface Convection {
    double[] convectiveFlux(double[] conservativeVars, Vector unitNormal);

    double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal);

    double[] maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal);
}
