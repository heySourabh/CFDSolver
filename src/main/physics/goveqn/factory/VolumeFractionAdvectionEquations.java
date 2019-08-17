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
                "C", "u", "v", "w", "inx", "iny", "inz"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "C", "u", "v", "w", "inx", "iny", "inz"
        };
    }

    @Override
    public String[] realVarNames() {
        return new String[]{
                "C", "u", "v", "w", "inx", "iny", "inz"
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

    private final Limits[] physicalLimits = new Limits[]{
            new Limits(0, 1),
            Limits.INFINITE,
            Limits.INFINITE,
            Limits.INFINITE,
            new Limits(-1, 1),
            new Limits(-1, 1),
            new Limits(-1, 1)
    };

    @Override
    public Limits[] physicalLimits() {
        return physicalLimits;
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
            double fnx = unitNormal.x;
            double fny = unitNormal.y;
            double fnz = unitNormal.z;

            double C = conservativeVars[0];
            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            Vector V = new Vector(u, v, w);

            double inx = conservativeVars[4];
            double iny = conservativeVars[5];
            double inz = conservativeVars[6];

            Vector unitGradC = new Vector(inx, iny, inz);

            double power = 0.5;
            double Lambda = Math.pow(Math.abs(unitGradC.dot(unitNormal)), power);
            double zeta = 1.2;
            double scalar = Lambda * zeta * Math.abs(V.dot(unitNormal));

            Vector Vr = unitGradC.mult(scalar * (1 - C));

            return (u + Vr.x) * fnx + (v + Vr.y) * fny + (w + Vr.z) * fnz;
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
