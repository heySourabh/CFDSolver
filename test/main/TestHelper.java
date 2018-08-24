package main;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.mesh.*;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class TestHelper {
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    public static void assertThrows(Class<? extends Throwable> ex, RunnableWithException code) {
        try {
            code.run();
            fail("Expecting an exception to be thrown, but no exception is thrown.");
        } catch (Exception e) {
            if (e.getClass() != ex) {
                fail("Exception: " + e.toString() + "\n" +
                        "Expected \"" + ex.toString() + "\", but received \"" + e.getClass().toString());
            }
        }
    }

    public static void assertVectorEquals(Vector expected, Vector actual, double tolerance) {
        assertEquals("x-component of vector", expected.x, actual.x, tolerance);
        assertEquals("y-component of vector", expected.y, actual.y, tolerance);
        assertEquals("z-component of vector", expected.z, actual.z, tolerance);
    }

    public static void assertPointEquals(Point expected, Point actual, double tolerance) {
        assertEquals("x-component of point", expected.x, actual.x, tolerance);
        assertEquals("y-component of point", expected.y, actual.y, tolerance);
        assertEquals("z-component of point", expected.z, actual.z, tolerance);
    }

    public static void assertNodeEquals(Node expected, Node actual, double tolerance) {
        assertPointEquals(expected.location(), actual.location(), tolerance);
        assertEquals(expected.neighbors.size(), actual.neighbors.size());
        assertTrue(containsSameCells(expected.neighbors, actual.neighbors, tolerance));
    }

    public static void assertCellEquals(Cell expected, Cell actual, double tolerance) {
        assertTrue(sameCells(expected, actual, tolerance));
    }

    public static void assertFaceListEquals(List<Face> expected, List<Face> actual, double tolerance) {
        assertTrue(containsSameFaces(expected, actual, tolerance));
    }

    private static boolean sameCells(Cell expected, Cell actual, double tolerance) {
        // has same index
        if (expected.index != actual.index) return false;

        // has same shape
        if (!sameShape(expected.shape, actual.shape, tolerance)) return false;

        // has same nodes
        if (!containsSameNodes(List.of(expected.nodes), List.of(actual.nodes), tolerance)) return false;

        // has same vtk type
        if (expected.vtkType != actual.vtkType) return false;

        // has same faces
        if (!containsSameFaces(expected.faces, actual.faces, tolerance)) return false;

        // has same lengths of arrays
        int numVars = expected.U.length;
        if (numVars != actual.U.length) return false;
        if (numVars != actual.residual.length) return false;
        return numVars == actual.reconstructCoeffs.length;
    }

    private static boolean sameShape(Shape expected, Shape actual, double tolerance) {
        // has same volume
        if (Math.abs(expected.volume - actual.volume) > tolerance) return false;

        // has same centroid
        return !(expected.centroid.distance(actual.centroid) > tolerance);
    }

    private static boolean containsSameCells(List<Cell> expected, List<Cell> actual, double tolerance) {
        if (expected.size() != actual.size()) return false;

        for (Cell ce : expected) {
            if (!contains(actual, ce, tolerance)) {
                return false;
            }
        }

        return true;
    }

    private static boolean contains(List<Cell> cellList, Cell cell, double tolerance) {
        for (Cell c : cellList) {
            if (sameCells(c, cell, tolerance))
                return true;
        }

        return false;
    }

    private static boolean sameNodes(Node expected, Node actual, double tolerance) {
        return expected.location().distance(actual.location()) < tolerance;
    }


    private static boolean containsSameNodes(List<Node> expected, List<Node> actual, double tolerance) {
        if (expected.size() != actual.size()) return false;

        for (Node ne : expected) {
            if (!contains(actual, ne, tolerance)) {
                return false;
            }
        }

        return true;
    }

    private static boolean contains(List<Node> nodeList, Node node, double tolerance) {
        for (Node n : nodeList) {
            if (sameNodes(n, node, tolerance))
                return true;
        }

        return false;
    }

    private static boolean containsSameFaces(List<Face> expected, List<Face> actual, double tolerance) {
        if (expected.size() != actual.size()) return false;

        for (Face fe : expected) {
            if (!contains(actual, fe, tolerance)) {
                return false;
            }
        }

        return true;
    }

    private static boolean contains(List<Face> faceList, Face face, double tolerance) {
        for (Face f : faceList) {
            if (sameFaces(f, face, tolerance))
                return true;
        }

        return false;
    }

    private static boolean sameFaces(Face expected, Face actual, double tolerance) {
        // Has same nodes
        if (!containsSameNodes(List.of(expected.nodes), List.of(actual.nodes), tolerance)) return false;

        // Has same vtk type
        if (expected.vtkType != actual.vtkType) return false;

        // Has same surface
        int direction = compareSurfaces(expected.surface, actual.surface, tolerance);
        if (direction == 0) return false;

        // Has same left and right cells
        if (direction == 1) {
            return sameShape(expected.left.shape, actual.left.shape, tolerance)
                    && sameShape(expected.right.shape, actual.right.shape, tolerance);
        }
        if (direction == -1) {
            return sameShape(expected.left.shape, actual.right.shape, tolerance)
                    && sameShape(expected.right.shape, actual.left.shape, tolerance);
        }

        // Has same length of arrays
        int numVars = expected.U.length;
        if (numVars != actual.U.length) return false;
        return numVars == actual.flux.length;
    }

    private static int compareSurfaces(Surface expected, Surface actual, double tolerance) {
        // compare area
        if (Math.abs(expected.area - actual.area) > tolerance) return 0;

        // compare centroid
        if (expected.centroid.distance(actual.centroid) > tolerance) return 0;

        // compare normals and directions
        if (expected.unitNormal.sub(actual.unitNormal).mag() < tolerance) return 1;
        if (expected.unitNormal.add(actual.unitNormal).mag() < tolerance) return -1;

        return 0;
    }

    // -------------------------- Tests for this class ------------------------------------------
    @Test
    public void nodes_at_same_location_are_same() {
        Node n1 = new Node(5, 6, 7);
        Node n2 = new Node(5, 6, 7);
        assertTrue(sameNodes(n1, n2, 1e-15));
    }

    @Test
    public void nodes_at_different_location_are_not_same() {
        Node n1 = new Node(5, 6, 7);
        Node n2 = new Node(1, 6, 7);
        assertFalse(sameNodes(n1, n2, 1e-15));
    }

    @Test
    public void node_lists_of_different_lengths_are_not_same() {
        Node n11 = new Node(1, 2, 3);

        List<Node> l1 = List.of(n11);
        List<Node> l2 = List.of();

        assertFalse(containsSameNodes(l1, l2, 1e-15));

        Node n12 = new Node(2, 3, 5);
        Node n22 = new Node(2, 3, 5);

        l1 = List.of(n11, n12);
        l2 = List.of(n22);

        assertFalse(containsSameNodes(l1, l2, 1e-15));
    }

    @Test
    public void node_lists_containing_same_nodes_in_any_order_are_same() {
        Node n11 = new Node(1, 2, 3);
        Node n21 = new Node(1, 2, 3);

        List<Node> l1 = List.of(n11);
        List<Node> l2 = List.of(n21);

        assertTrue(containsSameNodes(l1, l2, 1e-15));

        Node n12 = new Node(2, 3, 5);
        Node n22 = new Node(2, 3, 5);

        l1 = List.of(n11, n12);
        l2 = List.of(n22, n21);

        assertTrue(containsSameNodes(l1, l2, 1e-15));

        Node n13 = new Node(5, 36, 2);
        Node n23 = new Node(5, 36, 2);

        l1 = List.of(n11, n12, n13);
        l2 = List.of(n21, n23, n22);

        assertTrue(containsSameNodes(l1, l2, 1e-15));
    }

    @Test
    public void node_lists_containing_different_nodes_are_not_same() {
        Node n11 = new Node(1, 2, 3);
        Node n21 = new Node(1, 3, 3);

        List<Node> l1 = List.of(n11);
        List<Node> l2 = List.of(n21);

        assertFalse(containsSameNodes(l1, l2, 1e-15));

        Node n12 = new Node(2, 3, 5);
        Node n22 = new Node(5, 3, 5);

        l1 = List.of(n11, n12);
        l2 = List.of(n21, n22);

        assertFalse(containsSameNodes(l1, l2, 1e-15));

        Node n13 = new Node(5, 36, 2);
        Node n23 = new Node(5, 36, 2);

        l1 = List.of(n11, n12, n13);
        l2 = List.of(n21, n23, n22);

        assertFalse(containsSameNodes(l1, l2, 1e-15));
    }

    @Test
    public void shapes_with_same_volume_and_centroid_are_same() {
        Shape s1 = new Shape(1.2, new Point(1.5, 2.6, 3.0));
        Shape s2 = new Shape(1.2, new Point(1.5, 2.6, 3.0));

        assertTrue(sameShape(s1, s2, 1e-15));
    }

    @Test
    public void shapes_with_different_volume_or_centroid_are_not_same() {
        // different volume
        Shape s1 = new Shape(1.2, new Point(1.5, 2.6, 3.0));
        Shape s2 = new Shape(0.2, new Point(1.5, 2.6, 3.0));

        assertFalse(sameShape(s1, s2, 1e-15));

        // different centroid
        s1 = new Shape(1.2, new Point(1.5, 2.6, 3.0));
        s2 = new Shape(1.2, new Point(0.5, 2.6, 3.0));

        assertFalse(sameShape(s1, s2, 1e-15));
    }

    @Test
    public void surfaces_with_same_area_centroid_normal_returns_one() {
        Surface s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());
        Surface s2 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());

        assertEquals(1, compareSurfaces(s1, s2, 1e-15));
    }

    @Test
    public void surfaces_with_same_area_centroid_opposite_normal_returns_minus_one() {
        Surface s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());
        Surface s2 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit().mult(-1));

        assertEquals(-1, compareSurfaces(s1, s2, 1e-15));

        s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit().mult(-1));
        s2 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());

        assertEquals(-1, compareSurfaces(s1, s2, 1e-15));
    }

    @Test
    public void surfaces_with_different_area_or_centroid_or_normal_returns_zero() {
        // different area
        Surface s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());
        Surface s2 = new Surface(1.1, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());

        assertEquals(0, compareSurfaces(s1, s2, 1e-15));

        // different centroid
        s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());
        s2 = new Surface(1.4, new Point(-2.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());

        assertEquals(0, compareSurfaces(s1, s2, 1e-15));

        // different centroid
        s1 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(0.5, 0.5, 0.5).unit());
        s2 = new Surface(1.4, new Point(-0.5, 1.5, 2.0), new Vector(1.5, 0.5, 1.0).unit());

        assertEquals(0, compareSurfaces(s1, s2, 1e-15));
    }

    @Test
    public void faces_with_same_parameters_are_same() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        shape1 = new Shape(45.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        assertTrue(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void faces_with_same_parameters_but_opposite_normals_must_have_opposite_left_right_cells() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        shape1 = new Shape(45.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit().mult(-1));
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell2, cell1, numVars);

        assertTrue(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void faces_with_different_neighbor_cell_shapes_are_not_same() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        shape1 = new Shape(5.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        assertFalse(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void faces_with_different_vtktype_are_not_same() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_QUAD, surface, cell1, cell2, numVars);

        shape1 = new Shape(45.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        assertFalse(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void faces_with_different_nodes_are_not_same() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        shape1 = new Shape(45.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        nodesFace = List.of(nodesFace).subList(1, nodesFace.length).toArray(new Node[0]);
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        assertFalse(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void faces_with_different_surfaces_are_not_same() {
        Node[] nodesFace = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12)
        };
        Node[] nodesCell1 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                nodesFace[0], nodesFace[1], nodesFace[2],
                new Node(4, 4, 4)
        };

        int numVars = 4;

        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(23.4, new Point(5, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        Surface surface = new Surface(0.1, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f1 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        shape1 = new Shape(45.4, new Point(4, 8, 9));
        cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        shape2 = new Shape(23.4, new Point(5, 8, 9));
        cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);
        surface = new Surface(1.0, new Point(1.5, 4.1, 2), new Vector(.2, .5, .5).unit());
        Face f2 = new Face(nodesFace, VTKType.VTK_TRIANGLE, surface, cell1, cell2, numVars);

        assertFalse(sameFaces(f1, f2, 1e-15));
    }

    @Test
    public void cells_with_same_parameters_are_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(0, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertCellEquals(cell1, cell2, 1e-15);
    }

    @Test
    public void cells_with_different_index_are_not_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(1, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertFalse(sameCells(cell1, cell2, 1e-15));
    }

    @Test
    public void cells_with_different_shapes_are_not_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(5.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(0, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertFalse(sameCells(cell1, cell2, 1e-15));
    }

    @Test
    public void cells_with_different_nodes_are_not_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(0, 2, 4),
                new Node(1, 2, 4),
                new Node(2, 9, 12),
                new Node(3, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(0, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertFalse(sameCells(cell1, cell2, 1e-15));
    }

    @Test
    public void cells_with_different_vtktype_are_not_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_HEXAHEDRON, shape1, numVars);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(0, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertFalse(sameCells(cell1, cell2, 1e-15));
    }

    @Test
    public void cells_with_different_numVars_are_not_same() {
        Node[] nodesCell1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesCell2 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };

        int numVars = 5;
        Shape shape1 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell1 = new Cell(0, nodesCell1, VTKType.VTK_TETRA, shape1, numVars + 1);
        Shape shape2 = new Shape(45.4, new Point(4, 8, 9));
        Cell cell2 = new Cell(0, nodesCell2, VTKType.VTK_TETRA, shape2, numVars);

        assertFalse(sameCells(cell1, cell2, 1e-15));
    }

    @Test
    public void face_lists_containing_same_faces_in_any_order_are_same() {
        Node[] nodesFace1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesFace2 = {
                new Node(66, 2, 4),
                new Node(8, 6, 4),
                new Node(78, 9, 12),
                new Node(24, 78, 4)
        };
        Node[] nodesFace3 = {
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };

        Node[] centerCellNodes = new Node[]{
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };
        Node[] neigh1CellNodes = new Node[]{
                new Node(4, 2, 4),
                new Node(8, 2, 4),
                new Node(67, 6, 12),
                new Node(24, 41, 3)
        };
        Node[] neigh2CellNodes = new Node[]{
                new Node(4, 46, 4),
                new Node(8, 93, 4),
                new Node(90, 6, 37),
                new Node(24, 4, 3)
        };
        Node[] neigh3CellNodes = new Node[]{
                new Node(48, 46, 4),
                new Node(80, 9, 4),
                new Node(90, 16, 37),
                new Node(2, 40, 34)
        };

        int numVars = 2;
        Shape shapeCenterCell = new Shape(90, new Point(35, 54, 87));
        Cell cell = new Cell(1, centerCellNodes, VTKType.VTK_TETRA, shapeCenterCell, numVars);

        Shape shapeNeigh1 = new Shape(45, new Point(7, 5, 51));
        Cell cellNeigh1 = new Cell(10, neigh1CellNodes, VTKType.VTK_TETRA, shapeNeigh1, numVars);

        Shape shapeNeigh2 = new Shape(71, new Point(54, -78, 1));
        Cell cellNeigh2 = new Cell(12, neigh2CellNodes, VTKType.VTK_TETRA, shapeNeigh2, numVars);

        Shape shapeNeigh3 = new Shape(78, new Point(-27, 8, 0));
        Cell cellNeigh3 = new Cell(21, neigh3CellNodes, VTKType.VTK_TETRA, shapeNeigh3, numVars);

        Surface surface11 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
        Face f11 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface11, cell, cellNeigh1, numVars);

        Surface surface12 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f12 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface12, cell, cellNeigh2, numVars);

        Surface surface13 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
        Face f13 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface13, cell, cellNeigh3, numVars);


        Surface surface21 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
        Face f21 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface21, cell, cellNeigh1, numVars);

        Surface surface22 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f22 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface22, cell, cellNeigh2, numVars);

        Surface surface23 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
        Face f23 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface23, cell, cellNeigh3, numVars);

        List<Face> faceList1 = List.of(f11, f12, f13);
        List<Face> faceList2 = List.of(f23, f21, f22);

        assertTrue(containsSameFaces(faceList1, faceList2, 1e-15));
    }

    @Test
    public void face_lists_of_different_lengths_are_not_same() {
        Node[] nodesFace1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesFace2 = {
                new Node(66, 2, 4),
                new Node(8, 6, 4),
                new Node(78, 9, 12),
                new Node(24, 78, 4)
        };
        Node[] nodesFace3 = {
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };

        Node[] centerCellNodes = new Node[]{
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };
        Node[] neigh1CellNodes = new Node[]{
                new Node(4, 2, 4),
                new Node(8, 2, 4),
                new Node(67, 6, 12),
                new Node(24, 41, 3)
        };
        Node[] neigh2CellNodes = new Node[]{
                new Node(4, 46, 4),
                new Node(8, 93, 4),
                new Node(90, 6, 37),
                new Node(24, 4, 3)
        };
        Node[] neigh3CellNodes = new Node[]{
                new Node(48, 46, 4),
                new Node(80, 9, 4),
                new Node(90, 16, 37),
                new Node(2, 40, 34)
        };

        int numVars = 2;
        Shape shapeCenterCell = new Shape(90, new Point(35, 54, 87));
        Cell cell = new Cell(1, centerCellNodes, VTKType.VTK_TETRA, shapeCenterCell, numVars);

        Shape shapeNeigh1 = new Shape(45, new Point(7, 5, 51));
        Cell cellNeigh1 = new Cell(10, neigh1CellNodes, VTKType.VTK_TETRA, shapeNeigh1, numVars);

        Shape shapeNeigh2 = new Shape(71, new Point(54, -78, 1));
        Cell cellNeigh2 = new Cell(12, neigh2CellNodes, VTKType.VTK_TETRA, shapeNeigh2, numVars);

        Shape shapeNeigh3 = new Shape(78, new Point(-27, 8, 0));
        Cell cellNeigh3 = new Cell(21, neigh3CellNodes, VTKType.VTK_TETRA, shapeNeigh3, numVars);

        Surface surface11 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
        Face f11 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface11, cell, cellNeigh1, numVars);

        Surface surface12 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f12 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface12, cell, cellNeigh2, numVars);

        Surface surface13 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
        Face f13 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface13, cell, cellNeigh3, numVars);


        Surface surface21 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
        Face f21 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface21, cell, cellNeigh1, numVars);

        Surface surface22 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f22 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface22, cell, cellNeigh2, numVars);

