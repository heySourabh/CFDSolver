package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.util.DoubleArray;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Arrays;
import java.util.stream.IntStream;

import static main.util.DoubleArray.divide;
import static main.util.DoubleArray.sum;
import static main.util.DoubleMatrix.multiply;

public class LeastSquareLinearInterpolator {

    private final double[][] inverseMatrix;
    private final Point p0;

    public LeastSquareLinearInterpolator(Point[] points) {
        RealMatrix matrix = new Array2DRowRealMatrix(points.length - 1, 3);
        p0 = points[0];

        Vector[] distances = IntStream.range(1, points.length)
                .mapToObj(i -> new Vector(p0, points[i]))
                .toArray(Vector[]::new);

        double[] weights = normalize(Arrays.stream(distances)
                .mapToDouble(this::weight)
                .toArray());

        for (int i = 0; i < points.length - 1; i++) {
            Vector dr = distances[i].mult(weights[i]);
            matrix.setRow(i, new double[]{dr.x, dr.y, dr.z});
        }

        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        inverseMatrix = invert(svd).multiply(new DiagonalMatrix(weights)).getData();
    }

    /**
     * LinearFunction for interpolating the provided values.
     *
     * @param values The values at the points provided in the constructor, in the same order.
     * @return Interpolating function.
     */
    public LinearFunction interpolate(double[] values) {
        double v0 = values[0];
        double[] dU = new double[values.length - 1];
        for (int i = 1; i < values.length; i++) {
            dU[i - 1] = values[i] - v0;
        }
        double[] solution = multiply(inverseMatrix, dU);

        return new LinearFunction(p0, v0, new Vector(solution[0], solution[1], solution[2]));
    }

    // Ignores singular values smaller than 10% of largest value
    private RealMatrix invert(SingularValueDecomposition svd) {
        double[] singularValues = svd.getSingularValues();
        double maxS = singularValues[0];
        double[] invSingularValues = DoubleArray.apply(singularValues, s -> s > maxS / 10.0 ? 1.0 / s : 0.0);

        RealMatrix invS = new DiagonalMatrix(invSingularValues);
        return svd.getV().multiply(invS).multiply(svd.getUT());
    }

    private double weight(Vector dr) {
        return 1.0 / dr.mag();
    }

    private double[] normalize(double[] positiveWeights) {
        return divide(positiveWeights, sum(positiveWeights));
    }
}
