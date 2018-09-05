package main.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static main.util.DoubleArray.*;
import static main.util.DoubleArray.multiply;
import static main.util.DoubleArray.subtract;
import static main.util.DoubleMatrix.*;
import static main.util.DoubleMatrix.multiply;

public class QRDecomposition {

    private final double[][] Q;
    private final double[][] R;
    private final int[] P;

    public QRDecomposition(double[][] matrix, boolean permute) {
        int numCols = matrix[0].length;

        List<Integer> permutation = IntStream.range(0, numCols)
                .boxed().collect(Collectors.toList());
        List<double[]> matrixColumns = IntStream.range(0, numCols)
                .mapToObj(i -> column(matrix, i))
                .collect(Collectors.toList());

        if (permute)
            sortColumns(matrixColumns, permutation);

        P = permutation.stream()
                .mapToInt(i -> i)
                .toArray();

        List<double[]> orthonormalVectors = new ArrayList<>();
        for (int col = 0; col < numCols; col++) {
            double[] newVector = matrixColumns.get(col);
            for (double[] orthonormalVector : orthonormalVectors) {
                newVector = subtract(newVector, projection(newVector, orthonormalVector));
            }
            orthonormalVectors.add(unit(newVector));
        }

        this.Q = createColumnMatrix(orthonormalVectors);
        this.R = multiply(transpose(Q), createColumnMatrix(matrixColumns));
    }

    private void sortColumns(List<double[]> matrixColumns, List<Integer> permutation) {
        if (matrixColumns.size() <= 1) return;

        int largestColumnIndex = largestNormColumnIndex(matrixColumns);
        if (largestColumnIndex != 0) {
            Collections.swap(matrixColumns, 0, largestColumnIndex);
            Collections.swap(permutation, 0, largestColumnIndex);
        }
        sortColumns(matrixColumns.subList(1, matrixColumns.size()),
                permutation.subList(1, permutation.size()));
    }

    private int largestNormColumnIndex(List<double[]> columns) {
        double[] largestColumn = columns.stream()
                .reduce(columns.get(0), this::max);

        return columns.indexOf(largestColumn);
    }

    private double[] max(double[] col1, double[] col2) {
        return norm2Sqr(col1) > norm2Sqr(col2) ? col1 : col2;
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
        double projectionLength = dot(ofVector, onUnitVector);
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

    public int[] P() {
        return P;
    }
}
