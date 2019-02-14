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
        return 7;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "C", "u", "v", "w", "ur", "vr", "wr"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "C", "u", "v", "w", "ur", "vr", "wr"
        };
    }

    @Override
    public String[] realVarNames() {
        return new String[]{
                "C", "u", "v", "w", "ur", "vr", "wr"
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
                    0,
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
                    ev0, 0, 0, 0, 0, 0, ev3
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
            double ur = conservativeVars[4];
            double vr = conservativeVars[5];
            double wr = conservativeVars[6];

            return (u + ur) * nx + (v + vr) * ny + (w + wr) * nz;
        }
    };

    @Override
    public Convection convection() {
        return convection;
    }

    private final Diffusion diffusion = new ZeroDiffusion(numVars());

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
