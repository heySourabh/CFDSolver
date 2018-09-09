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

public class Structured3DMeshSimpleTest {
    @Test
    public void mesh_with_two_cartesian_cell_in_y_direction() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 3\n" +
                    "mode = ASCII\n" +
                    "xi = 2\n" +
                    "eta = 3\n" +
                    "zeta = 2\n" +
                    "-0.5      -0.5      0.0   \n" +
                    "-0.5      -0.5      3.5   \n" +
                    "-0.5      1.0       0.0   \n" +
                    "-0.5      1.0       3.5   \n" +
                    "-0.5      2.5       0.0   \n" +
                    "-0.5      2.5       3.5   \n" +
                    "2.0       -0.5      0.0   \n" +
                    "2.0       -0.5      3.5   \n" +
                    "2.0       1.0       0.0   \n" +
                    "2.0       1.0       3.5   \n" +
                    "2.0       2.5       0.0   \n" +
                    "2.0       2.5       3.5   \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured3DMesh mesh = new Structured3DMesh(tempFile, numVars, bc, bc, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(-0.5, -0.5, 0.0);
        Node n1 = new Node(-0.5, -0.5, 3.5);
        Node n2 = new Node(-0.5, 1.0, 0.0);
        Node n3 = new Node(-0.5, 1.0, 3.5);
        Node n4 = new Node(-0.5, 2.5, 0.0);
        Node n5 = new Node(-0.5, 2.5, 3.5);
        Node n6 = new Node(2.0, -0.5, 0.0);
        Node n7 = new Node(2.0, -0.5, 3.5);
        Node n8 = new Node(2.0, 1.0, 0.0);
        Node n9 = new Node(2.0, 1.0, 3.5);
        Node n10 = new Node(2.0, 2.5, 0.0);
        Node n11 = new Node(2.0, 2.5, 3.5);

        // left cell
        Shape shape = new Shape(13.125, new Point(0.75, 0.25, 1.75));
        Cell leftCell = new Cell(new Node[]{n0, n6, n8, n2, n1, n7, n9, n3},
                VTKType.VTK_HEXAHEDRON, shape, numVars);
        leftCell.setIndex(0);

        // right cell
        shape = new Shape(13.125, new Point(0.75, 1.75, 1.75));
        Cell rightCell = new Cell(new Node[]{n2, n8, n10, n4, n3, n9, n11, n5},
                VTKType.VTK_HEXAHEDRON, shape, numVars);
        rightCell.setIndex(1);

