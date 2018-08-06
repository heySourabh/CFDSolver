package main.solver;

import main.mesh.Cell;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FaceNeighbors implements NeighborsCalculator {
    @Override
    public List<Cell> calculateFor(Cell cell) {
        return cell.faces.stream()
                .flatMap(face -> Stream.of(face.left, face.right))
                .filter(c -> c != cell)
                .distinct()
                .collect(toList());
    }
}
