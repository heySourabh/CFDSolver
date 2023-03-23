package main.physics.goveqn.factory;

import main.geom.Point;
import main.geom.Vector;
import main.physics.goveqn.*;

import java.util.stream.IntStream;

import static main.util.DoubleArray.copyOf;

public class GrainGrowthFanChenEquations implements GoverningEquations {
    private final int NUM_ORIENTATIONS;
    private final double alpha, beta, gamma;
    private final double[] kappa, L;
    private final String[] conservativeVarNames;
    private final Limits[] physicalLimits;

    public GrainGrowthFanChenEquations(int numOrientations,
                                       double alpha, double beta, double gamma,
                                       double[] kappa, double[] L) {
        this.NUM_ORIENTATIONS = numOrientations;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;

        if (kappa.length != NUM_ORIENTATIONS || L.length != NUM_ORIENTATIONS) {
            throw new IllegalArgumentException("Array lengths are not compatible.");
        }
        this.kappa = copyOf(kappa);
        this.L = copyOf(L);

        this.conservativeVarNames = IntStream.range(0, NUM_ORIENTATIONS)
                .mapToObj(i -> "eta_" + (i + 1))
                .toArray(String[]::new);

        this.physicalLimits = IntStream.range(0, NUM_ORIENTATIONS)
                .mapToObj(i -> new Limits(0, 1))
                .toArray(Limits[]::new);
    }


    @Override
    public String description() {
        return "Grain Growth using a Continuum Field Model - Fan & Chen.";
    }

    @Override
    public int numVars() {
        return NUM_ORIENTATIONS;
    }

    @Override
    public String[] conservativeVarNames() {
        return conservativeVarNames;
    }

    @Override
    public String[] primitiveVarNames() {
        return conservativeVarNames;
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
    public Limits[] physicalLimits() {
        return physicalLimits;
    }

    private Convection zeroConvection = null;

    @Override
    public Convection convection() {
        if (zeroConvection == null) {
            zeroConvection = new ZeroConvection(numVars());
        }
        return zeroConvection;
    }

    private final Diffusion diffusion = new Diffusion() {
        @Override
        public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
            double[] diffusionFlux = new double[numVars()];
            for (int i = 0; i < diffusionFlux.length; i++) {
                diffusionFlux[i] = L[i] * kappa[i] * gradConservativeVars[i].dot(unitNormal);
            }

            return diffusionFlux;
        }

        private double maxAbsDiffusivity = 0.0;

        @Override
        public double maxAbsDiffusivity(double[] conservativeVars) {
            if (maxAbsDiffusivity == 0.0) {
                maxAbsDiffusivity = IntStream.range(0, numVars())
                        .mapToDouble(i -> L[i] * kappa[i])
                        .map(Math::abs)
                        .max().orElseThrow();
            }
            return maxAbsDiffusivity;
        }
    };

    @Override
    public Diffusion diffusion() {
        return diffusion;
    }

    private final Source source = new Source() {
        @Override
        public double[] sourceVector(Point at, double[] conservativeVars, Vector[] gradConservativeVars) {
            double[] sourceFlux = new double[numVars()];

            double sum_eta_i_sqr = 0.0;
            for (double eta : conservativeVars) {
                sum_eta_i_sqr += eta * eta;
            }

            for (int i = 0; i < NUM_ORIENTATIONS; i++) {
                double eta_i = conservativeVars[i];
                double eta_i_sqr = eta_i * eta_i;
                double eta_i_cubed = eta_i_sqr * eta_i;
                double sum_eta_j_sqr = sum_eta_i_sqr - eta_i_sqr;

                sourceFlux[i] = -alpha * eta_i + beta * eta_i_cubed
                        + 2.0 * gamma * eta_i * sum_eta_j_sqr;
                sourceFlux[i] *= -L[i];
            }

            return sourceFlux;
        }
    };

    @Override
    public Source source() {
        return source;
    }
}
