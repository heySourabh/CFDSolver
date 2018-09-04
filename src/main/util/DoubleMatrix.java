package main.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Use this class only for small matrices for quick prototyping.
 * May not have efficient algorithms for large matrices.
 */
public class DoubleMatrix {
    public static double[][] add(double[][] A, double[][] B) {
        int numRows = A.length;
        int numCols = A[0].length;

        if (numRows != B.length || numCols != B[0].length) {
            throw new IllegalArgumentException("Size of A != Size of B.");
        }

        double[][] result = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        }

        return result;
    }

    /**
     * Subtract matrix B from matrix A => A - B
     *
     * @param A matrix
     * @param B matrix
     * @return A - B
     */
    public static double[][] subtract(double[][] A, double[][] B) {
        int numRows = A.length;
        int numCols = A[0].length;

        if (numRows != B.length || numCols != B[0].length) {
            throw new IllegalArgumentException("Size of A != Size of B.");
        }

        double[][] result = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        }

        return result;
    }

    public static double[][] multiply(double[][] matrix, double scalar) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[][] result = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                result[i][j] = matrix[i][j] * scalar;
            }
        }

        return result;
    }

    public static double[][] multiply(double[][] A, double[][] B) {
        int numRowsA = A.length;
        int numColsA = A[0].length;
        int numRowsB = B.length;
        int numColsB = B[0].length;

        if (numColsA != numRowsB) {
            throw new IllegalArgumentException("Number of columns of A != Number of rows of B.");
        }

        double[][] result = new double[numRowsA][numColsB];

        for (int i = 0; i < numRowsA; i++) {
            for (int j = 0; j < numColsB; j++) {
                double sum = 0;
                for (int k = 0; k < numColsA; k++) {
                    sum = Math.fma(A[i][k], B[k][j], sum);
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    public static double[] multiply(double[][] matrix, double[] vector) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        if (numCols != vector.length) {
            throw new IllegalArgumentException("Number of matrix columns != Length of Vector.");
        }

        double[] result = new double[numRows];

        for (int i = 0; i < numRows; i++) {
            double sum = 0.0;
            for (int j = 0; j < numCols; j++) {
                sum = Math.fma(matrix[i][j], vector[j], sum);
            }
            result[i] = sum;
        }

        return result;
    }

    public static double[] column(double[][] matrix, int col) {
        int numRows = matrix.length;
        double[] column = new double[numRows];
        for (int r = 0; r < numRows; r++) {
            column[r] = matrix[r][col];
        }

        return column;
    }

    public static double[][] transpose(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[][] result = new double[numCols][numRows];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                result[j][i] = matrix[i][j];
            }
        }

        return result;
    }

    public static double determinant(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        if (numRows != numCols) {
            throw new IllegalArgumentException("Not square matrix.");
        }

        if (numRows == 1) {
            return matrix[0][0];
        }

        double det = 0.0;
        for (int c = 0; c < numCols; c++) {
            int r = 0;
            double[][] mat = removeRowColumn(matrix, r, c);
            det = Math.fma(matrix[r][c], determinant(mat) * signOf(r, c), det);
        }

        return det;
    }

    public static double[][] invert(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        if (numRows != numCols) {
            throw new IllegalArgumentException("Not square matrix.");
        }

        double[][] adjoint = transpose(cofactorMatrix(matrix));
        double determinant = IntStream.range(0, adjoint.length)
                .mapToDouble(i -> adjoint[0][i] * matrix[i][0])
                .sum();

        if (Math.abs(determinant) < 1e-15) {
            throw new ArithmeticException("Matrix is singular.");
        }

        return multiply(adjoint, 1.0 / determinant);
    }

    public static double[][] cofactorMatrix(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        if (numRows != numCols) {
            throw new IllegalArgumentException("Not square matrix.");
        }

        double[][] result = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                double[][] m = removeRowColumn(matrix, i, j);
                result[i][j] = determinant(m) * signOf(i, j);
            }
        }

        return result;
    }

    private static int signOf(int row, int column) {
        return (row % 2 == 0 ? 1 : -1) * (column % 2 == 0 ? 1 : -1);
    }

    public static double[][] removeRowColumn(double[][] matrix, int row, int col) {
        return removeColumn(removeRow(matrix, row), col);
    }

    public static double[][] removeColumn(double[][] matrix, int col) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[][] result = new double[numRows][numCols - 1];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0, c = 0; j < numCols; j++) {
                if (j == col) continue;
                result[i][c] = matrix[i][j];
                c++;
            }
        }

        return result;
    }

    public static double[][] removeRow(double[][] matrix, int row) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[][] result = new double[numRows - 1][numCols];

        for (int i = 0, r = 0; i < numRows; i++) {
            if (i == row) continue;
            System.arraycopy(matrix[i], 0, result[r], 0, numCols);
            r++;
        }

        return result;
    }

    public static double[][] copyOf(double[][] matrix) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        double[][] copy = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, numCols);
        }

        return copy;
    }

    public static double[][] swapRows(double[][] matrix, int row1, int row2) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        if (row1 < 0 || row1 >= numRows) throw new IllegalArgumentException("Row index is out of bounds: " + row1);
        if (row2 < 0 || row2 >= numRows) throw new IllegalArgumentException("Row index is out of bounds: " + row2);
        if (row1 == row2) return copyOf(matrix);

        double[][] result = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            if (i == row1)
                System.arraycopy(matrix[row2], 0, result[row1], 0, numCols);
            else if (i == row2)
                System.arraycopy(matrix[row1], 0, result[row2], 0, numCols);
            else
                System.arraycopy(matrix[i], 0, result[i], 0, numCols);
        }

        return result;
    }

    public static double[][] swapColumns(double[][] matrix, int col1, int col2) {
        int numRows = matrix.length;
        int numCols = matrix[0].length;

        if (col1 < 0 || col1 >= numCols) throw new IllegalArgumentException("Column index is out of bounds: " + col1);
        if (col2 < 0 || col2 >= numCols) throw new IllegalArgumentException("Column index is out of bounds: " + col2);
        if (col1 == col2) return copyOf(matrix);

        double[][] result = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int jj = j;
                if (j == col1)
                    jj = col2;
                else if (j == col2)
                    jj = col1;
                result[i][jj] = matrix[i][j];
            }
        }

        return result;
    }

    public static String stringify(double[][] matrix) {
        int decimals = 10;
        int numberWidth = decimals + 10;
        String startEndDelimiter = IntStream.range(0, numberWidth * matrix[0].length)
                .mapToObj(i -> "-")
                .collect(Collectors.joining(""));

        String matrixString = Arrays.stream(matrix)
                .map(row -> Arrays.stream(row)
                        .mapToObj(d -> format(d, numberWidth, decimals))
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));

        return startEndDelimiter + "\n"
                + matrixString + "\n"
                + startEndDelimiter;
    }

    private static String format(double number, int maxPrintWidth, int numDecimals) {
        if (Math.abs(number) < 0.1 || Math.abs(number) >= 10)
            return String.format("%-" + maxPrintWidth + "." + numDecimals + "E", number);
        else
            return String.format("%-" + maxPrintWidth + "." + numDecimals + "f", number);
    }
}
