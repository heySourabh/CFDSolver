package main.solver.reconstructor;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.solver.FaceNeighbors;
import main.solver.NeighborsCalculator;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static main.util.DoubleArray.add;
import static main.util.DoubleArray.*;
import static main.util.DoubleArray.multiply;
import static main.util.DoubleMatrix.*;
import static main.util.DoubleMatrix.multiply;

public class VKLimiterReconstructor implements SolutionReconstructor {
    private final Cell[][] neighbors;
    private final double[][][] matrices;
    private final NeighborsCalculator neighsCalc = new FaceNeighbors();
    private final Mesh mesh;

    public VKLimiterReconstructor(Mesh mesh) {
        this.mesh = mesh;
        final int numCells = mesh.cells().size();
        neighbors = new Cell[numCells][];
        matrices = new double[numCells][][];
        mesh.cellStream().forEach(this::setup);
    }

    private void setup(Cell cell) {
        Cell[] neighs = neighsCalc.calculateFor(cell).toArray(new Cell[0]);
        neighbors[cell.index] = neighs;

        List<Vector> distanceVectors = Arrays.stream(neighs)
                .map(neighCell -> new Vector(cell.shape.centroid, neighCell.shape.centroid))
                .collect(toList());

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
        for (int i = 0; i < A.length; i++) {
            Vector distance = distanceVectors.get(i).add(shiftBy);
            A[i][0] = distance.x;
            A[i][1] = distance.y;
            A[i][2] = distance.z;
        }

        double[][] AT = transpose(A);
        matrices[cell.index] = multiply(invert(multiply(AT, A)), AT);

        for (int var = 0; var < cell.U.length; var++) {
            cell.reconstructCoeffs[var] = new double[3];
        }
    }

    @Override
    public void reconstruct() {
        mesh.cellStream().forEach(this::reconstructCell);
    }

    private void reconstructCell(Cell cell) {
        for (int var = 0; var < cell.U.length; var++) {
            reconstructVar(cell, var);
        }
    }

    private void reconstructVar(Cell cell, int var) {
        double[] deltaU = Arrays.stream(neighbors[cell.index])
                .mapToDouble(neighCell -> neighCell.U[var] - cell.U[var])
                .toArray();
        double[] derivatives = multiply(matrices[cell.index], deltaU);
        Vector gradient = new Vector(derivatives[0], derivatives[1], derivatives[2]);

        double uMax = Arrays.stream(neighbors[cell.index])
                .mapToDouble(neighCell -> neighCell.U[var])
                .max().orElse(Double.POSITIVE_INFINITY);
        uMax = Math.max(uMax, cell.U[var]);
        double duMax = uMax - cell.U[var];

        double uMin = Arrays.stream(neighbors[cell.index])
                .mapToDouble(neighCell -> neighCell.U[var])
                .min().orElse(Double.NEGATIVE_INFINITY);
        uMin = Math.min(uMin, cell.U[var]);
        double duMin = uMin - cell.U[var];

        double phi_i = Arrays.stream(cell.nodes)
                .mapToDouble(node -> reconstructValueAt(cell, var, gradient, node.location()))
                .map(uj -> Phi(duMin, duMax, cell.U[var], uj))
                .min().orElse(1.0);

        derivatives = multiply(derivatives, phi_i);

        copy(derivatives, cell.reconstructCoeffs[var]);
    }

    private double reconstructValueAt(Cell cell, int var, Vector gradient, Point at) {
        Vector distanceVector = new Vector(cell.shape.centroid, at);
        return cell.U[var] + gradient.dot(distanceVector);
    }

    private double Phi(double duMin, double duMax, double ui, double uj) {
        double deltaMinus = (uj - ui);
        if (uj - ui > 0) {
            return phi(duMax, deltaMinus);
        } else if (uj - ui < 0) {
            return phi(duMin, deltaMinus);
        } else {
            return 1;
        }
    }

    private double phi(double dp, double dm) {
        double y = dp / dm;
        return (y * y + 2.0 * y) / (y * y + y + 2.0);
    }

    @Override
    public double[] conservativeVars(Cell cell, Point atPoint) {
        int numVars = cell.U.length;
        Vector r = new Vector(cell.shape.centroid, atPoint);
        double[] dU = new double[numVars];
        for (int var = 0; var < numVars; var++) {
            double du_dx = cell.reconstructCoeffs[var][0];
            double du_dy = cell.reconstructCoeffs[var][1];
            double du_dz = cell.reconstructCoeffs[var][2];
            Vector gradient = new Vector(du_dx, du_dy, du_dz);

            dU[var] = gradient.dot(r);
        }

        return add(cell.U, dU);
    }
}
