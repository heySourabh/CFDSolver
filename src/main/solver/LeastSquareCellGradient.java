package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.Arrays;
import java.util.List;

import static main.util.DoubleArray.divide;
import static main.util.DoubleArray.sum;
import static main.util.DoubleMatrix.multiply;

public class LeastSquareCellGradient implements CellGradientCalculator {

    private final Cell[][] neighbors;
    private final double[][][] inverseMatrix;
    private final Mesh mesh;

    /**
     * This gradient calculator can be used for 3D.
     * When using for 2D / 1D make sure that all the cells are in one single plane(2D) / line(1D),
     * otherwise the gradients will be erroneous.<br>
     * In case the mesh is not on a single plane(2D) / line(1D), then use GreenGaussCellGradient.
     *
     * @param mesh      Mesh
     * @param neighCalc (cell) -> List<Cell>
     */
    public LeastSquareCellGradient(Mesh mesh, CellNeighborCalculator neighCalc) {
        int numCells = mesh.cells().size();
        this.neighbors = new Cell[numCells][];
        this.inverseMatrix = new double[numCells][][];
        this.mesh = mesh;

        mesh.cellStream().forEach(cell -> setup(cell, neighCalc));
    }

    private void setup(Cell cell, CellNeighborCalculator neighCalc) {
        List<Cell> neighs = neighCalc.calculateFor(cell);
        this.neighbors[cell.index()] = neighs.toArray(new Cell[0]);
        this.inverseMatrix[cell.index()] = leastSquareMatrix(cell, neighs);
    }

    private double[][] leastSquareMatrix(Cell cell, List<Cell> neighs) {
        Vector[] distances = neighs.stream()
                .map(neigh -> new Vector(cell.shape.centroid, neigh.shape.centroid))
                .toArray(Vector[]::new);

        double[] weights = normalize(Arrays.stream(distances)
                .mapToDouble(this::weight)
                .toArray());

        RealMatrix A = new Array2DRowRealMatrix(neighs.size(), 3);
        for (int i = 0; i < neighs.size(); i++) {
            Vector rw = distances[i].mult(weights[i]);
            A.setRow(i, new double[]{rw.x, rw.y, rw.z});
        }

        return invert(A).multiply(new DiagonalMatrix(weights))
                .getData();
    }

    private RealMatrix invert(RealMatrix matrix) {
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        return svd.getSolver().getInverse();
    }

    private double weight(Vector dr) {
        return 1.0 / dr.mag();
    }

    private double[] normalize(double[] positiveWeights) {
        return divide(positiveWeights, sum(positiveWeights));
    }

    @Override
    public void setupAllCells() {
        mesh.cellStream().forEach(this::setCell);
    }

    private void setCell(Cell cell) {
        int numVars = cell.U.length;
        for (int var = 0; var < numVars; var++) {
            cell.gradientU[var] = forVar(cell, var);
        }
    }

    private Vector forVar(Cell cell, int var) {
        int cellIndex = cell.index();
        Cell[] neighCells = neighbors[cellIndex];
        double[] dU = new double[neighCells.length];

        for (int neigh = 0; neigh < dU.length; neigh++) {
            dU[neigh] = neighCells[neigh].U[var] - cell.U[var];
        }
        double[] solution = multiply(inverseMatrix[cellIndex], dU);

        return new Vector(solution[0], solution[1], solution[2]);
    }
}
