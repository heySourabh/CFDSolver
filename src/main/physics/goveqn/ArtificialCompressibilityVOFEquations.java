package main.physics.goveqn;

import main.geom.Vector;

public class ArtificialCompressibilityVOFEquations implements GoverningEquations {
    private final double BETA;
    private final double RHO1;
    private final double RHO2;
    private final double RHO1_MINUS_RHO2;
    private final double MU1;
    private final double MU2;
    private final double MU1_MINUS_M2;
    private final Vector GRAVITY;

    public ArtificialCompressibilityVOFEquations(double density1, double dynamicViscosity1,
                                                 double density2, double dynamicViscosity2,
                                                 Vector gravity) {
        this.RHO1 = density1;
        this.RHO2 = density2;
        this.RHO1_MINUS_RHO2 = this.RHO1 - this.RHO2;
        this.MU1 = dynamicViscosity1;
        this.MU2 = dynamicViscosity2;
        this.MU1_MINUS_M2 = this.MU1 - this.MU2;
        GRAVITY = gravity;
        this.BETA = 1.0;
    }

    public double rho(double C) {
        if (C >= 1) return RHO1;
        if (C <= 0) return RHO2;

        return RHO1_MINUS_RHO2 * C + RHO2;
    }

    public double mu(double C) {
        if (C >= 1) return MU1;
        if (C <= 0) return MU2;

        return MU1_MINUS_M2 * C + MU2;
    }

    @Override
    public String description() {
        return "Artificial compressibility equations for simulating " +
                "flows with two fluids having different density and viscosity.";
    }

    @Override
    public int numVars() {
        return 5;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "p/(rho beta)", "rho u", "rho v", "rho w", "C"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "p", "u", "v", "w", "C"
        };
    }

    @Override
    public String[] realVarNames() {
        return new String[]{
                "-", "rho u", "rho v", "rho w", "C"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        double p_rho_beta = conservativeVars[0];
        double rhou = conservativeVars[1];
        double rhov = conservativeVars[2];
        double rhow = conservativeVars[3];
        double C = conservativeVars[4];

        double rho = rho(C);

        double p = p_rho_beta * BETA * rho;
        double u = rhou / rho;
        double v = rhov / rho;
        double w = rhow / rho;

        return new double[]{
                p, u, v, w, C
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        double p = primitiveVars[0];
        double u = primitiveVars[1];
        double v = primitiveVars[2];
        double w = primitiveVars[3];
        double C = primitiveVars[4];

        double rho = rho(C);

        double p_beta_rho = p / BETA / rho;
        double rhou = rho * u;
        double rhov = rho * v;
        double rhow = rho * w;

        return new double[]{
                p_beta_rho, rhou, rhov, rhow, C
        };
    }

    @Override
    public double[] realVars(double[] conservativeVars) {
        // double p_beta_rho = conservativeVars[0];
        double rhou = conservativeVars[1];
        double rhov = conservativeVars[2];
        double rhow = conservativeVars[3];
        double C = conservativeVars[4];
        return new double[]{
                0, rhou, rhov, rhow, C
        };
    }

    private final Convection convection = new Convection() {
        @Override
        public double[] flux(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            double p = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];
            double C = primitiveVars[4];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double rho = rho(C);

            double Vp = u * nx + v * ny + w * nz;
            double rhoVp = rho * Vp;

            return new double[]{
                    Vp,
                    u * rhoVp + p * nx,
                    v * rhoVp + p * ny,
                    w * rhoVp + p * nz,
                    C * Vp
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            //double p = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double Vp = u * nx + v * ny + w * nz;

            double a = Math.sqrt(Vp * Vp + BETA);

            return new double[]{
                    Vp - a, Vp, Vp, Vp, Vp + a
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            //double p = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double Vp = u * nx + v * ny + w * nz;

            double a = Math.sqrt(Vp * Vp + BETA);

            return Math.abs(Vp) + a;
        }
    };

    @Override
    public Convection convection() {
        return convection;
    }

    private final Diffusion diffusion = new Diffusion() {

        @Override
        public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
            double C = conservativeVars[4];
            double rho = rho(C);
            double mu = mu(C);

            double u = conservativeVars[1] / rho;
            double v = conservativeVars[2] / rho;
            double w = conservativeVars[3] / rho;

            double one_by_rho = 1.0 / rho;
            double rho2_minus_rho1_by_rho = -RHO1_MINUS_RHO2 / rho;

            Vector dU4_times_rho_ratio = gradConservativeVars[4].mult(rho2_minus_rho1_by_rho);

            Vector grad_u = gradConservativeVars[1].mult(one_by_rho)
                    .add(dU4_times_rho_ratio.mult(u));
            Vector grad_v = gradConservativeVars[2].mult(one_by_rho)
                    .add(dU4_times_rho_ratio.mult(v));
            Vector grad_w = gradConservativeVars[3].mult(one_by_rho)
                    .add(dU4_times_rho_ratio.mult(w));

            double du_dx = grad_u.x;
            double du_dy = grad_u.y;
            double du_dz = grad_u.z;

            double dv_dx = grad_v.x;
            double dv_dy = grad_v.y;
            double dv_dz = grad_v.z;

            double dw_dx = grad_w.x;
            double dw_dy = grad_w.y;
            double dw_dz = grad_w.z;

            double tau_xx = 2.0 * mu * du_dx;
            double tau_yy = 2.0 * mu * dv_dy;
            double tau_zz = 2.0 * mu * dw_dz;

            double tau_xy = mu * (du_dy + dv_dx);
            double tau_yx = tau_xy;

            double tau_xz = mu * (du_dz + dw_dx);
            double tau_zx = tau_xz;

            double tau_yz = mu * (dv_dz + dw_dy);
            double tau_zy = tau_yz;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    0.0,
                    tau_xx * nx + tau_yx * ny + tau_zx * nz,
                    tau_xy * nx + tau_yy * ny + tau_zy * nz,
                    tau_xz * nx + tau_yz * ny + tau_zz * nz,
                    0.0
            };
        }

        @Override
        public double maxAbsDiffusivity(double[] conservativeVars) {
            double C = conservativeVars[4];
            double mu = mu(C);
            double rho = rho(C);
            return mu / rho;
        }
    };

    @Override
    public Diffusion diffusion() {
        return diffusion;
    }

    private final Source source = new Source() {
        @Override
        public double[] sourceVector(double[] conservativeVars, Vector[] gradConservativeVars) {
            double C = conservativeVars[4];
            double rho = rho(C);
            return new double[]{
                    0,
                    rho * GRAVITY.x,
                    rho * GRAVITY.y,
                    rho * GRAVITY.z,
                    0
            };
        }
    };

    @Override
    public Source source() {
        return source;
    }
}
