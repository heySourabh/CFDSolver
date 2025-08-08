package main.util;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.junit.jupiter.api.Test;

import static main.util.DoubleArray.dot;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ApacheMathTest {
    @Test
    public void obtaining_least_square_solution() {
        double[][] A = {
                {-1.5, 0, 0},
                {1.5, 0, 0}
        };
        double[] b = {
                7, 8
        };

        double[] Ae = {-1.5, 1.5};
        double[] expectedSolution = new double[]{dot(Ae, b) / dot(Ae, Ae), 0, 0};

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        assertArrayEquals(expectedSolution, svd.getSolver().solve(new ArrayRealVector(b)).toArray(), 1e-12);
    }
}
