package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.*;

import static main.util.DoubleArray.copyOf;

public class VolumeFractionAdvectionEquations implements GoverningEquations {
    @Override
    public String description() {
        return "Advection of volume fraction for two-phase flows with defined velocity field.";
    }

    @Override
    public int numVars() {
        return 4;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "C", "u", "v", "w"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "C", "u", "v", "w"
        };
    }

    @Override
    public String[] realVarNames() {
        return new String[]{
                "C", "u", "v", "w"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        return copyOf(conservativeVars);
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        return copyOf(primitiveVars);
    }

    @Override
    public double[] realVars(double[] conservativeVars) {
        return copyOf(conservativeVars);
    }

    private final Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double C = conservativeVars[0];
            double Vn = Vn(conservativeVars, unitNormal);

            return new double[]{
                    C * Vn,
                    0,
                    0,
                    0
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double Vn = Vn(conservativeVars, unitNormal);
            double ev0;
            double ev3;

            if (Vn < 0) {
                ev0 = Vn;
                ev3 = 0;
            } else {
                ev0 = 0;
                ev3 = Vn;
            }

            return new double[]{
                    ev0, 0, 0, ev3
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double Vn = Vn(conservativeVars, unitNormal);

            return Math.abs(Vn);
        }

        private double Vn(double[] conservativeVars, Vector unitNormal) {
            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            return u * nx + v * ny + w * nz;
        }
    };

    @Override
    public Convection convection() {
        return convection;
    }

    private final Diffusion diffusion = new Diffusion() {
        private final static double EPS = 1e-6;

        @Override
        public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
            double C = conservativeVars[0];
            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            Vector zeroVector = new Vector(0, 0, 0);

            Vector gradC = gradConservativeVars[0];
            double magGradC = gradC.mag();
            Vector unitGradC = magGradC > EPS ? gradC.mult(1.0 / magGradC) : zeroVector;

            Vector V = new Vector(u, v, w);
            double magV = V.mag();
            Vector unitV = magV > EPS ? V.mult(1.0 / magV) : zeroVector;

            double CAlpha = 0.5 * Math.sqrt(Math.abs(unitGradC.dot(unitV)));

            double scalar = -CAlpha * magV;
            Vector Vc = unitGradC.mult(scalar);
            Vector Vr = Vc.mult(C * (1 - C));
            return new double[]{
                    Vr.dot(unitNormal),
                    0,
                    0,
                    0
            };
        }

        @Override
        public double maxAbsDiffusivity(double[] conservativeVars) {
            return 0.25;
        }
    };

    @Override
    public Diffusion diffusion() {
        return diffusion;
    }

    private final Source source = new ZeroSource(numVars());

    @Override
    public Source source() {
        return source;
    }
}
