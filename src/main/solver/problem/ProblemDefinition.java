package main.solver.problem;

import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.solver.Config;
import main.solver.TimeIntegrator;

public interface ProblemDefinition {
    String description();

    GoverningEquations govEqn();

    TimeIntegrator timeIntegrator();

    Mesh mesh();

    Config config();
}
