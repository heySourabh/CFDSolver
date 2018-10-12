package main.solver;

import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;

public interface SolutionInitializer {
    void initialize(Mesh mesh, GoverningEquations govEqn);
}
