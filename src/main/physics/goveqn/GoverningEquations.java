package main.physics.goveqn;

public interface GoverningEquations {
    String description();

    int numVars();

    String[] conservativeVarNames();

    String[] primitiveVarNames();

    double[] primitiveVars(double[] conservativeVars);

    double[] conservativeVars(double[] primitiveVars);

    Convection convection();

    Diffusion diffusion();

    Source source();
}
