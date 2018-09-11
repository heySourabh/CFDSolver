package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LeastSquareCellGradient implements CellGradientCalculator {

    private final Cell[][] cellSupport;
    private final LeastSquareLinearInterpolator[] interpolators;

    /**
     * This gradient calculator can be used for 3D.
     * When using for 2D / 1D make sure that all the cells are in one single plane(2D) / line(1D),
     * otherwise the gradients will be erroneous.<br>
     * In case the mesh is not on a single plane(2D) / line(1D), then use GreenGaussCellGradient.
     *
     * @param mesh      Mesh
     * @param neighCalc (cell) -> List<Cell>
     */
    public LeastSquareCellGradient(Mesh mesh, NeighborsCalculator neighCalc) {
        int numCells = mesh.cells().size();
        this.cellSupport = new Cell[numCells][];
        this.interpolators = new LeastSquareLinearInterpolator[numCells];

        mesh.cellStream().forEach(cell -> setup(cell, neighCalc));
    }

    private void setup(Cell cell, NeighborsCalculator neighCalc) {
        List<Cell> neighs = neighCalc.calculateFor(cell);
        Cell[] allCells = Stream.concat(Stream.of(cell), neighs.stream())
                .toArray(Cell[]::new);
        this.cellSupport[cell.index()] = allCells;
        this.interpolators[cell.index()] = new LeastSquareLinearInterpolator(
                Arrays.stream(allCells)
                        .map(c -> c.shape.centroid).toArray(Point[]::new));
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
        int cellIndex = cell.index();
        Cell[] neighCells = cellSupport[cellIndex];
        double[] U = new double[neighCells.length];

        for (int neigh = 0; neigh < U.length; neigh++) {
            U[neigh] = neighCells[neigh].U[var];
        }

        return interpolators[cellIndex].interpolate(U).gradient();
    }
}
