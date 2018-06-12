package main.solver;

import main.mesh.Mesh;

public interface SolutionInitializer {
    void initialize(Mesh mesh);
}
