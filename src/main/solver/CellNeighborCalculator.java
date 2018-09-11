package main.solver;

import main.mesh.Cell;

import java.util.List;

public interface CellNeighborCalculator {
    List<Cell> calculateFor(Cell cell);
}
