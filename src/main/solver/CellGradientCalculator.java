package main.solver;

import main.geom.Vector;
import main.mesh.Cell;

public interface CellGradientCalculator {
    Vector[] forCell(Cell cell);
}
