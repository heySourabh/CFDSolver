package main.mesh.factory;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static main.util.TestHelper.*;
import static org.junit.Assert.assertEquals;

public class Structured2DMeshSimpleTest {
    @Test
    public void mesh_with_single_quad_cell() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 2\n" +
                    "eta = 2\n" +
                    "0.000000000000000    0.000000000000000    0.000000000000000   \n" +
                    "0.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "1.000000000000000    0.000000000000000    0.000000000000000   \n" +
                    "1.000000000000000    1.000000000000000    0.000000000000000   \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(0.0, 0.0, 0.0);
        Node n1 = new Node(0.0, 1.0, 0.0);
        Node n2 = new Node(1.0, 0.0, 0.0);
        Node n3 = new Node(1.0, 1.0, 0.0);

        Shape shape = new Shape(1, new Point(0.5, 0.5, 0));
        Cell cell = new Cell(0, new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, shape, numVars);

        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n0, n1, new Node(-1, 1, 0), new Node(-1, 0, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(-0.5, 0.5, 0)), numVars);
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n2, n3, new Node(2, 1, 0), new Node(2, 0, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(1.5, 0.5, 0)), numVars);
        Cell ghostCellBottom = new Cell(-1, new Node[]{
                n0, n2, new Node(1, -1, 0), new Node(0, -1, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(0.5, -0.5, 0)), numVars);
        Cell ghostCellTop = new Cell(-1, new Node[]{
                n1, n3, new Node(1, 2, 0), new Node(0, 2, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(0.5, 1.5, 0)), numVars);

        Surface surface = new Surface(1, new Point(0, 0.5, 0), new Vector(-1, 0, 0));
        Face f0 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cell, ghostCellLeft, numVars);
        surface = new Surface(1, new Point(0.5, 1, 0), new Vector(0, 1, 0));
        Face f1 = new Face(new Node[]{n1, n3}, VTKType.VTK_LINE, surface, cell, ghostCellTop, numVars);
        surface = new Surface(1, new Point(1, 0.5, 0), new Vector(1, 0, 0));
        Face f2 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cell, ghostCellRight, numVars);
        surface = new Surface(1, new Point(0.5, 0, 0), new Vector(0, -1, 0));
        Face f3 = new Face(new Node[]{n0, n2}, VTKType.VTK_LINE, surface, cell, ghostCellBottom, numVars);

        cell.faces.addAll(List.of(f0, f1, f2, f3));
        ghostCellLeft.faces.add(f0);
        ghostCellTop.faces.add(f1);
        ghostCellRight.faces.add(f2);
        ghostCellBottom.faces.add(f3);

        // Check cells
        List<Cell> expectedCells = List.of(cell);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-12);
        }

        n0.neighbors.addAll(List.of(cell, ghostCellBottom, ghostCellLeft));
        n1.neighbors.addAll(List.of(cell, ghostCellLeft, ghostCellTop));
        n2.neighbors.addAll(List.of(cell, ghostCellBottom, ghostCellRight));
        n3.neighbors.addAll(List.of(cell, ghostCellTop, ghostCellRight));

        // Check nodes
        assertEquals(4, mesh.nodes().size());
        assertNodeEquals(n0, mesh.nodes().get(0), 1e-12);
        assertNodeEquals(n1, mesh.nodes().get(1), 1e-12);
        assertNodeEquals(n2, mesh.nodes().get(2), 1e-12);
        assertNodeEquals(n3, mesh.nodes().get(3), 1e-12);

        // Internal face check
        List<Face> expectedInternalFaces = List.of();
        assertEquals(expectedInternalFaces.size(), mesh.internalFaces().size());
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-12);

        // Boundary face check
        Boundary xi_min = new Boundary("xi min", List.of(f0), bc);
        Boundary xi_max = new Boundary("xi max", List.of(f2), bc);
        Boundary eta_min = new Boundary("eta min", List.of(f3), bc);
        Boundary eta_max = new Boundary("eta max", List.of(f1), bc);

        List<Boundary> expectedBoundaries = List.of(xi_min, xi_max, eta_min, eta_max);
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-12);
    }

    @Test
    public void mesh_with_single_quad_cell_x_reversed() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 2\n" +
                    "eta = 2\n" +
                    "1.000000000000000    0.000000000000000    0.000000000000000   \n" +
                    "1.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "0.000000000000000    0.000000000000000    0.000000000000000   \n" +
                    "0.000000000000000    1.000000000000000    0.000000000000000   \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(1.0, 0.0, 0.0);
        Node n1 = new Node(1.0, 1.0, 0.0);
        Node n2 = new Node(0.0, 0.0, 0.0);
        Node n3 = new Node(0.0, 1.0, 0.0);

        Shape shape = new Shape(1, new Point(0.5, 0.5, 0));
        Cell cell = new Cell(0, new Node[]{n2, n3, n1, n0}, VTKType.VTK_QUAD, shape, numVars);

        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n2, n3, new Node(-1, 1, 0), new Node(-1, 0, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(-0.5, 0.5, 0)), numVars);
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n0, n1, new Node(2, 1, 0), new Node(2, 0, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(1.5, 0.5, 0)), numVars);
        Cell ghostCellBottom = new Cell(-1, new Node[]{
                n2, n0, new Node(1, -1, 0), new Node(0, -1, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(0.5, -0.5, 0)), numVars);
        Cell ghostCellTop = new Cell(-1, new Node[]{
                n3, n1, new Node(1, 2, 0), new Node(0, 2, 0)}, VTKType.VTK_QUAD,
                new Shape(1, new Point(0.5, 1.5, 0)), numVars);

        Surface surface = new Surface(1, new Point(0, 0.5, 0), new Vector(-1, 0, 0));
        Face f0 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cell, ghostCellLeft, numVars);
        surface = new Surface(1, new Point(0.5, 1, 0), new Vector(0, 1, 0));
        Face f1 = new Face(new Node[]{n3, n1}, VTKType.VTK_LINE, surface, cell, ghostCellTop, numVars);
        surface = new Surface(1, new Point(1, 0.5, 0), new Vector(1, 0, 0));
        Face f2 = new Face(new Node[]{n1, n0}, VTKType.VTK_LINE, surface, cell, ghostCellRight, numVars);
        surface = new Surface(1, new Point(0.5, 0, 0), new Vector(0, -1, 0));
        Face f3 = new Face(new Node[]{n0, n2}, VTKType.VTK_LINE, surface, cell, ghostCellBottom, numVars);

        cell.faces.addAll(List.of(f0, f1, f2, f3));
        ghostCellLeft.faces.add(f0);
        ghostCellTop.faces.add(f1);
        ghostCellRight.faces.add(f2);
        ghostCellBottom.faces.add(f3);

        // Check cells
        List<Cell> expectedCells = List.of(cell);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-12);
        }

        n0.neighbors.addAll(List.of(cell, ghostCellBottom, ghostCellRight));
        n1.neighbors.addAll(List.of(cell, ghostCellRight, ghostCellTop));
        n2.neighbors.addAll(List.of(cell, ghostCellBottom, ghostCellLeft));
        n3.neighbors.addAll(List.of(cell, ghostCellTop, ghostCellLeft));

        // Check nodes
        assertEquals(4, mesh.nodes().size());
        assertNodeEquals(n0, mesh.nodes().get(0), 1e-12);
        assertNodeEquals(n1, mesh.nodes().get(1), 1e-12);
        assertNodeEquals(n2, mesh.nodes().get(2), 1e-12);
        assertNodeEquals(n3, mesh.nodes().get(3), 1e-12);

        // Internal face check
        List<Face> expectedInternalFaces = List.of();
        assertEquals(expectedInternalFaces.size(), mesh.internalFaces().size());
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-12);

        // Boundary face check
        Boundary xi_min = new Boundary("xi min", List.of(f2), bc);
        Boundary xi_max = new Boundary("xi max", List.of(f0), bc);
        Boundary eta_min = new Boundary("eta min", List.of(f3), bc);
        Boundary eta_max = new Boundary("eta max", List.of(f1), bc);

        List<Boundary> expectedBoundaries = List.of(xi_min, xi_max, eta_min, eta_max);
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-12);
    }

    @Test
    public void mesh_with_two_cells_in_x_direction() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 2\n" +
                    "-1.0       -0.5       0.0       \n" +
                    "-1.0       0.5        0.0       \n" +
                    "0.0        -0.5       0.0       \n" +
                    "0.0        0.5        0.0       \n" +
                    "1.0        -0.5       0.0       \n" +
                    "1.0        0.5        0.0       \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(-1.0, -0.5, 0.0);
        Node n1 = new Node(-1.0, 0.5, 0.0);
        Node n2 = new Node(0.0, -0.5, 0.0);
        Node n3 = new Node(0.0, 0.5, 0.0);
        Node n4 = new Node(1.0, -0.5, 0.0);
        Node n5 = new Node(1.0, 0.5, 0.0);

        Shape shape0 = new Shape(1, new Point(-0.5, 0, 0));
        Cell cell0 = new Cell(0, new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, shape0, numVars);

        Shape shape1 = new Shape(1, new Point(0.5, 0, 0));
        Cell cell1 = new Cell(1, new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, shape1, numVars);

        Shape shape = new Shape(1, new Point(-1.5, 0, 0));
        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n0, n1, new Node(-2, -0.5, 0), new Node(-2, 0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(1.5, 0, 0));
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n4, n5, new Node(2, 0.5, 0), new Node(2, -0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, 1, 0));
        Cell ghostCellTopLeft = new Cell(-1, new Node[]{
                n1, n3, new Node(0, 1.5, 0), new Node(-1, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, 1, 0));
        Cell ghostCellTopRight = new Cell(-1, new Node[]{
                n3, n5, new Node(1, 1.5, 0), new Node(0, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, -1, 0));
        Cell ghostCellBottomLeft = new Cell(-1, new Node[]{
                n0, n2, new Node(0, -1.5, 0), new Node(-1, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, -1, 0));
        Cell ghostCellBottomRight = new Cell(-1, new Node[]{
                n2, n4, new Node(1, -1.5, 0), new Node(0, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        Surface surface = new Surface(1, new Point(-0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n0, n2}, VTKType.VTK_LINE, surface, cell0, ghostCellBottomLeft, numVars);

        surface = new Surface(1, new Point(0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f1 = new Face(new Node[]{n2, n4}, VTKType.VTK_LINE, surface, cell1, ghostCellBottomRight, numVars);

        surface = new Surface(1, new Point(-0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n1, n3}, VTKType.VTK_LINE, surface, cell0, ghostCellTopLeft, numVars);

        surface = new Surface(1, new Point(0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f3 = new Face(new Node[]{n3, n5}, VTKType.VTK_LINE, surface, cell1, ghostCellTopRight, numVars);

        surface = new Surface(1, new Point(-1, 0.0, 0.0), new Vector(-1, 0, 0));
        Face f4 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cell0, ghostCellLeft, numVars);

        surface = new Surface(1, new Point(0.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f5 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cell0, cell1, numVars);

        surface = new Surface(1, new Point(1.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f6 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cell1, ghostCellRight, numVars);

        cell0.faces.addAll(List.of(f0, f4, f2, f5));
        cell1.faces.addAll(List.of(f1, f5, f3, f6));
        ghostCellLeft.faces.add(f4);
        ghostCellRight.faces.add(f6);
        ghostCellBottomLeft.faces.add(f0);
        ghostCellBottomRight.faces.add(f1);
        ghostCellTopLeft.faces.add(f2);
        ghostCellTopRight.faces.add(f3);

        n0.neighbors.addAll(List.of(cell0, ghostCellLeft, ghostCellBottomLeft));
        n1.neighbors.addAll(List.of(cell0, ghostCellLeft, ghostCellTopLeft));
        n2.neighbors.addAll(List.of(cell0, cell1, ghostCellBottomLeft, ghostCellBottomRight));
        n3.neighbors.addAll(List.of(cell0, cell1, ghostCellTopLeft, ghostCellTopRight));
        n4.neighbors.addAll(List.of(cell1, ghostCellBottomRight, ghostCellRight));
        n5.neighbors.addAll(List.of(cell1, ghostCellTopRight, ghostCellRight));

        // Check cells
        List<Cell> actualCells = mesh.cells();
        List<Cell> expectedCells = List.of(cell0, cell1);
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i), 1e-12);
        }

        // Check nodes
        List<Node> actualNodes = mesh.nodes();
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5);
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i), 1e-12);
        }

        // Check internal faces
        List<Face> actualInternalFaces = mesh.internalFaces();
        List<Face> expectedInternalFaces = List.of(f5);
        assertFaceListEquals(expectedInternalFaces, actualInternalFaces, 1e-12);

        // Check boundaries
        List<Boundary> actualBoundaries = mesh.boundaries();
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f4), bc),
                new Boundary("xi max", List.of(f6), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f2, f3), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, actualBoundaries, 1e-12);
    }

    @Test
    public void mesh_with_two_cells_in_x_direction_with_reversed_x_coordinate() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 2\n" +
                    "1.0        -0.5       0.0       \n" +
                    "1.0        0.5        0.0       \n" +
                    "0.0        -0.5       0.0       \n" +
                    "0.0        0.5        0.0       \n" +
                    "-1.0       -0.5       0.0       \n" +
                    "-1.0       0.5        0.0       \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(1.0, -0.5, 0.0);
        Node n1 = new Node(1.0, 0.5, 0.0);
        Node n2 = new Node(0.0, -0.5, 0.0);
        Node n3 = new Node(0.0, 0.5, 0.0);
        Node n4 = new Node(-1.0, -0.5, 0.0);
        Node n5 = new Node(-1.0, 0.5, 0.0);

        Shape shape0 = new Shape(1, new Point(-0.5, 0, 0));
        Cell cellLeft = new Cell(1, new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, shape0, numVars);

        Shape shape1 = new Shape(1, new Point(0.5, 0, 0));
        Cell cellRight = new Cell(0, new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, shape1, numVars);

        Shape shape = new Shape(1, new Point(-1.5, 0, 0));
        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n4, n5, new Node(-2, -0.5, 0), new Node(-2, 0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(1.5, 0, 0));
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n0, n1, new Node(2, 0.5, 0), new Node(2, -0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, 1, 0));
        Cell ghostCellTopLeft = new Cell(-1, new Node[]{
                n5, n3, new Node(0, 1.5, 0), new Node(-1, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, 1, 0));
        Cell ghostCellTopRight = new Cell(-1, new Node[]{
                n3, n1, new Node(1, 1.5, 0), new Node(0, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, -1, 0));
        Cell ghostCellBottomLeft = new Cell(-1, new Node[]{
                n4, n2, new Node(0, -1.5, 0), new Node(-1, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, -1, 0));
        Cell ghostCellBottomRight = new Cell(-1, new Node[]{
                n2, n0, new Node(1, -1.5, 0), new Node(0, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        Surface surface = new Surface(1, new Point(-0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n4, n2}, VTKType.VTK_LINE, surface, cellLeft, ghostCellBottomLeft, numVars);

        surface = new Surface(1, new Point(0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f1 = new Face(new Node[]{n2, n0}, VTKType.VTK_LINE, surface, cellRight, ghostCellBottomRight, numVars);

        surface = new Surface(1, new Point(-0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n5, n3}, VTKType.VTK_LINE, surface, cellLeft, ghostCellTopLeft, numVars);

        surface = new Surface(1, new Point(0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f3 = new Face(new Node[]{n3, n1}, VTKType.VTK_LINE, surface, cellRight, ghostCellTopRight, numVars);

        surface = new Surface(1, new Point(-1, 0.0, 0.0), new Vector(-1, 0, 0));
        Face f4 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cellLeft, ghostCellLeft, numVars);

        surface = new Surface(1, new Point(0.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f5 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cellLeft, cellRight, numVars);

        surface = new Surface(1, new Point(1.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f6 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cellRight, ghostCellRight, numVars);

        cellLeft.faces.addAll(List.of(f0, f4, f2, f5));
        cellRight.faces.addAll(List.of(f1, f5, f3, f6));
        ghostCellLeft.faces.add(f4);
        ghostCellRight.faces.add(f6);
        ghostCellBottomLeft.faces.add(f0);
        ghostCellBottomRight.faces.add(f1);
        ghostCellTopLeft.faces.add(f2);
        ghostCellTopRight.faces.add(f3);

        n0.neighbors.addAll(List.of(cellRight, ghostCellRight, ghostCellBottomRight));
        n1.neighbors.addAll(List.of(cellRight, ghostCellRight, ghostCellTopRight));
        n2.neighbors.addAll(List.of(cellLeft, cellRight, ghostCellBottomLeft, ghostCellBottomRight));
        n3.neighbors.addAll(List.of(cellLeft, cellRight, ghostCellTopLeft, ghostCellTopRight));
        n4.neighbors.addAll(List.of(cellLeft, ghostCellBottomLeft, ghostCellLeft));
        n5.neighbors.addAll(List.of(cellLeft, ghostCellTopLeft, ghostCellLeft));

        // Check cells
        List<Cell> actualCells = mesh.cells();
        List<Cell> expectedCells = List.of(cellRight, cellLeft);
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i), 1e-12);
        }

        // Check nodes
        List<Node> actualNodes = mesh.nodes();
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5);
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i), 1e-12);
        }

        // Check internal faces
        List<Face> actualInternalFaces = mesh.internalFaces();
        List<Face> expectedInternalFaces = List.of(f5);
        assertFaceListEquals(expectedInternalFaces, actualInternalFaces, 1e-12);

        // Check boundaries
        List<Boundary> actualBoundaries = mesh.boundaries();
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6), bc),
                new Boundary("xi max", List.of(f4), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f2, f3), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, actualBoundaries, 1e-12);
    }

    @Test
    public void mesh_with_two_cells_in_x_direction_with_reversed_y_coordinate() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 2\n" +
                    "-1.0       0.5       0.0       \n" +
                    "-1.0       -0.5      0.0       \n" +
                    "0.0        0.5       0.0       \n" +
                    "0.0        -0.5      0.0       \n" +
                    "1.0        0.5       0.0       \n" +
                    "1.0        -0.5      0.0       \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(-1.0, 0.5, 0.0);
        Node n1 = new Node(-1.0, -0.5, 0.0);
        Node n2 = new Node(0.0, 0.5, 0.0);
        Node n3 = new Node(0.0, -0.5, 0.0);
        Node n4 = new Node(1.0, 0.5, 0.0);
        Node n5 = new Node(1.0, -0.5, 0.0);

        Shape shape0 = new Shape(1, new Point(-0.5, 0, 0));
        Cell cell0 = new Cell(0, new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, shape0, numVars);

        Shape shape1 = new Shape(1, new Point(0.5, 0, 0));
        Cell cell1 = new Cell(1, new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, shape1, numVars);

        Shape shape = new Shape(1, new Point(-1.5, 0, 0));
        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n0, n1, new Node(-2, -0.5, 0), new Node(-2, 0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(1.5, 0, 0));
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n4, n5, new Node(2, 0.5, 0), new Node(2, -0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, 1, 0));
        Cell ghostCellTopLeft = new Cell(-1, new Node[]{
                n0, n2, new Node(0, 1.5, 0), new Node(-1, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, 1, 0));
        Cell ghostCellTopRight = new Cell(-1, new Node[]{
                n2, n4, new Node(1, 1.5, 0), new Node(0, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, -1, 0));
        Cell ghostCellBottomLeft = new Cell(-1, new Node[]{
                n1, n3, new Node(0, -1.5, 0), new Node(-1, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, -1, 0));
        Cell ghostCellBottomRight = new Cell(-1, new Node[]{
                n3, n5, new Node(1, -1.5, 0), new Node(0, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        Surface surface = new Surface(1, new Point(-0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n1, n3}, VTKType.VTK_LINE, surface, cell0, ghostCellBottomLeft, numVars);

        surface = new Surface(1, new Point(0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f1 = new Face(new Node[]{n3, n5}, VTKType.VTK_LINE, surface, cell1, ghostCellBottomRight, numVars);

        surface = new Surface(1, new Point(-0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n0, n2}, VTKType.VTK_LINE, surface, cell0, ghostCellTopLeft, numVars);

        surface = new Surface(1, new Point(0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f3 = new Face(new Node[]{n2, n4}, VTKType.VTK_LINE, surface, cell1, ghostCellTopRight, numVars);

        surface = new Surface(1, new Point(-1, 0.0, 0.0), new Vector(-1, 0, 0));
        Face f4 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cell0, ghostCellLeft, numVars);

        surface = new Surface(1, new Point(0.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f5 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cell0, cell1, numVars);

        surface = new Surface(1, new Point(1.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f6 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cell1, ghostCellRight, numVars);

        cell0.faces.addAll(List.of(f0, f4, f2, f5));
        cell1.faces.addAll(List.of(f1, f5, f3, f6));
        ghostCellLeft.faces.add(f4);
        ghostCellRight.faces.add(f6);
        ghostCellBottomLeft.faces.add(f0);
        ghostCellBottomRight.faces.add(f1);
        ghostCellTopLeft.faces.add(f2);
        ghostCellTopRight.faces.add(f3);

        n0.neighbors.addAll(List.of(cell0, ghostCellLeft, ghostCellTopLeft));
        n1.neighbors.addAll(List.of(cell0, ghostCellLeft, ghostCellBottomLeft));
        n2.neighbors.addAll(List.of(cell0, cell1, ghostCellTopLeft, ghostCellTopRight));
        n3.neighbors.addAll(List.of(cell0, cell1, ghostCellBottomLeft, ghostCellBottomRight));
        n4.neighbors.addAll(List.of(cell1, ghostCellTopRight, ghostCellRight));
        n5.neighbors.addAll(List.of(cell1, ghostCellBottomRight, ghostCellRight));

        // Check cells
        List<Cell> actualCells = mesh.cells();
        List<Cell> expectedCells = List.of(cell0, cell1);
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i), 1e-12);
        }

        // Check nodes
        List<Node> actualNodes = mesh.nodes();
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5);
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i), 1e-12);
        }

        // Check internal faces
        List<Face> actualInternalFaces = mesh.internalFaces();
        List<Face> expectedInternalFaces = List.of(f5);
        assertFaceListEquals(expectedInternalFaces, actualInternalFaces, 1e-12);

        // Check boundaries
        List<Boundary> actualBoundaries = mesh.boundaries();
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f4), bc),
                new Boundary("xi max", List.of(f6), bc),
                new Boundary("eta min", List.of(f2, f3), bc),
                new Boundary("eta max", List.of(f0, f1), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, actualBoundaries, 1e-12);
    }

    @Test
    public void mesh_with_two_cells_in_x_direction_with_reversed_x_and_y_coordinate() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 2\n" +
                    "1.0        0.5        0.0       \n" +
                    "1.0        -0.5       0.0       \n" +
                    "0.0        0.5        0.0       \n" +
                    "0.0        -0.5       0.0       \n" +
                    "-1.0       0.5        0.0       \n" +
                    "-1.0       -0.5       0.0       \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(1.0, 0.5, 0.0);
        Node n1 = new Node(1.0, -0.5, 0.0);
        Node n2 = new Node(0.0, 0.5, 0.0);
        Node n3 = new Node(0.0, -0.5, 0.0);
        Node n4 = new Node(-1.0, 0.5, 0.0);
        Node n5 = new Node(-1.0, -0.5, 0.0);

        Shape shapeLeft = new Shape(1, new Point(-0.5, 0, 0));
        Cell cellLeft = new Cell(1, new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, shapeLeft, numVars);

        Shape shapeRight = new Shape(1, new Point(0.5, 0, 0));
        Cell cellRight = new Cell(0, new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, shapeRight, numVars);

        Shape shape = new Shape(1, new Point(-1.5, 0, 0));
        Cell ghostCellLeft = new Cell(-1, new Node[]{
                n4, n5, new Node(-2, -0.5, 0), new Node(-2, 0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(1.5, 0, 0));
        Cell ghostCellRight = new Cell(-1, new Node[]{
                n0, n1, new Node(2, 0.5, 0), new Node(2, -0.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, 1, 0));
        Cell ghostCellTopLeft = new Cell(-1, new Node[]{
                n4, n2, new Node(0, 1.5, 0), new Node(-1, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, 1, 0));
        Cell ghostCellTopRight = new Cell(-1, new Node[]{
                n2, n0, new Node(1, 1.5, 0), new Node(0, 1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(-0.5, -1, 0));
        Cell ghostCellBottomLeft = new Cell(-1, new Node[]{
                n5, n3, new Node(0, -1.5, 0), new Node(-1, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        shape = new Shape(1, new Point(0.5, -1, 0));
        Cell ghostCellBottomRight = new Cell(-1, new Node[]{
                n3, n1, new Node(1, -1.5, 0), new Node(0, -1.5, 0)},
                VTKType.VTK_QUAD, shape, numVars
        );

        Surface surface = new Surface(1, new Point(-0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n5, n3}, VTKType.VTK_LINE, surface, cellLeft, ghostCellBottomLeft, numVars);

        surface = new Surface(1, new Point(0.5, -0.5, 0.0), new Vector(0, -1, 0));
        Face f1 = new Face(new Node[]{n3, n1}, VTKType.VTK_LINE, surface, cellRight, ghostCellBottomRight, numVars);

        surface = new Surface(1, new Point(-0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n4, n2}, VTKType.VTK_LINE, surface, cellLeft, ghostCellTopLeft, numVars);

        surface = new Surface(1, new Point(0.5, 0.5, 0.0), new Vector(0, 1, 0));
        Face f3 = new Face(new Node[]{n2, n0}, VTKType.VTK_LINE, surface, cellRight, ghostCellTopRight, numVars);

        surface = new Surface(1, new Point(-1, 0.0, 0.0), new Vector(-1, 0, 0));
        Face f4 = new Face(new Node[]{n5, n4}, VTKType.VTK_LINE, surface, cellLeft, ghostCellLeft, numVars);

        surface = new Surface(1, new Point(0.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f5 = new Face(new Node[]{n2, n3}, VTKType.VTK_LINE, surface, cellLeft, cellRight, numVars);

        surface = new Surface(1, new Point(1.0, 0.0, 0.0), new Vector(1, 0, 0));
        Face f6 = new Face(new Node[]{n1, n0}, VTKType.VTK_LINE, surface, cellRight, ghostCellRight, numVars);

        cellLeft.faces.addAll(List.of(f0, f4, f2, f5));
        cellRight.faces.addAll(List.of(f1, f5, f3, f6));
        ghostCellLeft.faces.add(f4);
        ghostCellRight.faces.add(f6);
        ghostCellBottomLeft.faces.add(f0);
        ghostCellBottomRight.faces.add(f1);
        ghostCellTopLeft.faces.add(f2);
        ghostCellTopRight.faces.add(f3);

        n0.neighbors.addAll(List.of(cellRight, ghostCellRight, ghostCellTopRight));
        n1.neighbors.addAll(List.of(cellRight, ghostCellRight, ghostCellBottomRight));
        n2.neighbors.addAll(List.of(cellLeft, cellRight, ghostCellTopLeft, ghostCellTopRight));
        n3.neighbors.addAll(List.of(cellLeft, cellRight, ghostCellBottomLeft, ghostCellBottomRight));
        n4.neighbors.addAll(List.of(cellLeft, ghostCellTopLeft, ghostCellLeft));
        n5.neighbors.addAll(List.of(cellLeft, ghostCellBottomLeft, ghostCellLeft));

        // Check cells
        List<Cell> actualCells = mesh.cells();
        List<Cell> expectedCells = List.of(cellRight, cellLeft);
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i), 1e-12);
        }

        // Check nodes
        List<Node> actualNodes = mesh.nodes();
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5);
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i), 1e-12);
        }

        // Check internal faces
        List<Face> actualInternalFaces = mesh.internalFaces();
        List<Face> expectedInternalFaces = List.of(f5);
        assertFaceListEquals(expectedInternalFaces, actualInternalFaces, 1e-12);

        // Check boundaries
        List<Boundary> actualBoundaries = mesh.boundaries();
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6), bc),
                new Boundary("xi max", List.of(f4), bc),
                new Boundary("eta min", List.of(f2, f3), bc),
                new Boundary("eta max", List.of(f0, f1), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, actualBoundaries, 1e-12);
    }

    @Test
    public void mesh_with_two_cells_each_in_x_and_y_direction() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 3\n" +
                    "2.00       1.00       0.00\n" +
                    "2.00       2.00       0.00\n" +
                    "2.00       3.00       0.00\n" +
                    "3.50       1.00       0.00\n" +
                    "3.50       2.00       0.00\n" +
                    "3.50       3.00       0.00\n" +
                    "5.00       1.00       0.00\n" +
                    "5.00       2.00       0.00\n" +
                    "5.00       3.00       0.00\n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(2.00, 1.00, 0.00);
        Node n1 = new Node(2.00, 2.00, 0.00);
        Node n2 = new Node(2.00, 3.00, 0.00);
        Node n3 = new Node(3.50, 1.00, 0.00);
        Node n4 = new Node(3.50, 2.00, 0.00);
        Node n5 = new Node(3.50, 3.00, 0.00);
        Node n6 = new Node(5.00, 1.00, 0.00);
        Node n7 = new Node(5.00, 2.00, 0.00);
        Node n8 = new Node(5.00, 3.00, 0.00);

        Shape shape = new Shape(1.5, new Point(2.75, 1.5, 0));
        Cell cellLowerLeft = new Cell(0, new Node[]{n0, n1, n4, n3}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 2.5, 0));
        Cell cellUpperLeft = new Cell(1, new Node[]{n1, n2, n5, n4}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 1.5, 0));
        Cell cellLowerRight = new Cell(2, new Node[]{n3, n4, n7, n6}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 2.5, 0));
        Cell cellUpperRight = new Cell(3, new Node[]{n4, n5, n8, n7}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(1.25, 1.5, 0));
        Cell ghostCellLeft0 = new Cell(-1, new Node[]{
                n0, n1, new Node(0.5, 2, 0), new Node(0.5, 1, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(1.25, 2.5, 0));
        Cell ghostCellLeft1 = new Cell(-1, new Node[]{
                n1, n2, new Node(0.5, 3, 0), new Node(0.5, 2, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(5.75, 1.5, 0));
        Cell ghostCellRight0 = new Cell(-1, new Node[]{
                n6, n7, new Node(6.5, 2, 0), new Node(6.5, 1, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(5.75, 2.5, 0));
        Cell ghostCellRight1 = new Cell(-1, new Node[]{
                n7, n8, new Node(6.5, 3, 0), new Node(6.5, 2, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 0.5, 0));
        Cell ghostCellBottom0 = new Cell(-1, new Node[]{
                n0, n3, new Node(3.5, 0, 0), new Node(2.0, 0, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 0.5, 0));
        Cell ghostCellBottom1 = new Cell(-1, new Node[]{
                n3, n6, new Node(5.0, 0, 0), new Node(3.5, 0, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 3.5, 0));
        Cell ghostCellTop0 = new Cell(-1, new Node[]{
                n2, n5, new Node(3.5, 4, 0), new Node(2.0, 4, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 3.5, 0));
        Cell ghostCellTop1 = new Cell(-1, new Node[]{
                n5, n8, new Node(5.0, 4.0, 0), new Node(3.5, 4.0, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        Surface surface = new Surface(1.5, new Point(2.75, 1, 0), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n0, n3}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellBottom0, numVars);

        surface = new Surface(1.5, new Point(4.25, 1, 0), new Vector(0, -1, 0));
        Face f1 = new Face(new Node[]{n3, n6}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellBottom1, numVars);

        surface = new Surface(1.5, new Point(2.75, 2, 0), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n1, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellUpperLeft, numVars);

        surface = new Surface(1.5, new Point(4.25, 2, 0), new Vector(0, 1, 0));
        Face f3 = new Face(new Node[]{n4, n7}, VTKType.VTK_LINE, surface, cellLowerRight, cellUpperRight, numVars);

        surface = new Surface(1.5, new Point(2.75, 3, 0), new Vector(0, 1, 0));
        Face f4 = new Face(new Node[]{n2, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellTop0, numVars);

        surface = new Surface(1.5, new Point(4.25, 3, 0), new Vector(0, 1, 0));
        Face f5 = new Face(new Node[]{n5, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellTop1, numVars);

        surface = new Surface(1.0, new Point(2.0, 1.5, 0), new Vector(-1, 0, 0));
        Face f6 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellLeft0, numVars);

        surface = new Surface(1.0, new Point(3.5, 1.5, 0), new Vector(1, 0, 0));
        Face f7 = new Face(new Node[]{n3, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellLowerRight, numVars);

        surface = new Surface(1.0, new Point(5.0, 1.5, 0), new Vector(1, 0, 0));
        Face f8 = new Face(new Node[]{n6, n7}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellRight0, numVars);

        surface = new Surface(1.0, new Point(2.0, 2.5, 0), new Vector(-1, 0, 0));
        Face f9 = new Face(new Node[]{n1, n2}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellLeft1, numVars);

        surface = new Surface(1.0, new Point(3.5, 2.5, 0), new Vector(1, 0, 0));
        Face f10 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, cellUpperRight, numVars);

        surface = new Surface(1.0, new Point(5.0, 2.5, 0), new Vector(1, 0, 0));
        Face f11 = new Face(new Node[]{n7, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellRight1, numVars);

        cellLowerLeft.faces.addAll(List.of(f0, f6, f2, f7));
        cellLowerRight.faces.addAll(List.of(f1, f7, f3, f8));
        cellUpperLeft.faces.addAll(List.of(f2, f9, f4, f10));
        cellUpperRight.faces.addAll(List.of(f3, f10, f5, f11));
        ghostCellBottom0.faces.add(f0);
        ghostCellBottom1.faces.add(f1);
        ghostCellLeft0.faces.add(f6);
        ghostCellLeft1.faces.add(f9);
        ghostCellTop0.faces.add(f4);
        ghostCellTop1.faces.add(f5);
        ghostCellRight0.faces.add(f8);
        ghostCellRight1.faces.add(f11);

        n0.neighbors.addAll(List.of(cellLowerLeft, ghostCellLeft0, ghostCellBottom0));
        n1.neighbors.addAll(List.of(cellLowerLeft, cellUpperLeft, ghostCellLeft0, ghostCellLeft1));
        n2.neighbors.addAll(List.of(cellUpperLeft, ghostCellLeft1, ghostCellTop0));
        n3.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, ghostCellBottom0, ghostCellBottom1));
        n4.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, cellUpperLeft, cellUpperRight));
        n5.neighbors.addAll(List.of(cellUpperLeft, cellUpperRight, ghostCellTop0, ghostCellTop1));
        n6.neighbors.addAll(List.of(cellLowerRight, ghostCellBottom1, ghostCellRight0));
        n7.neighbors.addAll(List.of(cellLowerRight, cellUpperRight, ghostCellRight0, ghostCellRight1));
        n8.neighbors.addAll(List.of(cellUpperRight, ghostCellRight1, ghostCellTop1));

        // Check cells
        List<Cell> expectedCells = List.of(cellLowerLeft, cellUpperLeft, cellLowerRight, cellUpperRight);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-12);
        }

        // Check nodes
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-12);
        }

        // Check internal faces
        List<Face> expectedInternalFaces = List.of(f2, f3, f7, f10);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-12);

        // Check boundaries
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6, f9), bc),
                new Boundary("xi max", List.of(f8, f11), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f4, f5), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-12);
    }

    @Test
    public void mesh_with_two_cells_each_in_x_and_z_direction() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 3\n" +
                    "2.00       0.00       1.00\n" +
                    "2.00       0.00       2.00\n" +
                    "2.00       0.00       3.00\n" +
                    "3.50       0.00       1.00\n" +
                    "3.50       0.00       2.00\n" +
                    "3.50       0.00       3.00\n" +
                    "5.00       0.00       1.00\n" +
                    "5.00       0.00       2.00\n" +
                    "5.00       0.00       3.00\n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(2.00, 0.00, 1.00);
        Node n1 = new Node(2.00, 0.00, 2.00);
        Node n2 = new Node(2.00, 0.00, 3.00);
        Node n3 = new Node(3.50, 0.00, 1.00);
        Node n4 = new Node(3.50, 0.00, 2.00);
        Node n5 = new Node(3.50, 0.00, 3.00);
        Node n6 = new Node(5.00, 0.00, 1.00);
        Node n7 = new Node(5.00, 0.00, 2.00);
        Node n8 = new Node(5.00, 0.00, 3.00);

        Shape shape = new Shape(1.5, new Point(2.75, 0, 1.5));
        Cell cellLowerLeft = new Cell(0, new Node[]{n0, n1, n4, n3}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 0, 2.5));
        Cell cellUpperLeft = new Cell(1, new Node[]{n1, n2, n5, n4}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 0, 1.5));
        Cell cellLowerRight = new Cell(2, new Node[]{n3, n4, n7, n6}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 0, 2.5));
        Cell cellUpperRight = new Cell(3, new Node[]{n4, n5, n8, n7}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(1.25, 0, 1.5));
        Cell ghostCellLeft0 = new Cell(-1, new Node[]{
                n0, n1, new Node(0.5, 0, 2), new Node(0.5, 0, 1)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(1.25, 0, 2.5));
        Cell ghostCellLeft1 = new Cell(-1, new Node[]{
                n1, n2, new Node(0.5, 0, 3), new Node(0.5, 0, 2)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(5.75, 0, 1.5));
        Cell ghostCellRight0 = new Cell(-1, new Node[]{
                n6, n7, new Node(6.5, 0, 2), new Node(6.5, 0, 1)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(5.75, 0, 2.5));
        Cell ghostCellRight1 = new Cell(-1, new Node[]{
                n7, n8, new Node(6.5, 0, 3), new Node(6.5, 0, 2)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 0, 0.5));
        Cell ghostCellBottom0 = new Cell(-1, new Node[]{
                n0, n3, new Node(3.5, 0, 0), new Node(2.0, 0, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 0, 0.5));
        Cell ghostCellBottom1 = new Cell(-1, new Node[]{
                n3, n6, new Node(5.0, 0, 0), new Node(3.5, 0, 0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(2.75, 0, 3.5));
        Cell ghostCellTop0 = new Cell(-1, new Node[]{
                n2, n5, new Node(3.5, 0, 4), new Node(2.0, 0, 4)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(1.5, new Point(4.25, 0, 3.5));
        Cell ghostCellTop1 = new Cell(-1, new Node[]{
                n5, n8, new Node(5.0, 0, 4.0), new Node(3.5, 0, 4.0)},
                VTKType.VTK_QUAD, shape, numVars);

        Surface surface = new Surface(1.5, new Point(2.75, 0, 1), new Vector(0, 0, -1));
        Face f0 = new Face(new Node[]{n0, n3}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellBottom0, numVars);

        surface = new Surface(1.5, new Point(4.25, 0, 1), new Vector(0, 0, -1));
        Face f1 = new Face(new Node[]{n3, n6}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellBottom1, numVars);

        surface = new Surface(1.5, new Point(2.75, 0, 2), new Vector(0, 0, 1));
        Face f2 = new Face(new Node[]{n1, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellUpperLeft, numVars);

        surface = new Surface(1.5, new Point(4.25, 0, 2), new Vector(0, 0, 1));
        Face f3 = new Face(new Node[]{n4, n7}, VTKType.VTK_LINE, surface, cellLowerRight, cellUpperRight, numVars);

        surface = new Surface(1.5, new Point(2.75, 0, 3), new Vector(0, 0, 1));
        Face f4 = new Face(new Node[]{n2, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellTop0, numVars);

        surface = new Surface(1.5, new Point(4.25, 0, 3), new Vector(0, 0, 1));
        Face f5 = new Face(new Node[]{n5, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellTop1, numVars);

        surface = new Surface(1.0, new Point(2.0, 0, 1.5), new Vector(-1, 0, 0));
        Face f6 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellLeft0, numVars);

        surface = new Surface(1.0, new Point(3.5, 0, 1.5), new Vector(1, 0, 0));
        Face f7 = new Face(new Node[]{n3, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellLowerRight, numVars);

        surface = new Surface(1.0, new Point(5.0, 0, 1.5), new Vector(1, 0, 0));
        Face f8 = new Face(new Node[]{n6, n7}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellRight0, numVars);

        surface = new Surface(1.0, new Point(2.0, 0, 2.5), new Vector(-1, 0, 0));
        Face f9 = new Face(new Node[]{n1, n2}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellLeft1, numVars);

        surface = new Surface(1.0, new Point(3.5, 0, 2.5), new Vector(1, 0, 0));
        Face f10 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, cellUpperRight, numVars);

        surface = new Surface(1.0, new Point(5.0, 0, 2.5), new Vector(1, 0, 0));
        Face f11 = new Face(new Node[]{n7, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellRight1, numVars);

        cellLowerLeft.faces.addAll(List.of(f0, f6, f2, f7));
        cellLowerRight.faces.addAll(List.of(f1, f7, f3, f8));
        cellUpperLeft.faces.addAll(List.of(f2, f9, f4, f10));
        cellUpperRight.faces.addAll(List.of(f3, f10, f5, f11));
        ghostCellBottom0.faces.add(f0);
        ghostCellBottom1.faces.add(f1);
        ghostCellLeft0.faces.add(f6);
        ghostCellLeft1.faces.add(f9);
        ghostCellTop0.faces.add(f4);
        ghostCellTop1.faces.add(f5);
        ghostCellRight0.faces.add(f8);
        ghostCellRight1.faces.add(f11);

        n0.neighbors.addAll(List.of(cellLowerLeft, ghostCellLeft0, ghostCellBottom0));
        n1.neighbors.addAll(List.of(cellLowerLeft, cellUpperLeft, ghostCellLeft0, ghostCellLeft1));
        n2.neighbors.addAll(List.of(cellUpperLeft, ghostCellLeft1, ghostCellTop0));
        n3.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, ghostCellBottom0, ghostCellBottom1));
        n4.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, cellUpperLeft, cellUpperRight));
        n5.neighbors.addAll(List.of(cellUpperLeft, cellUpperRight, ghostCellTop0, ghostCellTop1));
        n6.neighbors.addAll(List.of(cellLowerRight, ghostCellBottom1, ghostCellRight0));
        n7.neighbors.addAll(List.of(cellLowerRight, cellUpperRight, ghostCellRight0, ghostCellRight1));
        n8.neighbors.addAll(List.of(cellUpperRight, ghostCellRight1, ghostCellTop1));

        // Check cells
        List<Cell> expectedCells = List.of(cellLowerLeft, cellUpperLeft, cellLowerRight, cellUpperRight);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-12);
        }

        // Check nodes
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-12);
        }

        // Check internal faces
        List<Face> expectedInternalFaces = List.of(f2, f3, f7, f10);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-12);

        // Check boundaries
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6, f9), bc),
                new Boundary("xi max", List.of(f8, f11), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f4, f5), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-12);
    }

    @Test
    public void mesh_with_nonuniform_2_by_2_cells_x_y() throws IOException {
        // Geometry created and calculated using Solidworks software for testing
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 3\n" +
                    "-32.0        -11.0        20.0\n" +
                    "-36.5        -1.0         20.0\n" +
                    "-40.0        30.0         20.0\n" +
                    "-10.0        -6.0         20.0\n" +
                    "-12.0        10.0         20.0\n" +
                    "-12.0        40.0         20.0\n" +
                    "19.0         -8.0         20.0\n" +
                    "12.0         15.5         20.0\n" +
                    "11.5         34.0         20.0\n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(-32.0, -11.0, 20.0);
        Node n1 = new Node(-36.5, -1.0, 20.0);
        Node n2 = new Node(-40.0, 30.0, 20.0);
        Node n3 = new Node(-10.0, -6.0, 20.0);
        Node n4 = new Node(-12.0, 10.0, 20.0);
        Node n5 = new Node(-12.0, 40.0, 20.0);
        Node n6 = new Node(19.0, -8.0, 20.0);
        Node n7 = new Node(12.0, 15.5, 20.0);
        Node n8 = new Node(11.5, 34.0, 20.0);

        Shape shape = new Shape(328.25, new Point(-21.96255395, -1.58568164, 20.0));
        Cell cellLowerLeft = new Cell(0, new Node[]{n0, n1, n4, n3}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-25.31196581, 20.00854701, 20.0));
        Cell cellUpperLeft = new Cell(1, new Node[]{n1, n2, n5, n4}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(3.15843137, 2.73058824, 20.0));
        Cell cellLowerRight = new Cell(2, new Node[]{n3, n4, n7, n6}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(-1.06356269, 24.83224803, 20.0));
        Cell cellUpperRight = new Cell(3, new Node[]{n4, n5, n8, n7}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(328.25, new Point(-45.70291039, -12.26884204, 20.0));
        Cell ghostCellLeft0 = new Cell(-1, new Node[]{
                n0, n1, new Node(-50.33264033, -24.14968815, 20.0), new Node(-60.98128898, -12.04158004, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-52.09054933, 16.98515855, 20.0));
        Cell ghostCellLeft1 = new Cell(-1, new Node[]{
                n1, n2, new Node(-62.83585923, 4.26046751, 20.0), new Node(-69.52478808, 33.50526586, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(26.38778525, 9.64997024, 20.0));
        Cell ghostCellRight0 = new Cell(-1, new Node[]{
                n6, n7, new Node(42.17879418, 9.54261954, 20.0), new Node(35.09771310, 24.02910603, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(24.54041417, 25.52424740, 20.0));
        Cell ghostCellRight1 = new Cell(-1, new Node[]{
                n7, n8, new Node(34.64160584, 41.26058394, 20.0), new Node(36.26204380, 11.30437956, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(328.25, new Point(-18.87949356, -15.15114733, 20.0));
        Cell ghostCellBottom0 = new Cell(-1, new Node[]{
                n0, n3, new Node(-31.73575638, -21.96267191, 20.0), new Node(-4.88801572, -21.29273084, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(1.83533589, -16.45429632, 20.0));
        Cell ghostCellBottom1 = new Cell(-1, new Node[]{
                n3, n6, new Node(-14.17751479, -21.57396450, 20), new Node(8.84023669, -30.31656805, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-34.96447770, 47.03558031, 20.0));
        Cell ghostCellTop0 = new Cell(-1, new Node[]{
                n2, n5, new Node(-56.92986425, 56.20361991, 20.0), new Node(-31.00452489, 63.21266968, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(4.86908936, 48.06846857, 20.0));
        Cell ghostCellTop1 = new Cell(-1, new Node[]{
                n5, n8, new Node(2.38164046, 66.32809180, 20.0), new Node(20.80747981, 49.99596260, 20.0)},
                VTKType.VTK_QUAD, shape, numVars);

        Surface surface = new Surface(22.56102835, new Point(-21, -8.5, 20.0), new Vector(0.22162110, -0.97513286, 0.0));
        Face f0 = new Face(new Node[]{n0, n3}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellBottom0, numVars);

        surface = new Surface(29.06888371, new Point(4.5, -7.0, 20.0), new Vector(-0.06880209, -0.99763033, 0.0));
        Face f1 = new Face(new Node[]{n3, n6}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellBottom1, numVars);

        surface = new Surface(26.85609800, new Point(-24.25, 4.5, 20.0), new Vector(-0.40959040, 0.91226953, 0.0));
        Face f2 = new Face(new Node[]{n1, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellUpperLeft, numVars);

        surface = new Surface(24.62214450, new Point(0.0, 12.75, 20.0), new Vector(-0.22337616, 0.97473232, 0.0));
        Face f3 = new Face(new Node[]{n4, n7}, VTKType.VTK_LINE, surface, cellLowerRight, cellUpperRight, numVars);

        surface = new Surface(29.73213749, new Point(-26.0, 35.0, 20.0), new Vector(-0.33633640, 0.94174191, 0.0));
        Face f4 = new Face(new Node[]{n2, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellTop0, numVars);

        surface = new Surface(24.25386567, new Point(-0.25, 37.0, 20.0), new Vector(0.24738325, 0.96891771, 0.0));
        Face f5 = new Face(new Node[]{n5, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellTop1, numVars);

        surface = new Surface(10.96585610, new Point(-34.25, -6.0, 20.0), new Vector(-0.91192151, -0.41036468, 0.0));
        Face f6 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellLeft0, numVars);

        surface = new Surface(16.12451550, new Point(-11.0, 2.0, 20.0), new Vector(0.99227788, 0.12403473, 0.0));
        Face f7 = new Face(new Node[]{n3, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellLowerRight, numVars);

        surface = new Surface(24.52039967, new Point(15.5, 3.75, 20.0), new Vector(0.95838568, 0.28547659, 0.0));
        Face f8 = new Face(new Node[]{n6, n7}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellRight0, numVars);

        surface = new Surface(31.19695498, new Point(-38.25, 14.5, 20.0), new Vector(-0.99368672, -0.11219044, 0.0));
        Face f9 = new Face(new Node[]{n1, n2}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellLeft1, numVars);

        surface = new Surface(30.0, new Point(-12.0, 25.0, 20.0), new Vector(1, 0, 0));
        Face f10 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, cellUpperRight, numVars);

        surface = new Surface(18.50675552, new Point(11.75, 24.75, 20.0), new Vector(0.99963497, 0.02701716, 0.0));
        Face f11 = new Face(new Node[]{n7, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellRight1, numVars);

        cellLowerLeft.faces.addAll(List.of(f0, f6, f2, f7));
        cellLowerRight.faces.addAll(List.of(f1, f7, f3, f8));
        cellUpperLeft.faces.addAll(List.of(f2, f9, f4, f10));
        cellUpperRight.faces.addAll(List.of(f3, f10, f5, f11));
        ghostCellBottom0.faces.add(f0);
        ghostCellBottom1.faces.add(f1);
        ghostCellLeft0.faces.add(f6);
        ghostCellLeft1.faces.add(f9);
        ghostCellTop0.faces.add(f4);
        ghostCellTop1.faces.add(f5);
        ghostCellRight0.faces.add(f8);
        ghostCellRight1.faces.add(f11);

        n0.neighbors.addAll(List.of(cellLowerLeft, ghostCellLeft0, ghostCellBottom0));
        n1.neighbors.addAll(List.of(cellLowerLeft, cellUpperLeft, ghostCellLeft0, ghostCellLeft1));
        n2.neighbors.addAll(List.of(cellUpperLeft, ghostCellLeft1, ghostCellTop0));
        n3.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, ghostCellBottom0, ghostCellBottom1));
        n4.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, cellUpperLeft, cellUpperRight));
        n5.neighbors.addAll(List.of(cellUpperLeft, cellUpperRight, ghostCellTop0, ghostCellTop1));
        n6.neighbors.addAll(List.of(cellLowerRight, ghostCellBottom1, ghostCellRight0));
        n7.neighbors.addAll(List.of(cellLowerRight, cellUpperRight, ghostCellRight0, ghostCellRight1));
        n8.neighbors.addAll(List.of(cellUpperRight, ghostCellRight1, ghostCellTop1));

        // Check cells
        List<Cell> expectedCells = List.of(cellLowerLeft, cellUpperLeft, cellLowerRight, cellUpperRight);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-8);
        }

        // Check nodes
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-8);
        }

        // Check internal faces
        List<Face> expectedInternalFaces = List.of(f2, f3, f7, f10);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-8);

        // Check boundaries
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6, f9), bc),
                new Boundary("xi max", List.of(f8, f11), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f4, f5), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-8);
    }

    @Test
    public void mesh_with_nonuniform_2_by_2_cells_x_z() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 3\n" +
                    "eta = 3\n" +
                    "-32.0        20.0        -11.0\n" +
                    "-36.5        20.0        -1.0 \n" +
                    "-40.0        20.0        30.0 \n" +
                    "-10.0        20.0        -6.0 \n" +
                    "-12.0        20.0        10.0 \n" +
                    "-12.0        20.0        40.0 \n" +
                    "19.0         20.0        -8.0 \n" +
                    "12.0         20.0        15.5 \n" +
                    "11.5         20.0        34.0 \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured2DMesh mesh = new Structured2DMesh(tempFile, numVars, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(-32.0, 20.0, -11.0);
        Node n1 = new Node(-36.5, 20.0, -1.0);
        Node n2 = new Node(-40.0, 20.0, 30.0);
        Node n3 = new Node(-10.0, 20.0, -6.0);
        Node n4 = new Node(-12.0, 20.0, 10.0);
        Node n5 = new Node(-12.0, 20.0, 40.0);
        Node n6 = new Node(19.0, 20.0, -8.0);
        Node n7 = new Node(12.0, 20.0, 15.5);
        Node n8 = new Node(11.5, 20.0, 34.0);

        Shape shape = new Shape(328.25, new Point(-21.96255395, 20.0, -1.58568164));
        Cell cellLowerLeft = new Cell(0, new Node[]{n0, n1, n4, n3}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-25.31196581, 20.0, 20.00854701));
        Cell cellUpperLeft = new Cell(1, new Node[]{n1, n2, n5, n4}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(3.15843137, 20.0, 2.73058824));
        Cell cellLowerRight = new Cell(2, new Node[]{n3, n4, n7, n6}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(-1.06356269, 20.0, 24.83224803));
        Cell cellUpperRight = new Cell(3, new Node[]{n4, n5, n8, n7}, VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(328.25, new Point(-45.70291039, 20.0, -12.26884204));
        Cell ghostCellLeft0 = new Cell(-1, new Node[]{
                n0, n1, new Node(-50.33264033, 20.0, -24.14968815), new Node(-60.98128898, 20.0, -12.04158004)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-52.09054933, 20.0, 16.98515855));
        Cell ghostCellLeft1 = new Cell(-1, new Node[]{
                n1, n2, new Node(-62.83585923, 20.0, 4.26046751), new Node(-69.52478808, 20.0, 33.50526586)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(26.38778525, 20.0, 9.64997024));
        Cell ghostCellRight0 = new Cell(-1, new Node[]{
                n6, n7, new Node(42.17879418, 20.0, 9.54261954), new Node(35.09771310, 20.0, 24.02910603)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(24.54041417, 20.0, 25.52424740));
        Cell ghostCellRight1 = new Cell(-1, new Node[]{
                n7, n8, new Node(34.64160584, 20.0, 41.26058394), new Node(36.26204380, 20.0, 11.30437956)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(328.25, new Point(-18.87949356, 20.0, -15.15114733));
        Cell ghostCellBottom0 = new Cell(-1, new Node[]{
                n0, n3, new Node(-31.73575638, 20.0, -21.96267191), new Node(-4.88801572, 20.0, -21.29273084)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(531.25, new Point(1.83533589, 20.0, -16.45429632));
        Cell ghostCellBottom1 = new Cell(-1, new Node[]{
                n3, n6, new Node(-14.17751479, 20, -21.57396450), new Node(8.84023669, 20.0, -30.31656805)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(819.0, new Point(-34.96447770, 20.0, 47.03558031));
        Cell ghostCellTop0 = new Cell(-1, new Node[]{
                n2, n5, new Node(-56.92986425, 20.0, 56.20361991), new Node(-31.00452489, 20.0, 63.21266968)},
                VTKType.VTK_QUAD, shape, numVars);

        shape = new Shape(575.875, new Point(4.86908936, 20.0, 48.06846857));
        Cell ghostCellTop1 = new Cell(-1, new Node[]{
                n5, n8, new Node(2.38164046, 20.0, 66.32809180), new Node(20.80747981, 20.0, 49.99596260)},
                VTKType.VTK_QUAD, shape, numVars);

        Surface surface = new Surface(22.56102835, new Point(-21, 20.0, -8.5), new Vector(0.22162110, 0.0, -0.97513286));
        Face f0 = new Face(new Node[]{n0, n3}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellBottom0, numVars);

        surface = new Surface(29.06888371, new Point(4.5, 20.0, -7.0), new Vector(-0.06880209, 0.0, -0.99763033));
        Face f1 = new Face(new Node[]{n3, n6}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellBottom1, numVars);

        surface = new Surface(26.85609800, new Point(-24.25, 20.0, 4.5), new Vector(-0.40959040, 0.0, 0.91226953));
        Face f2 = new Face(new Node[]{n1, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellUpperLeft, numVars);

        surface = new Surface(24.62214450, new Point(0.0, 20.0, 12.75), new Vector(-0.22337616, 0.0, 0.97473232));
        Face f3 = new Face(new Node[]{n4, n7}, VTKType.VTK_LINE, surface, cellLowerRight, cellUpperRight, numVars);

        surface = new Surface(29.73213749, new Point(-26.0, 20.0, 35.0), new Vector(-0.33633640, 0.0, 0.94174191));
        Face f4 = new Face(new Node[]{n2, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellTop0, numVars);

        surface = new Surface(24.25386567, new Point(-0.25, 20.0, 37.0), new Vector(0.24738325, 0.0, 0.96891771));
        Face f5 = new Face(new Node[]{n5, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellTop1, numVars);

        surface = new Surface(10.96585610, new Point(-34.25, 20.0, -6.0), new Vector(-0.91192151, 0.0, -0.41036468));
        Face f6 = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, cellLowerLeft, ghostCellLeft0, numVars);

        surface = new Surface(16.12451550, new Point(-11.0, 20.0, 2.0), new Vector(0.99227788, 0.0, 0.12403473));
        Face f7 = new Face(new Node[]{n3, n4}, VTKType.VTK_LINE, surface, cellLowerLeft, cellLowerRight, numVars);

        surface = new Surface(24.52039967, new Point(15.5, 20.0, 3.75), new Vector(0.95838568, 0.0, 0.28547659));
        Face f8 = new Face(new Node[]{n6, n7}, VTKType.VTK_LINE, surface, cellLowerRight, ghostCellRight0, numVars);

        surface = new Surface(31.19695498, new Point(-38.25, 20.0, 14.5), new Vector(-0.99368672, 0.0, -0.11219044));
        Face f9 = new Face(new Node[]{n1, n2}, VTKType.VTK_LINE, surface, cellUpperLeft, ghostCellLeft1, numVars);

        surface = new Surface(30.0, new Point(-12.0, 20.0, 25.0), new Vector(1, 0, 0));
        Face f10 = new Face(new Node[]{n4, n5}, VTKType.VTK_LINE, surface, cellUpperLeft, cellUpperRight, numVars);

        surface = new Surface(18.50675552, new Point(11.75, 20.0, 24.75), new Vector(0.99963497, 0.0, 0.02701716));
        Face f11 = new Face(new Node[]{n7, n8}, VTKType.VTK_LINE, surface, cellUpperRight, ghostCellRight1, numVars);

        cellLowerLeft.faces.addAll(List.of(f0, f6, f2, f7));
        cellLowerRight.faces.addAll(List.of(f1, f7, f3, f8));
        cellUpperLeft.faces.addAll(List.of(f2, f9, f4, f10));
        cellUpperRight.faces.addAll(List.of(f3, f10, f5, f11));
        ghostCellBottom0.faces.add(f0);
        ghostCellBottom1.faces.add(f1);
        ghostCellLeft0.faces.add(f6);
        ghostCellLeft1.faces.add(f9);
        ghostCellTop0.faces.add(f4);
        ghostCellTop1.faces.add(f5);
        ghostCellRight0.faces.add(f8);
        ghostCellRight1.faces.add(f11);

        n0.neighbors.addAll(List.of(cellLowerLeft, ghostCellLeft0, ghostCellBottom0));
        n1.neighbors.addAll(List.of(cellLowerLeft, cellUpperLeft, ghostCellLeft0, ghostCellLeft1));
        n2.neighbors.addAll(List.of(cellUpperLeft, ghostCellLeft1, ghostCellTop0));
        n3.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, ghostCellBottom0, ghostCellBottom1));
        n4.neighbors.addAll(List.of(cellLowerLeft, cellLowerRight, cellUpperLeft, cellUpperRight));
        n5.neighbors.addAll(List.of(cellUpperLeft, cellUpperRight, ghostCellTop0, ghostCellTop1));
        n6.neighbors.addAll(List.of(cellLowerRight, ghostCellBottom1, ghostCellRight0));
        n7.neighbors.addAll(List.of(cellLowerRight, cellUpperRight, ghostCellRight0, ghostCellRight1));
        n8.neighbors.addAll(List.of(cellUpperRight, ghostCellRight1, ghostCellTop1));

        // Check cells
        List<Cell> expectedCells = List.of(cellLowerLeft, cellUpperLeft, cellLowerRight, cellUpperRight);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-8);
        }

        // Check nodes
        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-8);
        }

        // Check internal faces
        List<Face> expectedInternalFaces = List.of(f2, f3, f7, f10);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-8);

        // Check boundaries
        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f6, f9), bc),
                new Boundary("xi max", List.of(f8, f11), bc),
                new Boundary("eta min", List.of(f0, f1), bc),
                new Boundary("eta max", List.of(f4, f5), bc)
        );
        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-8);
    }
}
