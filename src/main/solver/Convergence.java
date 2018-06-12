package main.solver;

import static java.util.Arrays.stream;
import static main.util.DoubleArray.subtract;

public class Convergence {
    private final double[] convergenceCriteria;

    public Convergence(double[] convergenceCriteria) {
        this.convergenceCriteria = convergenceCriteria;
    }

    public boolean hasConverged(double[] totalResidual) {
        double[] diff = subtract(totalResidual, convergenceCriteria);

        return stream(diff).allMatch(this::negative);
    }

    private boolean negative(double d) {
        return d < 0;
    }
}