//        Surface surface23 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
//        Face f23 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface23, cell, cellNeigh3, numVars);

        List<Face> faceList1 = List.of(f11, f12, f13);
        List<Face> faceList2 = List.of(f21, f22);

        assertFalse(containsSameFaces(faceList1, faceList2, 1e-15));
    }

    @Test
    public void face_lists_containing_different_faces_are_not_same() {
        Node[] nodesFace1 = {
                new Node(1, 2, 4),
                new Node(8, 2, 4),
                new Node(7, 9, 12),
                new Node(24, 4, 4)
        };
        Node[] nodesFace2 = {
                new Node(66, 2, 4),
                new Node(8, 6, 4),
                new Node(78, 9, 12),
                new Node(24, 78, 4)
        };
        Node[] nodesFace3 = {
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };

        Node[] centerCellNodes = new Node[]{
                new Node(56, 2, 4),
                new Node(8, 21, 4),
                new Node(67, 9, 12),
                new Node(24, 41, 46)
        };
        Node[] neigh1CellNodes = new Node[]{
                new Node(4, 2, 4),
                new Node(8, 2, 4),
                new Node(67, 6, 12),
                new Node(24, 41, 3)
        };
        Node[] neigh2CellNodes = new Node[]{
                new Node(4, 46, 4),
                new Node(8, 93, 4),
                new Node(90, 6, 37),
                new Node(24, 4, 3)
        };
        Node[] neigh3CellNodes = new Node[]{
                new Node(48, 46, 4),
                new Node(80, 9, 4),
                new Node(90, 16, 37),
                new Node(2, 40, 34)
        };

        int numVars = 2;
        Shape shapeCenterCell = new Shape(90, new Point(35, 54, 87));
        Cell cell = new Cell(1, centerCellNodes, VTKType.VTK_TETRA, shapeCenterCell, numVars);

        Shape shapeNeigh1 = new Shape(45, new Point(7, 5, 51));
        Cell cellNeigh1 = new Cell(10, neigh1CellNodes, VTKType.VTK_TETRA, shapeNeigh1, numVars);

        Shape shapeNeigh2 = new Shape(71, new Point(54, -78, 1));
        Cell cellNeigh2 = new Cell(12, neigh2CellNodes, VTKType.VTK_TETRA, shapeNeigh2, numVars);

        Shape shapeNeigh3 = new Shape(78, new Point(-27, 8, 0));
        Cell cellNeigh3 = new Cell(21, neigh3CellNodes, VTKType.VTK_TETRA, shapeNeigh3, numVars);

        Surface surface11 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
        Face f11 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface11, cell, cellNeigh1, numVars);

        Surface surface12 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f12 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface12, cell, cellNeigh2, numVars);

