package main.solver.convection.riemann;

import main.mesh.Surface;

public interface RiemannSolver {
    double[] flux(double[] UL, double[] UR, Surface surface);
}
