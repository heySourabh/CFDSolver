package main.util;

import org.junit.Assert;
import org.junit.Test;

import static main.util.DoubleMatrix.column;
import static main.util.DoubleMatrix.multiply;
import static main.util.TestHelper.assertMatrixEquals;

public class QRDecompositionTest {
    @Test
    public void qr_decomposition_of_tall_matrix_example1() {
        double[][] A = {
                {2, 1},
                {2, 1},
                {1, 5}
        };

        QRDecomposition qrDecomposition = new QRDecomposition(A, true);
        double[][] Q = qrDecomposition.Q();
        double[][] R = qrDecomposition.R();
        int[] permutations = qrDecomposition.P();
        double[][] P = createPermutationMatrix(permutations);

        assertMatrixEquals(A, multiply(multiply(Q, R), P), 1e-12);

        assertColumnsOrthonormal(Q, 1e-12);

        assertUpperTriangular(R, 1e-12);
    }

    @Test
    public void qr_decomposition_of_tall_matrix_example2() {
        double[][] A = {
                {1, 2, 3},
                {-1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };

        QRDecomposition qrDecomposition = new QRDecomposition(A, true);
        double[][] Q = qrDecomposition.Q();
        double[][] R = qrDecomposition.R();
        int[] permutations = qrDecomposition.P();
        double[][] P = createPermutationMatrix(permutations);

        assertMatrixEquals(A, multiply(multiply(Q, R), P), 1e-12);

        assertColumnsOrthonormal(Q, 1e-12);

        assertUpperTriangular(R, 1e-12);
    }

    @Test
    public void qr_decomposition_of_tall_matrix_example3() {
        double[][] A = {
                {1, -1, 6},
                {1, 2, 0},
                {1, 3, -1},
                {1, 4, -1}
        };
        double sqrt14 = Math.sqrt(14);
        double sqrt10 = Math.sqrt(10);

        double[][] Q = {
                {0.5, -3.0 / sqrt14, 1.0 / sqrt10},
                {0.5, 0, -2.0 / sqrt10},
                {0.5, 1.0 / sqrt14, -1.0 / sqrt10},
                {0.5, 2.0 / sqrt14, 2.0 / sqrt10}
        };
        double[][] R = {
                {2, 4, 2},
                {0, 14.0 / sqrt14, -21.0 / sqrt14},
                {0, 0, 5.0 / sqrt10}
        };
        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A, false);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }

    @Test
    public void qr_decomposition_of_tall_matrix_example4() {
        double[][] A = {
                {12, 27},
                {4, 2},
                {6, 10}
        };

        double[][] Q = {
                {6.0 / 7.0, 3.0 / 7.0},
                {2.0 / 7.0, -6.0 / 7.0},
                {3.0 / 7.0, -2.0 / 7.0}
        };
        double[][] R = {
                {14, 28},
                {0, 7}
        };
        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A, false);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }

    @Test
    public void checking_properties_of_qr_decomposition_of_tall_matrix() {
        double[][] A = {
                {6, -1, 1},
                {0, 2, 1},
                {-1, 3, 1},
                {-1, 4, 1}
        };

        QRDecomposition qrDecomposition = new QRDecomposition(A, true);
        double[][] Q = qrDecomposition.Q();
        double[][] R = qrDecomposition.R();
        int[] permutations = qrDecomposition.P();
        double[][] P = createPermutationMatrix(permutations);

        assertMatrixEquals(A, multiply(multiply(Q, R), P), 1e-12);

        assertColumnsOrthonormal(Q, 1e-12);

        assertUpperTriangular(R, 1e-12);
    }

    @Test
    public void qr_decomposition_of_square_matrix_example1() {
        double[][] A = {
                {4, 9, -14},
                {3, 13, 2},
                {0, 5, 0}
        };

        QRDecomposition qrDecomposition = new QRDecomposition(A, true);
        double[][] Q = qrDecomposition.Q();
        double[][] R = qrDecomposition.R();
        int[] permutations = qrDecomposition.P();
        double[][] P = createPermutationMatrix(permutations);

        assertMatrixEquals(A, multiply(multiply(Q, R), P), 1e-12);

        assertColumnsOrthonormal(Q, 1e-12);

        assertUpperTriangular(R, 1e-12);
    }

    @Test
    public void qr_decomposition_of_square_matrix_example2() {
        double[][] A = {
                {1, 2, 6},
                {0, 0, 7},
                {0, 5, 12}
        };

        double[][] Q = {
                {1, 0, 0},
                {0, 0, 1},
                {0, 1, 0}
        };

        double[][] R = {
                {1, 2, 6},
                {0, 5, 12},
                {0, 0, 7}
        };

        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A, false);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }

    @Test
    public void checking_properties_of_qr_decomposition_of_square_matrix() {
        double[][] A = {
                {1, 2, 6},
                {0, 0, 7},
                {0, 5, 12}
        };

        QRDecomposition qrDecomposition = new QRDecomposition(A, true);
        double[][] Q = qrDecomposition.Q();
        double[][] R = qrDecomposition.R();
        int[] permutations = qrDecomposition.P();
        double[][] P = createPermutationMatrix(permutations);

        assertMatrixEquals(A, multiply(multiply(Q, R), P), 1e-12);

        assertColumnsOrthonormal(Q, 1e-15);

        assertUpperTriangular(R, 1e-15);
    }

    private double[][] createPermutationMatrix(int[] permutations) {
        double[][] P = new double[permutations.length][permutations.length];
        for (int i = 0; i < P.length; i++) {
            P[i][permutations[i]] = 1.0;
        }

        return P;
    }

    private void assertOrthonormal(double[] v1, double[] v2, double tolerance) {
        // assert normalized
        Assert.assertEquals(1, DoubleArray.norm2Sqr(v1), tolerance);
        Assert.assertEquals(1, DoubleArray.norm2Sqr(v2), tolerance);

        // assert orthogonal
        Assert.assertEquals(0, DoubleArray.dot(v1, v2), tolerance);
    }

    private void assertColumnsOrthonormal(double[][] matrix, double tolerance) {
        int numCols = matrix[0].length;

        for (int c = 0; c < numCols; c++) {
            double[] col1 = column(matrix, c);
            for (int i = c + 1; i < numCols; i++) {
                double[] col2 = column(matrix, i);
                assertOrthonormal(col1, col2, tolerance);
            }
        }
    }

    private void assertUpperTriangular(double[][] matrix, double tolerance) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        for (int c = 0; c < numCols; c++) {
            for (int r = c + 1; r < numRows; r++) {
                Assert.assertEquals(0, matrix[r][c], tolerance);
            }
        }
    }
}
