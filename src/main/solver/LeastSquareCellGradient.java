package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static main.util.DoubleMatrix.*;

public class LeastSquareCellGradient implements CellGradientCalculator {

    private final Cell[][] neighbors;
    private final double[][][] matrices;
    private final double[][] weights;

    public LeastSquareCellGradient(Mesh mesh, NeighborsCalculator neighCalc) {
        int numCells = mesh.cells().size();
        this.neighbors = new Cell[numCells][];
        this.matrices = new double[numCells][][];
        this.weights = new double[numCells][];
        mesh.cellStream()
                .forEach(cell -> setup(cell, neighCalc));
    }

    private void setup(Cell cell, NeighborsCalculator neighCalc) {
        Cell[] neighs = neighCalc.calculateFor(cell).toArray(new Cell[0]);
        this.neighbors[cell.index] = neighs;

        List<Vector> distanceVectors = Arrays.stream(neighs)
                .map(neighCell -> new Vector(cell.shape.centroid, neighCell.shape.centroid))
                .collect(toList());

        this.weights[cell.index] = distanceVectors.stream()
                .mapToDouble(Vector::mag)
                .map(distance -> 1.0 / distance)
                .toArray();
        double sumWeights = Arrays.stream(this.weights[cell.index]).sum();
        this.weights[cell.index] = Arrays.stream(this.weights[cell.index])
                .map(w -> w / sumWeights)
                .toArray();

        double minDistance = distanceVectors.stream()
                .mapToDouble(Vector::mag)
                .min().orElse(1.0);

        Vector shiftBy = new Vector(0, 0, 0);
        boolean xZero = distanceVectors.stream().allMatch(v -> Math.abs(v.x) < 1e-15);
        if (xZero) {
            shiftBy = shiftBy.add(new Vector(minDistance, 0, 0));
        }
        boolean yZero = distanceVectors.stream().allMatch(v -> Math.abs(v.y) < 1e-15);
        if (yZero) {
            shiftBy = shiftBy.add(new Vector(0, minDistance, 0));
        }
        boolean zZero = distanceVectors.stream().allMatch(v -> Math.abs(v.z) < 1e-15);
        if (zZero) {
            shiftBy = shiftBy.add(new Vector(0, 0, minDistance));
        }

        double[][] A = new double[neighs.length][3];
        for (int iNeigh = 0; iNeigh < neighs.length; iNeigh++) {
            Vector distance = distanceVectors.get(iNeigh).add(shiftBy);
            double w = this.weights[cell.index][iNeigh];
            A[iNeigh][0] = distance.x * w;
            A[iNeigh][1] = distance.y * w;
            A[iNeigh][2] = distance.z * w;
        }

        double[][] AT = transpose(A);
        this.matrices[cell.index] = multiply(invert(multiply(AT, A)), AT);
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
        double[] deltaU = Arrays.stream(neighbors[cell.index])
                .mapToDouble(neighCell -> neighCell.U[var] - cell.U[var])
                .toArray();
        double[] deltaUWeighted = IntStream.range(0, deltaU.length)
                .mapToDouble(iNeigh -> weights[cell.index][iNeigh] * deltaU[iNeigh])
                .toArray();
        double[] derivatives = multiply(matrices[cell.index], deltaUWeighted);

        return new Vector(derivatives[0], derivatives[1], derivatives[2]);
    }
}
