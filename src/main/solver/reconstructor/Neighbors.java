package main.solver.reconstructor;

import main.mesh.Cell;

import java.util.List;

public interface Neighbors {
    List<Cell> getFor(Cell cell);
}
