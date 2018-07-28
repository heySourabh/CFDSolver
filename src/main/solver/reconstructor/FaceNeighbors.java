package main.solver.reconstructor;

import main.mesh.Cell;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FaceNeighbors implements Neighbors {
    @Override
    public List<Cell> getFor(Cell cell) {
        return cell.faces.stream()
                .flatMap(face -> Stream.of(face.left, face.right))
                .filter(c -> c != cell)
                .distinct()
                .collect(toList());
    }
}
