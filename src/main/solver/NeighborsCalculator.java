package main.solver;

import main.mesh.Cell;

import java.util.List;

public interface NeighborsCalculator {
    List<Cell> calculateFor(Cell cell);
}
