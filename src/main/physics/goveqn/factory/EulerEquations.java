package main.physics.goveqn.factory;

import main.geom.Vector;
import main.physics.goveqn.*;

public class EulerEquations implements GoverningEquations {

    public final double GAMMA, R, CV;

    public EulerEquations(double gamma, double R) {
        this.GAMMA = gamma;
        this.R = R;
        this.CV = R / (gamma - 1.0);
    }

    private double eos_p(double rho, double T) {
        return rho * R * T;
    }

    private double eos_T(double rho, double p) {
        return p / rho / R;
    }

    public double mach(double[] primitiveVars, Vector unitDirection) {
        double rho = primitiveVars[0];
        double u = primitiveVars[1];
        double v = primitiveVars[2];
        double w = primitiveVars[3];
        double p = primitiveVars[4];

        double flowSpeed = new Vector(u, v, w).dot(unitDirection);
        double soundSpeed = Math.sqrt(GAMMA * p / rho);

        return flowSpeed / soundSpeed;
    }

    @Override
    public String description() {
        return "Euler Equations (Inviscid fluid flow governing equations)";
    }

    @Override
    public int numVars() {
        return 5;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "rho", "rho u", "rho v", "rho w", "rho E"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "rho", "u", "v", "w", "p"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        double rho = conservativeVars[0];
        double rhou = conservativeVars[1];
        double rhov = conservativeVars[2];
        double rhow = conservativeVars[3];
        double rhoE = conservativeVars[4];

        double u = rhou / rho;
        double v = rhov / rho;
        double w = rhow / rho;
        double E = rhoE / rho;

        double kineticE = 0.5 * (u * u + v * v + w * w);
        double internalE = E - kineticE;

        double T = internalE / CV;
        double p = eos_p(rho, T);

        return new double[]{
                rho, u, v, w, p
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        double rho = primitiveVars[0];
        double u = primitiveVars[1];
        double v = primitiveVars[2];
        double w = primitiveVars[3];
        double p = primitiveVars[4];

        double rhou = rho * u;
        double rhov = rho * v;
        double rhow = rho * w;

        double T = eos_T(rho, p);
        double internalE = CV * T;
        double kineticE = 0.5 * (u * u + v * v + w * w);
        double E = internalE + kineticE;

        double rhoE = rho * E;

        return new double[]{
                rho, rhou, rhov, rhow, rhoE
        };
    }

    private final Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            double rho = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];
            double p = primitiveVars[4];

            double rhoE = conservativeVars[4];
            double rhoE_plus_p = rhoE + p;

            double[] F = {
                    rho * u,
                    rho * u * u + p,
                    rho * u * v,
                    rho * u * w,
                    rhoE_plus_p * u
            };

            double[] G = {
                    rho * v,
                    rho * u * v,
                    rho * v * v + p,
                    rho * v * w,
                    rhoE_plus_p * v
            };

            double[] H = {
                    rho * w,
                    rho * u * w,
                    rho * v * w,
                    rho * w * w + p,
                    rhoE_plus_p * w
            };

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;
            double[] flux = new double[F.length];
            for (int i = 0; i < F.length; i++) {
                flux[i] = F[i] * nx + G[i] * ny + H[i] * nz;
            }

            return flux;
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            double rho = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];
            double p = primitiveVars[4];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;
            double vn = u * nx + v * ny + w * nz;

            double a = Math.sqrt(GAMMA * p / rho);

            return new double[]{
                    vn - a, vn, vn, vn, vn + a
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            double rho = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];
            double p = primitiveVars[4];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;
            double vn = u * nx + v * ny + w * nz;

            double a = Math.sqrt(GAMMA * p / rho);

            return Math.abs(vn) + a;
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
