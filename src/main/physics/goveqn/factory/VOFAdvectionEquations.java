package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.*;

public class VOFAdvectionEquations implements GoverningEquations {
    @Override
    public String description() {
        return "Equations for advection of volume fraction in a varying non-divergent velocity field.";
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
    public double[] primitiveVars(double[] conservativeVars) {
        double C = conservativeVars[0];
        double u = conservativeVars[1];
        double v = conservativeVars[2];
        double w = conservativeVars[3];

        return new double[]{
                C, u, v, w
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        double C = primitiveVars[0];
        double u = primitiveVars[1];
        double v = primitiveVars[2];
        double w = primitiveVars[3];

        return new double[]{
                C, u, v, w
        };
    }

    final private Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double C = conservativeVars[0];
            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double vel = u * nx + v * ny + w * nz;

            return new double[]{
                    C * vel,
                    0,
                    0,
                    0
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            // double C = conservativeVars[0];
            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double vel = u * nx + v * ny + w * nz;

            return new double[]{
                    0, 0, 0, vel
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            // double C = conservativeVars[0];
            double u = conservativeVars[1];
            double v = conservativeVars[2];
            double w = conservativeVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double vel = u * nx + v * ny + w * nz;

            return Math.abs(vel);
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
