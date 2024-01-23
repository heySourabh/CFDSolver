package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.*;

/**
 * The governing equation is given as: <br>
 * ∂phi/∂t = ∇.(k ∇phi) + f <br>
 * where, k is diffusivity (>= 0), and f is a source term.
 */
public class PoissonEquation implements GoverningEquations {

    private String varName = "phi";
    private final double diffusivity;
    private final Source source;

    public PoissonEquation(double diffusivity, Source source) {
        this.diffusivity = diffusivity;
        this.source = source;
    }

    public void setVariableName(String varName) {
        this.varName = varName;
    }

    @Override
    public String description() {
        return "Poisson Equation";
    }

    @Override
    public int numVars() {
        return 1;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                varName
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                varName
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

    private final Limits[] physicalLimits = new Limits[]{
            Limits.INFINITE
    };

    @Override
    public Limits[] physicalLimits() {
        return physicalLimits;
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

    @Override
    public Source source() {
        return source;
    }
}
