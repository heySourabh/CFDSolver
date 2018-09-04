package main.util;

import org.junit.Test;

import static main.util.DoubleMatrix.multiply;
import static main.util.DoubleMatrix.stringify;
import static main.util.TestHelper.assertMatrixEquals;

public class QRDecompositionTest {
    @Test
    public void qr_decomposition_of_tall_matrix_example1() {
        double[][] A = {
                {2, 1},
                {2, 1},
                {1, 5}
        };
        double[][] Q = {
                {2.0 / 3.0, -1.0 / Math.sqrt(18.0)},
                {2.0 / 3.0, -1.0 / Math.sqrt(18.0)},
                {1.0 / 3.0, 4.0 / Math.sqrt(18.0)}
        };
        double[][] R = {
                {3.0, 3.0},
                {0.0, Math.sqrt(18.0)}
        };
        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }

    @Test
    public void qr_decomposition_of_tall_matrix_example2() {
        double[][] A = {
                {1, 2, 3},
                {-1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };
        double sqrt76 = Math.sqrt(76);
        double sqrt38 = Math.sqrt(38);
        double[][] Q = {
                {0.5, 5.0 / sqrt76, 4.0 / sqrt38},
                {-0.5, 7.0 / sqrt76, -2.0 / sqrt38},
                {0.5, 1.0 / sqrt76, -3.0 / sqrt38},
                {0.5, 1.0 / sqrt76, -3.0 / sqrt38}
        };
        double[][] R = {
                {2, 1.5, 2},
                {0, 19.0 / sqrt76, 24.0 / sqrt76},
                {0, 0, 4.0 / sqrt38}
        };
        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
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

        QRDecomposition qrDecomposition = new QRDecomposition(A);

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

        QRDecomposition qrDecomposition = new QRDecomposition(A);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }

    @Test
    public void qr_decomposition_of_square_matrix_example1() {
        double[][] A = {
                {4, 9, -14},
                {3, 13, 2},
                {0, 5, 0}
        };

        double sqrt2 = Math.sqrt(2);
        double sqrt2x5 = sqrt2 * 5;
        double[][] Q = {
                {4.0 / 5.0, -3.0 / sqrt2x5, -3.0 / sqrt2x5},
                {3.0 / 5.0, 4.0 / sqrt2x5, 4.0 / sqrt2x5},
                {0.0, 1.0 / sqrt2, -1.0 / sqrt2}
        };

        double[][] R = {
                {5, 15, -10},
                {0, sqrt2x5, sqrt2x5},
                {0, 0, sqrt2x5}
        };

        assertMatrixEquals(A, multiply(Q, R), 1e-12);

        QRDecomposition qrDecomposition = new QRDecomposition(A);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
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

        QRDecomposition qrDecomposition = new QRDecomposition(A);

        assertMatrixEquals(Q, qrDecomposition.Q(), 1e-12);

        assertMatrixEquals(R, qrDecomposition.R(), 1e-12);
    }
}
