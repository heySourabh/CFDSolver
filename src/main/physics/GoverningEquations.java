package main.physics;

import main.geom.Vector;

public interface GoverningEquations {
    String description();

    String[] conservativeVarNames();

    String[] primitiveVarNames();

    double[] primitiveVars(double[] conservativeVars);

    double[] conservativeVars(double[] primitiveVars);

    Convection convection(double[] conservativeVars, Vector unitNormal);
}
