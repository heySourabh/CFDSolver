package main.solver;

import main.geom.Vector;

public interface RiemannSolver {
    double[] flux(double[] UL, double[] UR, Vector unitNormal);
}
