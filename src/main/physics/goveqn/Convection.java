package main.physics.goveqn;

import main.geom.Vector;

public interface Convection {
    /**
     * Flux required in finite volume method: Fc nx + Gc ny + Hc nz.
     *
     * @param conservativeVars U
     * @param unitNormal       n
     * @return Fc nx + Gc ny + Hc nz
     */
    double[] flux(double[] conservativeVars, Vector unitNormal);

    /**
     * Sorted eigenvalues of Fc nx + Gc ny + Hc nz.
     * These eigenvalues may or may not be equal to ev(Fc) nx + ev(Gc) ny + ev(Hc) nz.
     *
     * @param conservativeVars U
     * @param unitNormal       n
     * @return Sorted eigenvalues of Fc nx + Gc ny + Hc nz.
     */
    double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal);

    /**
     * This value is same as max(|sortedEigenvalues|).
     * However, it will be efficient to calculate only one eigenvalue using this method.
     *
     * @param conservativeVars U
     * @param unitNormal       n
     * @return maximum of absolute eigenvalues
     */
    double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal);
}
