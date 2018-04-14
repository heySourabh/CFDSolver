package main.physics;

public interface GoverningEquations {
    String description();

    String[] conservativeVarNames();

    String[] primitiveVarNames();

    double[] primitiveVars(double[] conservativeVars);

    double[] conservativeVars(double[] primitiveVars);

    Convection convection();

    Diffusion diffusion();

    Source source();
}
