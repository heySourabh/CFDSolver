package main.solver;

import main.mesh.Face;

public interface RiemannSolver {
    double[] flux(double[] UL, double[] UR, Face face);
}
