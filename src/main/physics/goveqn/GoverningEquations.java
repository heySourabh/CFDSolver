package main.physics.goveqn;

import static main.util.DoubleArray.copyOf;

public interface GoverningEquations {
    String description();

    int numVars();

    String[] conservativeVarNames();

    String[] primitiveVarNames();

    default String[] realVarNames() {
        return conservativeVarNames();
    }

    double[] primitiveVars(double[] conservativeVars);

    double[] conservativeVars(double[] primitiveVars);

    default double[] realVars(double[] conservativeVars) {
        return copyOf(conservativeVars);
    }

    Convection convection();

    Diffusion diffusion();

    Source source();
}
