package main.util;

import java.util.ArrayList;
import java.util.List;

import static main.util.DoubleArray.*;
import static main.util.DoubleMatrix.*;

public class QRDecomposition {

    private final double[][] Q;
    private final double[][] R;

    public QRDecomposition(double[][] matrix) {
        int numCols = matrix[0].length;

        List<double[]> orthonormalVectors = new ArrayList<>();
        for (int col = 0; col < numCols; col++) {
            double[] newVector = column(matrix, col);
            for (double[] orthonormalVector : orthonormalVectors) {
                newVector = subtract(newVector, projection(newVector, orthonormalVector));
            }
            orthonormalVectors.add(unit(newVector));
        }

        this.Q = createColumnMatrix(orthonormalVectors);
        this.R = multiply(transpose(Q), matrix);
    }

    private double[][] createColumnMatrix(List<double[]> columns) {
        int numRows = columns.get(0).length;
        int numCols = columns.size();

        double[][] matrix = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                matrix[i][j] = columns.get(j)[i];
            }
        }

        return matrix;
    }

    private double[] projection(double[] ofVector, double[] onUnitVector) {
        double projectionLength = sum(multiply(ofVector, onUnitVector));
        return multiply(onUnitVector, projectionLength);
    }

    private double[] unit(double[] vector) {
        double norm2 = norm2(vector);
        return divide(vector, norm2);
    }

    public double[][] Q() {
        return Q;
    }

    public double[][] R() {
        return R;
    }
}
