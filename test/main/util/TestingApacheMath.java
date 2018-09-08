package main.util;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.junit.Test;

public class TestingApacheMath {
    @Test
    public void obtaining_least_square_solution() {
        double[][] A = {
                {-1.5, -1, -2},
                {1.5, 1, 2}
        };
        double[] b = {
                7, 8
        };

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        System.out.println(svd.getSolver().solve(new ArrayRealVector(b)));
    }
}
