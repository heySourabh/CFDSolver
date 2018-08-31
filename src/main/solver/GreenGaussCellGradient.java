package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;

import java.util.Arrays;
import java.util.stream.IntStream;

public class GreenGaussCellGradient implements CellGradientCalculator {
    @Override
    public Vector[] forCell(Cell cell) {
        // Assuming that face average U is calculated
        Vector[] zeroVectors = IntStream.range(0, cell.U.length)
                .mapToObj(var -> new Vector(0, 0, 0))
                .toArray(Vector[]::new);

        Vector[] outNormalSum = cell.faces.stream()
                .filter(face -> face.left == cell)
                .map(this::scaledGradients)
                .reduce(zeroVectors, this::addVectorArrays);

        Vector[] inNormalSum = cell.faces.stream()
                .filter(face -> face.right == cell)
                .map(this::scaledGradients)
                .reduce(zeroVectors, this::addVectorArrays);

        return multiplyToVectors(
                subtractVectorArrays(outNormalSum, inNormalSum),
                1.0 / cell.shape.volume);
    }

    private Vector[] scaledGradients(Face face) {
        Vector areaProjection = face.surface.unitNormal.mult(face.surface.area);

        return Arrays.stream(face.U)
                .mapToObj(areaProjection::mult)
                .toArray(Vector[]::new);
    }

    private Vector[] multiplyToVectors(Vector[] vectors, double scalar) {
        return Arrays.stream(vectors)
                .map(v -> v.mult(scalar))
                .toArray(Vector[]::new);
    }

    private Vector[] addVectorArrays(Vector[] v1, Vector[] v2) {
        return IntStream.range(0, v1.length)
                .mapToObj(i -> v1[i].add(v2[i]))
                .toArray(Vector[]::new);
    }

    private Vector[] subtractVectorArrays(Vector[] v1, Vector[] v2) {
        return IntStream.range(0, v1.length)
                .mapToObj(i -> v1[i].sub(v2[i]))
                .toArray(Vector[]::new);
    }
}
