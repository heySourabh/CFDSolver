package main.solver;

import main.mesh.Cell;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class NodeNeighbors implements NeighborsCalculator {
    @Override
    public List<Cell> getFor(Cell cell) {
        return Arrays.stream(cell.nodes)
                .flatMap(node -> node.neighbors.stream())
                .filter(c -> c != cell)
                .distinct()
                .collect(toList());
    }
}
