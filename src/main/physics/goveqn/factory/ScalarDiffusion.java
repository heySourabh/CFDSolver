package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.*;

public class ScalarDiffusion implements GoverningEquations {

    private final double diffusivity;

    public ScalarDiffusion(double diffusivity) {
        this.diffusivity = diffusivity;
    }

    @Override
    public String description() {
        return "Scalar Diffusion Equation";
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

    private final Convection convection = new ZeroConvection(numVars());

    @Override
    public Convection convection() {
        return convection;
    }

    private final Diffusion diffusion = new Diffusion() {
        @Override
        public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
            double dphi_dx = gradConservativeVars[0].x;
            double dphi_dy = gradConservativeVars[0].y;
            double dphi_dz = gradConservativeVars[0].z;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    diffusivity * (dphi_dx * nx + dphi_dy * ny + dphi_dz * nz)
            };
        }

        @Override
        public double maxAbsDiffusivity(double[] conservativeVars) {
            return diffusivity;
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
