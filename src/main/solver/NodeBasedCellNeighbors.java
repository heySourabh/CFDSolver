package main.solver;

import main.mesh.Cell;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class NodeBasedCellNeighbors implements CellNeighborCalculator {
    @Override
    public List<Cell> calculateFor(Cell cell) {
        return Arrays.stream(cell.nodes)
                .flatMap(node -> node.neighbors.stream())
                .filter(c -> c != cell)
                .distinct()
                .collect(toList());
    }
}
