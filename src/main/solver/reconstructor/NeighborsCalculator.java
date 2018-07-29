package main.solver.reconstructor;

import main.mesh.Cell;

import java.util.List;

public interface NeighborsCalculator {
    List<Cell> getFor(Cell cell);
}
