package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.*;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

public class FunctionInitializerTest {

    @Test
    public void initialize_with_constant_function() {
        double[] conservativeVars = {4, 5, 8, 6, 7};
        Function<Point, double[]> f = p -> conservativeVars;
        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        Node[] nodes = {
                new Node(-2.83, -5.92, -3.01, numVars),
                new Node(-9.54, -0.81, -4.15, numVars),
                new Node(-7.22, 0.39, -1.66, numVars),
                new Node(-0.91, -8.49, -5.67, numVars)};
        Cell c1 = new Cell(nodes,
                null, createShape(nodes), numVars);

        nodes = new Node[]{
                new Node(8.88, 6.42, 1.51, numVars),
                new Node(9.96, 9.96, -2.58, numVars),
                new Node(-5.01, -6.85, -5.75, numVars),
                new Node(4.79, 5.06, 5.30, numVars),
                new Node(4.33, 7.21, -1.46, numVars)};
        Cell c2 = new Cell(nodes,
                null, createShape(nodes), numVars);

        nodes = new Node[]{
                new Node(-1.99, -7.47, -7.02, numVars),
                new Node(1.24, -3.67, 2.56, numVars),
                new Node(-1.83, -1.20, 8.40, numVars)};
        Cell c3 = new Cell(nodes,
                null, createShape(nodes), numVars);

        nodes = new Node[]{
                new Node(5.91, 0.03, 1.34, numVars),
                new Node(-8.35, 4.93, -9.20, numVars),
                new Node(-4.44, -3.62, 5.77, numVars),
                new Node(-0.59, -5.27, 2.63, numVars)};
        Cell c4 = new Cell(nodes,
                null, createShape(nodes), numVars);

        Cell[] cells = {c1, c2, c3, c4};

        Mesh mesh = createMesh(cells);

        FunctionInitializer initializer = new FunctionInitializer(f);
        initializer.initialize(mesh, govEqn);

        for (Cell cell : cells) {
            assertArrayEquals(conservativeVars, cell.U, 1e-12);
        }
    }

    private Mesh createMesh(Cell... cellArray) {
        return new Mesh() {
            private List<Cell> cells = List.of(cellArray);

            @Override
            public List<Cell> cells() {
                return cells;
            }

            @Override
            public List<Face> internalFaces() {
                return null;
            }

            @Override
            public List<Node> nodes() {
                return null;
            }

            @Override
            public List<Boundary> boundaries() {
                return null;
            }
        };
    }

    private Shape createShape(Node... nodes) {
        Point approxCentroid = Arrays.stream(nodes)
                .map(node -> node.location().toVector())
                .reduce(Vector::add)
                .orElseThrow()
                .mult(1.0 / nodes.length)
                .toPoint();

        return new Shape(0.0, approxCentroid);
    }
}