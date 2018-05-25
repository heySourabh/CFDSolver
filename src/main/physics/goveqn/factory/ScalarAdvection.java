package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.Convection;
import main.physics.goveqn.Diffusion;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.Source;

public class ScalarAdvection implements GoverningEquations {
    private final double ax, ay, az;

    public ScalarAdvection(double ax, double ay, double az) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }

    @Override
    public String description() {
        return "Scalar Advection Equation";
    }

    @Override
    public int numVars() {
        return 1;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "phi"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "phi"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        return new double[]{
                conservativeVars[0]
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        return new double[]{
                primitiveVars[0]
        };
    }

    private final Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double phi = conservativeVars[0];
            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    ax * phi * nx + ay * phi * ny + az * phi * nz
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    ax * nx + ay * ny + az * nz
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;
            double ev = ax * nx + ay * ny + az * nz;

            return Math.abs(ev);
        }
    };

    @Override
    public Convection convection() {
        return convection;
    }

    private final Diffusion diffusion = (conservativeVars, gradConservativeVars, unitNormal) -> new double[1];

    @Override
    public Diffusion diffusion() {
        return diffusion;
    }

    private final Source source = conservativeVars -> new double[1];

    @Override
    public Source source() {
        return source;
    }
}
