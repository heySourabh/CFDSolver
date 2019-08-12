package main.solver.convection.riemann;

import main.mesh.Face;

public interface RiemannSolver {
    double[] flux(double[] UL, double[] UR, Face face);
}
