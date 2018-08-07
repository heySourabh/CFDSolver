package main.solver.reconstructor;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.solver.CellGradientCalculator;
import main.solver.LeastSquareCellGradient;
import main.solver.NeighborsCalculator;

import java.util.Arrays;

import static main.util.DoubleArray.add;

public class VKLimiterReconstructor implements SolutionReconstructor {
    private final Mesh mesh;
    private final CellGradientCalculator gradientCalculator;
    private final NeighborsCalculator neighCalc;
    private final Cell[][] neighbors;

    public VKLimiterReconstructor(Mesh mesh, NeighborsCalculator neighCalc) {
        int numCells = mesh.cells().size();
        this.mesh = mesh;
        this.gradientCalculator = new LeastSquareCellGradient(mesh, neighCalc);
        this.neighCalc = neighCalc;
        this.neighbors = new Cell[numCells][];

        mesh.cellStream().forEach(this::setup);
    }

    private void setup(Cell cell) {
        neighbors[cell.index] = neighCalc.calculateFor(cell).toArray(new Cell[0]);

        for (int var = 0; var < cell.U.length; var++) {
            cell.reconstructCoeffs[var] = new double[3];
        }
    }

    @Override
    public void reconstruct() {
        mesh.cellStream().forEach(this::reconstructCell);
    }

    private void reconstructCell(Cell cell) {
        Vector[] gradients = gradientCalculator.forCell(cell);
        for (int var = 0; var < cell.U.length; var++) {
            reconstructVar(cell, gradients, var);
        }
    }

    private void reconstructVar(Cell cell, Vector[] gradients, int var) {
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

        Vector gradient_unlimited = gradients[var];
        double phi_i = Arrays.stream(cell.nodes)
                .mapToDouble(node -> reconstructValueAt(cell, var, gradient_unlimited, node.location()))
                .map(uj -> Phi(duMin, duMax, cell.U[var], uj))
                .min().orElse(1.0);

        Vector gradient_limited = gradient_unlimited.mult(phi_i);
        cell.reconstructCoeffs[var][0] = gradient_limited.x;
        cell.reconstructCoeffs[var][1] = gradient_limited.y;
        cell.reconstructCoeffs[var][2] = gradient_limited.z;
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
