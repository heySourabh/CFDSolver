package main.solver;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Polygon;
import main.mesh.*;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.physics.goveqn.factory.ScalarDiffusion;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;
import static org.junit.Assert.assertArrayEquals;

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

    @Test
    public void initialize_quad_with_varying_function() {
        Function<Point, double[]> f = p -> new double[]{sin(p.x) + cos(p.y) + 2.0};
        GoverningEquations govEqn = new ScalarDiffusion(0.145);
        int numVars = govEqn.numVars();
        Node n1 = new Node(0, 0, 0, numVars);
        Node n2 = new Node(PI, 0, 0, numVars);
        Node n3 = new Node(PI, PI, 0, numVars);
        Node n4 = new Node(0, PI, 0, numVars);
        Shape shape = new Shape(PI * PI, null);
        Cell cell = new Cell(new Node[]{n1, n2, n3, n4}, VTKType.VTK_QUAD, shape, numVars);
        Mesh mesh = createMesh(cell);

        SolutionInitializer initializer = new FunctionInitializer(f);
        initializer.initialize(mesh, govEqn);

        double[] expectedValue = {2.6372747421591165}; // Calculated using simple code for square
        assertArrayEquals(expectedValue, cell.U, 1e-12);
    }

    @Test
    public void initialize_tri_with_varying_function() {
        Function<Point, double[]> f = p -> new double[]{sin(p.x) + cos(p.y) + 2.0};
        GoverningEquations govEqn = new ScalarDiffusion(0.145);
        int numVars = govEqn.numVars();
        Node n1 = new Node(0, 0, 0, numVars);
        Node n2 = new Node(PI, 0, 0, numVars);
        Node n3 = new Node(PI, PI, 0, numVars);
        Shape shape = new Shape(PI * PI / 2, null);
        Cell cell = new Cell(new Node[]{n1, n2, n3}, VTKType.VTK_TRIANGLE, shape, numVars);
        Mesh mesh = createMesh(cell);

        SolutionInitializer initializer = new FunctionInitializer(f);
        initializer.initialize(mesh, govEqn);

        double[] expectedValue = {3.0421834976763016}; // Calculated using simple code for triangle, 5 levels
        assertArrayEquals(expectedValue, cell.U, 1e-15);
    }

    @Test
    public void initialize_geometry_with_varying_function() {
        Function<Point, double[]> f = p -> new double[]{sin(p.x) + cos(p.y) + 2.0};
        GoverningEquations govEqn = new ScalarDiffusion(0.145);
        int numVars = govEqn.numVars();
        Node n1 = new Node(0, 0, 0, numVars);
        Node n2 = new Node(PI, 0, 0, numVars);
        Node n3 = new Node(PI, PI, 0, numVars);
        Node n4 = new Node(0, PI, 0, numVars);
        Node n5 = new Node(0, PI / 2, 0, numVars);
        Point centroid = new Polygon(new Point[]{
                n1.location(), n2.location(), n3.location(),
                n4.location(), n5.location()}).centroid();
        Shape shape = new Shape(PI * PI, centroid);
        Cell cell = new Cell(new Node[]{n1, n2, n3, n4, n5}, VTKType.VTK_POLYGON, shape, numVars);
        Mesh mesh = createMesh(cell);

        SolutionInitializer initializer = new FunctionInitializer(f);
        initializer.initialize(mesh, govEqn);

        double[] expectedValue = f.apply(centroid);
        assertArrayEquals(expectedValue, cell.U, 1e-12);
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