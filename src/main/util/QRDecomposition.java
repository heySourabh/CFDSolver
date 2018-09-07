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

        List<Integer> permutations = IntStream.range(0, numCols)
                .boxed().collect(Collectors.toList());

        List<double[]> matrixColumns = IntStream.range(0, numCols)
                .mapToObj(i -> column(matrix, i))
                .map(this::unit)
                .collect(Collectors.toList());

        List<double[]> orthonormalVectors = new ArrayList<>();

        if (permute)
            addOrthonormalVectors(matrixColumns, permutations, orthonormalVectors);
        else
            addOrthonormalVectors(matrixColumns, orthonormalVectors);

        this.P = permutations.stream().mapToInt(i -> i)
                .toArray();
        this.Q = createColumnMatrix(orthonormalVectors);
        this.R = multiply(transpose(Q), permute(matrix, P));
    }

    private void addOrthonormalVectors(List<double[]> matrixColumns, List<double[]> orthonormalVectors) {
        for (double[] col : matrixColumns) {
            orthonormalVectors.add(unit(newVectorUsing(col, orthonormalVectors)));
        }
    }

    private double[][] permute(double[][] matrix, int[] permutations) {
        List<double[]> columns = new ArrayList<>();
        for (int permutation : permutations) {
            columns.add(column(matrix, permutation));
        }
        return createColumnMatrix(columns);
    }

    private void addOrthonormalVectors(List<double[]> remainingColumns,
                                       List<Integer> permutation,
                                       List<double[]> existingOrthonormalVectors) {
        int numRemainingColumns = remainingColumns.size();
        if (numRemainingColumns == 0) return;

        int bestColumnIndex = bestColumnIndex(remainingColumns, existingOrthonormalVectors);
        double[] bestColumn = remainingColumns.get(bestColumnIndex);
        Collections.swap(remainingColumns, 0, bestColumnIndex);
        Collections.swap(permutation, 0, bestColumnIndex);

        double[] newUnitVector = unit(newVectorUsing(bestColumn, existingOrthonormalVectors));
        existingOrthonormalVectors.add(newUnitVector);

        addOrthonormalVectors(remainingColumns.subList(1, numRemainingColumns),
                permutation.subList(1, numRemainingColumns), existingOrthonormalVectors);
    }

    private int bestColumnIndex(List<double[]> columns, List<double[]> existingOrthonormalVectors) {
        if (columns.size() == 1) return 0;

        List<double[]> orthogonalVectors = columns.stream()
                .map(col -> newVectorUsing(col, existingOrthonormalVectors))
                .collect(Collectors.toList());

        double[] bestVector = orthogonalVectors.stream()
                .reduce(orthogonalVectors.get(0), this::max);

        return orthogonalVectors.indexOf(bestVector);
    }

    private double[] max(double[] v1, double[] v2) {
        return norm2Sqr(v1) > norm2Sqr(v2) ? v1 : v2;
    }

    private boolean isZero(double[] column) {
        return norm2Sqr(column) < 1e-15;
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

    private double[] newVectorUsing(double[] column, List<double[]> existingOrthonormalVectors) {
        double[] totalProjection = existingOrthonormalVectors.stream()
                .map(orthonormalVector -> projection(column, orthonormalVector))
                .reduce(zeros(column.length), DoubleArray::add);

        return subtract(column, totalProjection);
    }

    private double[] projection(double[] ofVector, double[] onUnitVector) {
        double projectionLength = dot(ofVector, onUnitVector);
        return multiply(onUnitVector, projectionLength);
    }

    private double[] unit(double[] vector) {
        if (isZero(vector))
            return vector;

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