//        Surface surface13 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
//        Face f13 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface13, cell, cellNeigh3, numVars);
//
//
//        Surface surface21 = new Surface(90, new Point(34, 56, 87), new Vector(1, 3, 5).unit());
//        Face f21 = new Face(nodesFace1, VTKType.VTK_TRIANGLE, surface21, cell, cellNeigh1, numVars);

        Surface surface22 = new Surface(78, new Point(54, 78, 7), new Vector(-1, 3, 15).unit());
        Face f22 = new Face(nodesFace2, VTKType.VTK_TRIANGLE, surface22, cell, cellNeigh2, numVars);

        Surface surface23 = new Surface(90, new Point(-34, 25, 12), new Vector(11, -3, 5).unit());
        Face f23 = new Face(nodesFace3, VTKType.VTK_TRIANGLE, surface23, cell, cellNeigh3, numVars);

        List<Face> faceList1 = List.of(f11, f12);
        List<Face> faceList2 = List.of(f23, f22);

        assertFalse(containsSameFaces(faceList1, faceList2, 1e-15));
    }

    @Test
    public void cell_lists_with_same_cells_in_any_order_are_same() {
        Random rand = new Random(19);
        int numVars = 5;
        Cell cell11 = createArbitraryCell(123, numVars);
        Face face111 = createArbirtaryFace(98, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face112 = createArbirtaryFace(45, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face113 = createArbirtaryFace(78, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell11.faces.addAll(List.of(face111, face112, face113));

        Cell cell12 = createArbitraryCell(54, numVars);
        Face face121 = createArbirtaryFace(91, cell12,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face122 = createArbirtaryFace(5, cell12,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face123 = createArbirtaryFace(9, cell12,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell12.faces.addAll(List.of(face121, face122, face123));

        Cell cell13 = createArbitraryCell(56, numVars);
        Face face131 = createArbirtaryFace(1, cell13,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face132 = createArbirtaryFace(50, cell13,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face133 = createArbirtaryFace(49, cell13,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell13.faces.addAll(List.of(face131, face132, face133));


        rand = new Random(19);
        Cell cell21 = createArbitraryCell(123, numVars);
        Face face211 = createArbirtaryFace(98, cell21,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face212 = createArbirtaryFace(45, cell21,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face213 = createArbirtaryFace(78, cell21,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell21.faces.addAll(List.of(face211, face212, face213));

        Cell cell22 = createArbitraryCell(54, numVars);
        Face face221 = createArbirtaryFace(91, cell22,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face222 = createArbirtaryFace(5, cell22,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face223 = createArbirtaryFace(9, cell22,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell22.faces.addAll(List.of(face221, face222, face223));

        Cell cell23 = createArbitraryCell(56, numVars);
        Face face231 = createArbirtaryFace(1, cell23,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face232 = createArbirtaryFace(50, cell23,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face233 = createArbirtaryFace(49, cell23,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell23.faces.addAll(List.of(face231, face232, face233));

        assertTrue(containsSameCells(List.of(cell11, cell12, cell13),
                List.of(cell22, cell23, cell21), 1e-15));
    }

    @Test
    public void cell_lists_of_different_lengths_are_not_same() {
        Random rand = new Random(19);
        int numVars = 5;
        Cell cell11 = createArbitraryCell(123, numVars);
        Face face111 = createArbirtaryFace(98, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face112 = createArbirtaryFace(45, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face113 = createArbirtaryFace(78, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell11.faces.addAll(List.of(face111, face112, face113));

        Cell cell12 = createArbitraryCell(54, numVars);
        Face face121 = createArbirtaryFace(91, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face122 = createArbirtaryFace(5, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face123 = createArbirtaryFace(9, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell12.faces.addAll(List.of(face121, face122, face123));

        Cell cell13 = createArbitraryCell(56, numVars);
        Face face131 = createArbirtaryFace(1, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face132 = createArbirtaryFace(50, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face133 = createArbirtaryFace(49, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell13.faces.addAll(List.of(face131, face132, face133));


        rand = new Random(19);
        Cell cell21 = createArbitraryCell(123, numVars);
        Face face211 = createArbirtaryFace(98, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face212 = createArbirtaryFace(45, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face213 = createArbirtaryFace(78, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell21.faces.addAll(List.of(face211, face212, face213));

        Cell cell22 = createArbitraryCell(54, numVars);
        Face face221 = createArbirtaryFace(91, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face222 = createArbirtaryFace(5, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face223 = createArbirtaryFace(9, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell22.faces.addAll(List.of(face221, face222, face223));

        Cell cell23 = createArbitraryCell(56, numVars);
        Face face231 = createArbirtaryFace(1, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face232 = createArbirtaryFace(50, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face233 = createArbirtaryFace(49, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell23.faces.addAll(List.of(face231, face232, face233));

        assertFalse(containsSameCells(List.of(cell11, cell12, cell13),
                List.of(cell22, cell23), 1e-15));
    }

    @Test
    public void cell_lists_with_different_cells_are_not_same() {
        Random rand = new Random(19);
        int numVars = 5;
        Cell cell11 = createArbitraryCell(123, numVars);
        Face face111 = createArbirtaryFace(98, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face112 = createArbirtaryFace(45, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face113 = createArbirtaryFace(78, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell11.faces.addAll(List.of(face111, face112, face113));

//        Cell cell12 = createArbitraryCell(54, numVars);
//        Face face121 = createArbirtaryFace(91, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        Face face122 = createArbirtaryFace(5, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        Face face123 = createArbirtaryFace(9, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        cell12.faces.addAll(List.of(face121, face122, face123));

        Cell cell13 = createArbitraryCell(56, numVars);
        Face face131 = createArbirtaryFace(1, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face132 = createArbirtaryFace(50, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face133 = createArbirtaryFace(49, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell13.faces.addAll(List.of(face131, face132, face133));


        rand = new Random(19);
        Cell cell21 = createArbitraryCell(123, numVars);
        Face face211 = createArbirtaryFace(98, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face212 = createArbirtaryFace(45, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face213 = createArbirtaryFace(78, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell21.faces.addAll(List.of(face211, face212, face213));

        Cell cell22 = createArbitraryCell(54, numVars);
        Face face221 = createArbirtaryFace(91, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face222 = createArbirtaryFace(5, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        Face face223 = createArbirtaryFace(9, cell11,
                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
        cell22.faces.addAll(List.of(face221, face222, face223));

//        Cell cell23 = createArbitraryCell(56, numVars);
//        Face face231 = createArbirtaryFace(1, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        Face face232 = createArbirtaryFace(50, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        Face face233 = createArbirtaryFace(49, cell11,
//                createArbitraryCell(rand.nextInt(1220), numVars), numVars);
//        cell23.faces.addAll(List.of(face231, face232, face233));

        assertFalse(containsSameCells(List.of(cell11, cell13),
                List.of(cell22, cell21), 1e-15));
    }

    private Face createArbirtaryFace(int seed, Cell left, Cell right, int numVars) {
        Random rand = new Random(seed);
        Surface surface = new Surface(
                rand.nextDouble() * 100 - 50,
                new Point(
                        rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50),
                new Vector(
                        rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50));

        int numNodes = rand.nextInt(3) + 2;

        Node[] nodes = new Node[numNodes];
        for (int i = 0; i < numNodes; i++) {
            nodes[i] = new Node(rand.nextDouble() * 100 - 50,
                    rand.nextDouble() * 100 - 50,
                    rand.nextDouble() * 100 - 50);
        }

        VTKType[] vtkTypes = {VTKType.VTK_LINE, VTKType.VTK_TRIANGLE, VTKType.VTK_QUAD};
        VTKType vtkType = vtkTypes[rand.nextInt(vtkTypes.length)];

        return new Face(nodes, vtkType, surface, left, right, numVars);
    }

    private Cell createArbitraryCell(int seed, int numVars) {
        Random rand = new Random(seed);

        int numNodes = rand.nextInt(3) + 2;

        Node[] nodes = new Node[numNodes];
        for (int i = 0; i < numNodes; i++) {
            nodes[i] = new Node(rand.nextDouble() * 100 - 50,
                    rand.nextDouble() * 100 - 50,
                    rand.nextDouble() * 100 - 50);
        }

        VTKType[] vtkTypes = {VTKType.VTK_LINE, VTKType.VTK_TRIANGLE, VTKType.VTK_QUAD, VTKType.VTK_HEXAHEDRON, VTKType.VTK_WEDGE, VTKType.VTK_TETRA};
        VTKType vtkType = vtkTypes[rand.nextInt(vtkTypes.length)];

        Shape shape = new Shape(rand.nextDouble() * 100 - 50,
                new Point(rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50,
                        rand.nextDouble() * 100 - 50));
        return new Cell(rand.nextInt(123456), nodes, vtkType, shape, numVars);
    }
}