        // left ghost
        shape = new Shape(13.125, new Point(0.75, -1.25, 1.75));
        Cell leftGhost = new Cell(new Node[]{
                n0, n6, n7, n1,
                new Node(-0.5, -2, 0.0), new Node(2.0, -2.0, 0.0),
                new Node(2.0, -2.0, 3.5), new Node(-0.5, -2.0, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // right ghost
        shape = new Shape(13.125, new Point(0.75, 3.25, 1.75));
        Cell rightGhost = new Cell(new Node[]{
                n4, n10, n11, n5,
                new Node(-0.5, 4.0, 0.0), new Node(2.0, 4.0, 0.0),
                new Node(2.0, 4.0, 3.5), new Node(-0.5, 4.0, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // top-left ghost
        shape = new Shape(13.125, new Point(0.75, 0.25, 5.25));
        Cell topLeftGhost = new Cell(new Node[]{
                n1, n7, n9, n3,
                new Node(-0.5, -0.5, 7.0), new Node(2.0, -0.5, 7.0),
                new Node(2.0, 1.0, 7.0), new Node(-0.5, 1.0, 7.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // top-right ghost
        shape = new Shape(13.125, new Point(0.75, 1.75, 5.25));
        Cell topRightGhost = new Cell(new Node[]{
                n3, n9, n11, n5,
                new Node(-0.5, 1.0, 7.0), new Node(2.0, 1.0, 7.0),
                new Node(2.0, 2.5, 7.0), new Node(-0.5, 2.5, 7.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // bottom-left ghost
        shape = new Shape(13.125, new Point(0.75, 0.25, -1.75));
        Cell bottomLeftGhost = new Cell(new Node[]{
                n0, n6, n8, n2,
                new Node(-0.5, -0.5, -3.5), new Node(2.0, -0.5, -3.5),
                new Node(2.0, 1.0, -3.5), new Node(-0.5, 1.0, -3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // bottom-right ghost
        shape = new Shape(13.125, new Point(0.75, 1.75, -1.75));
        Cell bottomRightGhost = new Cell(new Node[]{
                n2, n8, n10, n4,
                new Node(-0.5, 1.0, -3.5), new Node(2.0, 1.0, -3.5),
                new Node(2.0, 2.5, -3.5), new Node(-0.5, 2.5, -3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // front-left ghost
        shape = new Shape(13.125, new Point(3.25, 0.25, 1.75));
        Cell frontLeftGhost = new Cell(new Node[]{
                n6, n8, n9, n7,
                new Node(4.5, -0.5, 0.0), new Node(4.5, 1.0, 0.0),
                new Node(4.5, 1.0, 3.5), new Node(4.5, -0.5, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // front-right ghost
        shape = new Shape(13.125, new Point(3.25, 1.75, 1.75));
        Cell frontRightGhost = new Cell(new Node[]{
                n8, n9, n11, n10,
                new Node(4.5, 1.0, 0.0), new Node(4.5, 1.0, 3.5),
                new Node(4.5, 2.5, 3.5), new Node(4.5, 2.5, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // back-left ghost
        shape = new Shape(13.125, new Point(-1.75, 0.25, 1.75));
        Cell backLeftGhost = new Cell(new Node[]{
                n0, n1, n3, n2,
                new Node(-3.0, -0.5, 0.0), new Node(-3.0, -0.5, 3.5),
                new Node(-3.0, 1.0, 3.5), new Node(-3.0, 1.0, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // back-right ghost
        shape = new Shape(13.125, new Point(-1.75, 1.75, 1.75));
        Cell backRightGhost = new Cell(new Node[]{
                n2, n3, n5, n4,
                new Node(-3.0, 1.0, 0.0), new Node(-3.0, 1.0, 3.5),
                new Node(-3.0, 2.5, 3.5), new Node(-3.0, 2.5, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);


        Surface surface = new Surface(8.75, new Point(0.75, -0.5, 1.75), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n0, n1, n7, n6}, VTKType.VTK_QUAD, surface, leftCell, leftGhost, numVars);

        surface = new Surface(8.75, new Point(0.75, 1.0, 1.75), new Vector(0, 1, 0));
        Face f1 = new Face(new Node[]{n2, n3, n9, n8}, VTKType.VTK_QUAD, surface, leftCell, rightCell, numVars);

        surface = new Surface(8.75, new Point(0.75, 2.5, 1.75), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n4, n5, n11, n10}, VTKType.VTK_QUAD, surface, rightCell, rightGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 0.25, 0.0), new Vector(0, 0, -1));
        Face f3 = new Face(new Node[]{n0, n2, n8, n6}, VTKType.VTK_QUAD, surface, leftCell, bottomLeftGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 1.75, 0.0), new Vector(0, 0, -1));
        Face f4 = new Face(new Node[]{n2, n8, n10, n4}, VTKType.VTK_QUAD, surface, rightCell, bottomRightGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 0.25, 3.5), new Vector(0, 0, 1));
        Face f5 = new Face(new Node[]{n1, n3, n9, n7}, VTKType.VTK_QUAD, surface, leftCell, topLeftGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 1.75, 3.5), new Vector(0, 0, 1));
        Face f6 = new Face(new Node[]{n3, n5, n11, n9}, VTKType.VTK_QUAD, surface, rightCell, topRightGhost, numVars);

        surface = new Surface(5.25, new Point(2.0, 0.25, 1.75), new Vector(1, 0, 0));
        Face f7 = new Face(new Node[]{n6, n7, n9, n8}, VTKType.VTK_QUAD, surface, leftCell, frontLeftGhost, numVars);

        surface = new Surface(5.25, new Point(2.0, 1.75, 1.75), new Vector(1, 0, 0));
        Face f8 = new Face(new Node[]{n8, n9, n11, n10}, VTKType.VTK_QUAD, surface, rightCell, frontRightGhost, numVars);

        surface = new Surface(5.25, new Point(-0.5, 0.25, 1.75), new Vector(-1, 0, 0));
        Face f9 = new Face(new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, surface, leftCell, backLeftGhost, numVars);

        surface = new Surface(5.25, new Point(-0.5, 1.75, 1.75), new Vector(-1, 0, 0));
        Face f10 = new Face(new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, surface, rightCell, backRightGhost, numVars);

        leftCell.faces.addAll(List.of(f0, f1, f3, f5, f7, f9));
        rightCell.faces.addAll(List.of(f1, f2, f4, f6, f8, f10));
        leftGhost.faces.add(f0);
        rightGhost.faces.add(f2);
        topLeftGhost.faces.add(f5);
        topRightGhost.faces.add(f6);
        bottomLeftGhost.faces.add(f3);
        bottomRightGhost.faces.add(f4);
        frontLeftGhost.faces.add(f7);
        frontRightGhost.faces.add(f8);
        backLeftGhost.faces.add(f9);
        backRightGhost.faces.add(f10);

        n0.neighbors.addAll(List.of(leftCell, leftGhost, bottomLeftGhost, backLeftGhost));
        n1.neighbors.addAll(List.of(leftCell, leftGhost, topLeftGhost, backLeftGhost));
        n2.neighbors.addAll(List.of(leftCell, rightCell, backLeftGhost, backRightGhost, bottomLeftGhost, bottomRightGhost));
        n3.neighbors.addAll(List.of(leftCell, rightCell, backLeftGhost, backRightGhost, topLeftGhost, topRightGhost));
        n4.neighbors.addAll(List.of(rightCell, rightGhost, bottomRightGhost, backRightGhost));
        n5.neighbors.addAll(List.of(rightCell, rightGhost, topRightGhost, backRightGhost));
        n6.neighbors.addAll(List.of(leftCell, leftGhost, bottomLeftGhost, frontLeftGhost));
        n7.neighbors.addAll(List.of(leftCell, leftGhost, topLeftGhost, frontLeftGhost));
        n8.neighbors.addAll(List.of(leftCell, rightCell, frontLeftGhost, frontRightGhost, bottomLeftGhost, bottomRightGhost));
        n9.neighbors.addAll(List.of(leftCell, rightCell, frontLeftGhost, frontRightGhost, topLeftGhost, topRightGhost));
        n10.neighbors.addAll(List.of(rightCell, rightGhost, bottomRightGhost, frontRightGhost));
        n11.neighbors.addAll(List.of(rightCell, rightGhost, topRightGhost, frontRightGhost));

        List<Cell> expectedCells = List.of(leftCell, rightCell);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-8);
        }

        List<Face> expectedInternalFaces = List.of(f1);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-8);

        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-8);
        }

        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f9, f10), bc),
                new Boundary("xi max", List.of(f7, f8), bc),
                new Boundary("eta min", List.of(f0), bc),
                new Boundary("eta max", List.of(f2), bc),
                new Boundary("zeta min", List.of(f3, f4), bc),
                new Boundary("zeta max", List.of(f5, f6), bc)
        );

        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-8);
    }

    @Test
    public void mesh_with_two_cartesian_cell_in_y_direction_with_xi_reversed() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 3\n" +
                    "mode = ASCII\n" +
                    "xi = 2\n" +
                    "eta = 3\n" +
                    "zeta = 2\n" +
                    "2.0       -0.5       0.0   \n" +
                    "2.0       -0.5       3.5   \n" +
                    "2.0       1.0        0.0   \n" +
                    "2.0       1.0        3.5   \n" +
                    "2.0       2.5        0.0   \n" +
                    "2.0       2.5        3.5   \n" +
                    "-0.5      -0.5       0.0   \n" +
                    "-0.5      -0.5       3.5   \n" +
                    "-0.5      1.0        0.0   \n" +
                    "-0.5      1.0        3.5   \n" +
                    "-0.5      2.5        0.0   \n" +
                    "-0.5      2.5        3.5   \n");
        }

        GoverningEquations govEqn = new EulerEquations(1.4, 287);
        int numVars = govEqn.numVars();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Structured3DMesh mesh = new Structured3DMesh(tempFile, numVars, bc, bc, bc, bc, bc, bc);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Node n0 = new Node(2.0, -0.5, 0.0);
        Node n1 = new Node(2.0, -0.5, 3.5);
        Node n2 = new Node(2.0, 1.0, 0.0);
        Node n3 = new Node(2.0, 1.0, 3.5);
        Node n4 = new Node(2.0, 2.5, 0.0);
        Node n5 = new Node(2.0, 2.5, 3.5);
        Node n6 = new Node(-0.5, -0.5, 0.0);
        Node n7 = new Node(-0.5, -0.5, 3.5);
        Node n8 = new Node(-0.5, 1.0, 0.0);
        Node n9 = new Node(-0.5, 1.0, 3.5);
        Node n10 = new Node(-0.5, 2.5, 0.0);
        Node n11 = new Node(-0.5, 2.5, 3.5);

        // left cell
        Shape shape = new Shape(13.125, new Point(0.75, 0.25, 1.75));
        Cell leftCell = new Cell(new Node[]{n0, n6, n8, n2, n1, n7, n9, n3},
                VTKType.VTK_HEXAHEDRON, shape, numVars);
        leftCell.setIndex(0);

        // right cell
        shape = new Shape(13.125, new Point(0.75, 1.75, 1.75));
        Cell rightCell = new Cell(new Node[]{n2, n8, n10, n4, n3, n9, n11, n5},
                VTKType.VTK_HEXAHEDRON, shape, numVars);
        rightCell.setIndex(1);

        // left ghost
        shape = new Shape(13.125, new Point(0.75, -1.25, 1.75));
        Cell leftGhost = new Cell(new Node[]{
                n0, n6, n7, n1,
                new Node(-0.5, -2, 0.0), new Node(2.0, -2.0, 0.0),
                new Node(2.0, -2.0, 3.5), new Node(-0.5, -2.0, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // right ghost
        shape = new Shape(13.125, new Point(0.75, 3.25, 1.75));
        Cell rightGhost = new Cell(new Node[]{
                n4, n10, n11, n5,
                new Node(-0.5, 4.0, 0.0), new Node(2.0, 4.0, 0.0),
                new Node(2.0, 4.0, 3.5), new Node(-0.5, 4.0, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // top-left ghost
        shape = new Shape(13.125, new Point(0.75, 0.25, 5.25));
        Cell topLeftGhost = new Cell(new Node[]{
                n1, n7, n9, n3,
                new Node(-0.5, -0.5, 7.0), new Node(2.0, -0.5, 7.0),
                new Node(2.0, 1.0, 7.0), new Node(-0.5, 1.0, 7.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // top-right ghost
        shape = new Shape(13.125, new Point(0.75, 1.75, 5.25));
        Cell topRightGhost = new Cell(new Node[]{
                n3, n9, n11, n5,
                new Node(-0.5, 1.0, 7.0), new Node(2.0, 1.0, 7.0),
                new Node(2.0, 2.5, 7.0), new Node(-0.5, 2.5, 7.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // bottom-left ghost
        shape = new Shape(13.125, new Point(0.75, 0.25, -1.75));
        Cell bottomLeftGhost = new Cell(new Node[]{
                n0, n6, n8, n2,
                new Node(-0.5, -0.5, -3.5), new Node(2.0, -0.5, -3.5),
                new Node(2.0, 1.0, -3.5), new Node(-0.5, 1.0, -3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // bottom-right ghost
        shape = new Shape(13.125, new Point(0.75, 1.75, -1.75));
        Cell bottomRightGhost = new Cell(new Node[]{
                n2, n8, n10, n4,
                new Node(-0.5, 1.0, -3.5), new Node(2.0, 1.0, -3.5),
                new Node(2.0, 2.5, -3.5), new Node(-0.5, 2.5, -3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // front-left ghost
        shape = new Shape(13.125, new Point(3.25, 0.25, 1.75));
        Cell frontLeftGhost = new Cell(new Node[]{
                n0, n1, n3, n2,
                new Node(4.5, -0.5, 0.0), new Node(4.5, 1.0, 0.0),
                new Node(4.5, 1.0, 3.5), new Node(4.5, -0.5, 3.5)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // front-right ghost
        shape = new Shape(13.125, new Point(3.25, 1.75, 1.75));
        Cell frontRightGhost = new Cell(new Node[]{
                n2, n3, n5, n4,
                new Node(4.5, 1.0, 0.0), new Node(4.5, 1.0, 3.5),
                new Node(4.5, 2.5, 3.5), new Node(4.5, 2.5, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // back-left ghost
        shape = new Shape(13.125, new Point(-1.75, 0.25, 1.75));
        Cell backLeftGhost = new Cell(new Node[]{
                n6, n8, n9, n7,
                new Node(-3.0, -0.5, 0.0), new Node(-3.0, -0.5, 3.5),
                new Node(-3.0, 1.0, 3.5), new Node(-3.0, 1.0, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);

        // back-right ghost
        shape = new Shape(13.125, new Point(-1.75, 1.75, 1.75));
        Cell backRightGhost = new Cell(new Node[]{
                n8, n9, n11, n10,
                new Node(-3.0, 1.0, 0.0), new Node(-3.0, 1.0, 3.5),
                new Node(-3.0, 2.5, 3.5), new Node(-3.0, 2.5, 0.0)},
                VTKType.VTK_HEXAHEDRON, shape, numVars);


        Surface surface = new Surface(8.75, new Point(0.75, -0.5, 1.75), new Vector(0, -1, 0));
        Face f0 = new Face(new Node[]{n0, n1, n7, n6}, VTKType.VTK_QUAD, surface, leftCell, leftGhost, numVars);

        surface = new Surface(8.75, new Point(0.75, 1.0, 1.75), new Vector(0, 1, 0));
        Face f1 = new Face(new Node[]{n2, n3, n9, n8}, VTKType.VTK_QUAD, surface, leftCell, rightCell, numVars);

        surface = new Surface(8.75, new Point(0.75, 2.5, 1.75), new Vector(0, 1, 0));
        Face f2 = new Face(new Node[]{n4, n5, n11, n10}, VTKType.VTK_QUAD, surface, rightCell, rightGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 0.25, 0.0), new Vector(0, 0, -1));
        Face f3 = new Face(new Node[]{n0, n2, n8, n6}, VTKType.VTK_QUAD, surface, leftCell, bottomLeftGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 1.75, 0.0), new Vector(0, 0, -1));
        Face f4 = new Face(new Node[]{n2, n8, n10, n4}, VTKType.VTK_QUAD, surface, rightCell, bottomRightGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 0.25, 3.5), new Vector(0, 0, 1));
        Face f5 = new Face(new Node[]{n1, n3, n9, n7}, VTKType.VTK_QUAD, surface, leftCell, topLeftGhost, numVars);

        surface = new Surface(3.75, new Point(0.75, 1.75, 3.5), new Vector(0, 0, 1));
        Face f6 = new Face(new Node[]{n3, n5, n11, n9}, VTKType.VTK_QUAD, surface, rightCell, topRightGhost, numVars);

        surface = new Surface(5.25, new Point(2.0, 0.25, 1.75), new Vector(1, 0, 0));
        Face f7 = new Face(new Node[]{n0, n1, n3, n2}, VTKType.VTK_QUAD, surface, leftCell, frontLeftGhost, numVars);

        surface = new Surface(5.25, new Point(2.0, 1.75, 1.75), new Vector(1, 0, 0));
        Face f8 = new Face(new Node[]{n2, n3, n5, n4}, VTKType.VTK_QUAD, surface, rightCell, frontRightGhost, numVars);

        surface = new Surface(5.25, new Point(-0.5, 0.25, 1.75), new Vector(-1, 0, 0));
        Face f9 = new Face(new Node[]{n6, n7, n9, n8}, VTKType.VTK_QUAD, surface, leftCell, backLeftGhost, numVars);

        surface = new Surface(5.25, new Point(-0.5, 1.75, 1.75), new Vector(-1, 0, 0));
        Face f10 = new Face(new Node[]{n8, n9, n11, n10}, VTKType.VTK_QUAD, surface, rightCell, backRightGhost, numVars);

        leftCell.faces.addAll(List.of(f0, f1, f3, f5, f7, f9));
        rightCell.faces.addAll(List.of(f1, f2, f4, f6, f8, f10));
        leftGhost.faces.add(f0);
        rightGhost.faces.add(f2);
        topLeftGhost.faces.add(f5);
        topRightGhost.faces.add(f6);
        bottomLeftGhost.faces.add(f3);
        bottomRightGhost.faces.add(f4);
        frontLeftGhost.faces.add(f7);
        frontRightGhost.faces.add(f8);
        backLeftGhost.faces.add(f9);
        backRightGhost.faces.add(f10);

        n0.neighbors.addAll(List.of(leftCell, leftGhost, bottomLeftGhost, frontLeftGhost));
        n1.neighbors.addAll(List.of(leftCell, leftGhost, topLeftGhost, frontLeftGhost));
        n2.neighbors.addAll(List.of(leftCell, rightCell, frontLeftGhost, frontRightGhost, bottomLeftGhost, bottomRightGhost));
        n3.neighbors.addAll(List.of(leftCell, rightCell, frontLeftGhost, frontRightGhost, topLeftGhost, topRightGhost));
        n4.neighbors.addAll(List.of(rightCell, rightGhost, bottomRightGhost, frontRightGhost));
        n5.neighbors.addAll(List.of(rightCell, rightGhost, topRightGhost, frontRightGhost));
        n6.neighbors.addAll(List.of(leftCell, leftGhost, bottomLeftGhost, backLeftGhost));
        n7.neighbors.addAll(List.of(leftCell, leftGhost, topLeftGhost, backLeftGhost));
        n8.neighbors.addAll(List.of(leftCell, rightCell, backLeftGhost, backRightGhost, bottomLeftGhost, bottomRightGhost));
        n9.neighbors.addAll(List.of(leftCell, rightCell, backLeftGhost, backRightGhost, topLeftGhost, topRightGhost));
        n10.neighbors.addAll(List.of(rightCell, rightGhost, bottomRightGhost, backRightGhost));
        n11.neighbors.addAll(List.of(rightCell, rightGhost, topRightGhost, backRightGhost));

        List<Cell> expectedCells = List.of(leftCell, rightCell);
        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-8);
        }

        List<Face> expectedInternalFaces = List.of(f1);
        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-8);

        List<Node> expectedNodes = List.of(n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11);
        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-8);
        }

        List<Boundary> expectedBoundaries = List.of(
                new Boundary("xi min", List.of(f7, f8), bc),
                new Boundary("xi max", List.of(f9, f10), bc),
                new Boundary("eta min", List.of(f0), bc),
                new Boundary("eta max", List.of(f2), bc),
                new Boundary("zeta min", List.of(f3, f4), bc),
                new Boundary("zeta max", List.of(f5, f6), bc)
        );

        assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-8);
    }
}