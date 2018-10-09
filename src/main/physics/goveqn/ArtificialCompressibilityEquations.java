package main.physics.goveqn;

import main.geom.Vector;

public class ArtificialCompressibilityEquations implements GoverningEquations {
    private final double BETA;
    private final double RHO;
    private final double NU;
    private final Vector GRAVITY;

    public ArtificialCompressibilityEquations(double density, double dynamicViscosity, Vector gravity) {
        this.RHO = density;
        GRAVITY = gravity;
        this.NU = dynamicViscosity / RHO;
        this.BETA = 1.0;
    }

    @Override
    public String description() {
        return "Artificial compressibility equations for simulating " +
                "flows with uniform density and viscosity.";
    }

    @Override
    public int numVars() {
        return 4;
    }

    @Override
    public String[] conservativeVarNames() {
        return new String[]{
                "p/beta", "u", "v", "w"
        };
    }

    @Override
    public String[] primitiveVarNames() {
        return new String[]{
                "p", "u", "v", "w"
        };
    }

    @Override
    public double[] primitiveVars(double[] conservativeVars) {
        double p_beta = conservativeVars[0];
        double u = conservativeVars[1];
        double v = conservativeVars[2];
        double w = conservativeVars[3];

        double p = p_beta * BETA;

        return new double[]{
                p, u, v, w
        };
    }

    @Override
    public double[] conservativeVars(double[] primitiveVars) {
        double p = primitiveVars[0];
        double u = primitiveVars[1];
        double v = primitiveVars[2];
        double w = primitiveVars[3];

        double p_beta = p / BETA;

        return new double[]{
                p_beta, u, v, w
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

            double p_rho = p / RHO;
            double uSqr = u * u;
            double vSqr = v * v;
            double wSqr = w * w;
            double uv = u * v;
            double uw = u * w;
            double vw = v * w;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    u * nx + v * ny + w * nz,
                    (uSqr + p_rho) * nx + uv * ny + uw * nz,
                    uv * nx + (vSqr + p_rho) * ny + vw * nz,
                    uw * nx + vw * ny + (wSqr + p_rho) * nz
            };
        }

        @Override
        public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            //double p = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];

            double beta_rho = BETA / RHO;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double Vp = u * nx + v * ny + w * nz;

            double a = Math.sqrt(Vp * Vp + beta_rho);

            return new double[]{
                    Vp - a, Vp, Vp, Vp + a
            };
        }

        @Override
        public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
            double[] primitiveVars = primitiveVars(conservativeVars);
            //double p = primitiveVars[0];
            double u = primitiveVars[1];
            double v = primitiveVars[2];
            double w = primitiveVars[3];

            double beta_rho = BETA / RHO;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            double Vp = u * nx + v * ny + w * nz;

            double a = Math.sqrt(Vp * Vp + beta_rho);

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
            Vector grad_u = gradConservativeVars[1];
            Vector grad_v = gradConservativeVars[2];
            Vector grad_w = gradConservativeVars[3];

            double du_dx = grad_u.x;
            double du_dy = grad_u.y;
            double du_dz = grad_u.z;

            double dv_dx = grad_v.x;
            double dv_dy = grad_v.y;
            double dv_dz = grad_v.z;

            double dw_dx = grad_w.x;
            double dw_dy = grad_w.y;
            double dw_dz = grad_w.z;

            double tau_xx = 2.0 * NU * du_dx;
            double tau_yy = 2.0 * NU * dv_dy;
            double tau_zz = 2.0 * NU * dw_dz;

            double tau_xy = NU * (du_dy + dv_dx);
            double tau_yx = tau_xy;

            double tau_xz = NU * (du_dz + dw_dx);
            double tau_zx = tau_xz;

            double tau_yz = NU * (dv_dz + dw_dy);
            double tau_zy = tau_yz;

            double nx = unitNormal.x;
            double ny = unitNormal.y;
            double nz = unitNormal.z;

            return new double[]{
                    0.0,
                    tau_xx * nx + tau_yx * ny + tau_zx * nz,
                    tau_xy * nx + tau_yy * ny + tau_zy * nz,
                    tau_xz * nx + tau_yz * ny + tau_zz * nz
            };
        }

        @Override
        public double maxAbsDiffusivity(double[] conservativeVars) {
            return NU;
        }
    };

    @Override
    public Diffusion diffusion() {
        return diffusion;
    }

    private final Source source = new Source() {
        @Override
        public double[] sourceVector(double[] conservativeVars, Vector[] gradConservativeVars) {
            return new double[]{
                    0,
                    GRAVITY.x,
                    GRAVITY.y,
                    GRAVITY.z
            };
        }
    };

    @Override
    public Source source() {
        return source;
    }
}
