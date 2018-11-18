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
                "u", "v", "w", "C"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "u", "v", "w", "C"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        double u = conservativeVars[0];
        double v = conservativeVars[1];
        double w = conservativeVars[2];
        double C = conservativeVars[3];

        return new double[]{
                u, v, w, C
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        double u = primitiveVars[0];
        double v = primitiveVars[1];
        double w = primitiveVars[2];
        double C = primitiveVars[3];

        return new double[]{
                u, v, w, C
        };
    }

    final private Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double u = conservativeVars[0];
            double v = conservativeVars[1];
            double w = conservativeVars[2];
            double C = conservativeVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double vel = u * nx + v * ny + w * nz;

            return new double[]{
                    0,
                    0,
                    0,
                    C * vel
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double u = conservativeVars[0];
            double v = conservativeVars[1];
            double w = conservativeVars[2];
            //double C = conservativeVars[3];

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
            double u = conservativeVars[0];
            double v = conservativeVars[1];
            double w = conservativeVars[2];
            //double C = conservativeVars[3];

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
