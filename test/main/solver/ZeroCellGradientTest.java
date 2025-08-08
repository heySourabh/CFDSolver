package main.solver;

import main.geom.Vector;
import main.mesh.*;
import main.util.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ZeroCellGradientTest {

    @Test
    public void setupAllCells_sets_all_cell_gradients_to_zero_vectors() {
        int numVars = 5;
        Cell cell1 = new Cell(null, null, null, numVars);
        Cell cell2 = new Cell(null, null, null, numVars);
        Mesh mesh = createMesh(cell1, cell2);
        CellGradientCalculator cellGradientCalculator = new ZeroCellGradient(mesh);
        cellGradientCalculator.setupAllCells();

        for (Cell cell : mesh.cells()) {
            for (int var = 0; var < numVars; var++) {
                TestHelper.assertVectorEquals(new Vector(0, 0, 0), cell.gradientU[var], 1e-15);
            }
        }
    }

    private Mesh createMesh(Cell... cellArray) {
        return new Mesh() {
            private final List<Cell> cells = List.of(cellArray);

            @Override
            public List<Cell> cells() {
                return cells;
            }

            @Override
            public List<Face> internalFaces() {
                return null;
            }

            @Override
            public List<Node> nodes() {
                return null;
            }

            @Override
            public List<Boundary> boundaries() {
                return null;
            }
        };
    }
}