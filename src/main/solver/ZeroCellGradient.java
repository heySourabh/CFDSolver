package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;

public class ZeroCellGradient implements CellGradientCalculator {
    private final Mesh mesh;

    public ZeroCellGradient(Mesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public void setupAllCells() {
        mesh.cellStream().forEach(this::setupCell);
    }

    private void setupCell(Cell cell) {
        int numVars = cell.gradientU.length;
        for (int var = 0; var < numVars; var++) {
            cell.gradientU[var] = new Vector(0, 0, 0);
        }
    }
}
