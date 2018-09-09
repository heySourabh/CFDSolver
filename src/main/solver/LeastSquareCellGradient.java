package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static main.util.DoubleArray.divide;
import static main.util.DoubleArray.sum;
import static main.util.DoubleMatrix.multiply;

public class LeastSquareCellGradient implements CellGradientCalculator {

    private final Cell[][] neighbors;
    private final double[][][] matrices;

    public LeastSquareCellGradient(Mesh mesh, NeighborsCalculator neighCalc) {
        int numCells = mesh.cells().size();
        this.neighbors = new Cell[numCells][];
        this.matrices = new double[numCells][][];

        mesh.cellStream().forEach(cell -> setup(cell, neighCalc));
    }

    private void setup(Cell cell, NeighborsCalculator neighCalc) {
        Cell[] neighs = neighCalc.calculateFor(cell).toArray(new Cell[0]);
        this.neighbors[cell.index()] = neighs;

        List<Vector> distanceVectors = Arrays.stream(neighs)
                .map(neighCell -> new Vector(cell.shape.centroid, neighCell.shape.centroid))
                .collect(toList());

        double[] weights = normalizeWeights(distanceVectors.stream()
                .mapToDouble(Vector::mag)
                .map(distance -> 1.0 / distance)
                .toArray());

        double[][] A = new double[neighs.length][3];
        for (int iNeigh = 0; iNeigh < neighs.length; iNeigh++) {
            Vector distance = distanceVectors.get(iNeigh);
            double w = weights[iNeigh];
            A[iNeigh][0] = distance.x * w;
            A[iNeigh][1] = distance.y * w;
            A[iNeigh][2] = distance.z * w;
        }

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        this.matrices[cell.index()] = svd.getSolver()
                .getInverse()
                .multiply(new DiagonalMatrix(weights))
                .getData();
    }

    @Override
    public Vector[] forCell(Cell cell) {
        int numVars = cell.U.length;
        Vector[] gradients = new Vector[numVars];
        for (int var = 0; var < numVars; var++) {
            gradients[var] = forVar(cell, var);
        }

        return gradients;
    }

    private Vector forVar(Cell cell, int var) {
        double[] deltaU = Arrays.stream(neighbors[cell.index()])
                .mapToDouble(neighCell -> neighCell.U[var] - cell.U[var])
                .toArray();
        double[] derivatives = multiply(matrices[cell.index()], deltaU);

        return new Vector(derivatives[0], derivatives[1], derivatives[2]);
    }

    private double[] normalizeWeights(double[] positiveWeights) {
        return divide(positiveWeights, sum(positiveWeights));
    }
}
