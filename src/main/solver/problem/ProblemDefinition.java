package main.solver.problem;

import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.solver.Config;
import main.solver.SolutionInitializer;
import main.solver.time.TimeIntegrator;

public interface ProblemDefinition {
    String description();

    GoverningEquations govEqn();

    Mesh mesh();

    SolutionInitializer solutionInitializer();

    TimeIntegrator timeIntegrator();

    Config config();
}
