package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.Node;
import main.physics.goveqn.GoverningEquations;
import main.util.DoubleArray;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static main.util.DoubleArray.*;

public class FunctionInitializer implements SolutionInitializer {

    private final Function<Point, double[]> f;

    public FunctionInitializer(Function<Point, double[]> conservativeVarsFunction) {
        this.f = conservativeVarsFunction;
    }

    @Override
    public void initialize(Mesh mesh, GoverningEquations govEqn) {
        mesh.cellStream().forEach(cell -> initialize(cell, govEqn));
    }

    private void initialize(Cell cell, GoverningEquations govEqn) {
        double[] conservativeVars = calculateCentroidValues(cell);
        DoubleArray.copy(conservativeVars, cell.U);
        DoubleArray.copy(govEqn.realVars(conservativeVars), cell.Wn);
    }

    private double[] calculateCentroidValues(Cell cell) {
        // Using distance weighted least-square of nodal values
//        Point centroid = cell.shape.centroid;
//        List<Vector> distances = Arrays.stream(cell.nodes)
//                .map(Node::location)
//                .map(point -> new Vector(centroid, point))
//                .collect(Collectors.toList());
//        double[] weights = normalize(distances.stream()
//                .mapToDouble(this::weight)
//                .toArray());
//
//        RealMatrix A = new Array2DRowRealMatrix(weights.length, 4);
//        for (int i = 0; i < weights.length; i++) {
//            Vector rw = distances.get(i).mult(weights[i]);
//            A.setRow(i, new double[]{weights[i], rw.x, rw.y, rw.z});
//        }
//
//        double[][] U = Arrays.stream(cell.nodes)
//                .map(Node::location)
//                .map(f)
//                .toArray(double[][]::new);
//
//        SingularValueDecomposition svd = new SingularValueDecomposition(A);
//        return svd.getSolver().solve(
//                new DiagonalMatrix(weights).multiply(new Array2DRowRealMatrix(U)))
//                .getRow(0);

        // Using distance weighted nodal values
//        Point centroid = cell.shape.centroid;
//        List<Vector> distances = Arrays.stream(cell.nodes)
//                .map(Node::location)
//                .map(point -> new Vector(centroid, point))
//                .collect(Collectors.toList());
//        double[] weights = normalize(distances.stream()
//                .mapToDouble(this::weight)
//                .toArray());
//
//        double[] U = new double[cell.U.length];
//        for (int i = 0; i < cell.nodes.length; i++) {
//            increment(U, multiply(f.apply(cell.nodes[i].location()), weights[i]));
//        }
//
//        return U;

        // Value at cell centroid
        return f.apply(cell.shape.centroid);
    }

    private double weight(Vector dr) {
        return 1.0 / dr.mag();
    }

    private double[] normalize(double[] positiveWeights) {
        return divide(positiveWeights, sum(positiveWeights));
    }
}
