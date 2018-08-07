package main.solver;

import main.geom.Vector;
import main.mesh.Cell;

public interface CellGradient {
    Vector[] forCell(Cell cell);
}
