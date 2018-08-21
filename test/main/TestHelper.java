package main;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    public static void assertFaceEquals(Face expected, Face actual, double tolerance) {
        assertTrue(sameFaces(expected, actual, tolerance));
    }

    private static boolean sameCells(Cell expected, Cell actual, double tolerance) {
        // has same index
        if (expected.index != actual.index) {
            System.err.println("Expected cell index: " + expected.index + "\n" +
                    "Actual cell index: " + actual.index);

            return false;
        }

        // has same shape
        if (!sameShape(expected.shape, actual.shape, tolerance)) {
            System.err.println("Expected shape: " + expected.shape + "\n" +
                    "Actual shape: " + actual.shape);
            return false;
        }

        // has same nodes
        if (!containsSameNodes(List.of(expected.nodes), List.of(actual.nodes), tolerance)) {
            System.err.println("Cell does not have same nodes.");
            return false;
        }

        // has same vtk type
        if (expected.vtkType != actual.vtkType) {
            System.err.println("Expected cell vtk type: " + expected.vtkType + "\n" +
                    "Actual cell vtk type: " + actual.vtkType);
            return false;
        }

        // has same faces
        if (!containsSameFaces(expected.faces, actual.faces, tolerance)) {
            System.err.println("Cell does not have same faces.");
            return false;
        }

        // has same lengths of arrays
        int numVars = expected.U.length;
        if (numVars != actual.U.length) {
            System.err.println("Expected variable array length: " + numVars + "\n" +
                    "Actual variable array length: " + actual.U.length);
            return false;
        }
        if (numVars != actual.residual.length) {
            System.err.println("Expected residual array length: " + numVars + "\n" +
                    "Actual residual array length: " + actual.residual.length);
            return false;
        }
        if (numVars != actual.reconstructCoeffs.length) {
            System.err.println("Expected reconstructCoeffs array length: " + numVars + "\n" +
                    "Actual reconstructCoeffs array length: " + actual.reconstructCoeffs.length);
            return false;
        }

        return true;
    }

    private static boolean sameShape(Shape expected, Shape actual, double tolerance) {
        // has same volume
        if (Math.abs(expected.volume - actual.volume) > tolerance) {
            System.err.println("Expected volume: " + expected.volume + "\n" +
                    "Actual volume: " + actual.volume);
            return false;
        }

        // has same centroid
        if (expected.centroid.distance(actual.centroid) > tolerance) {
            System.err.println("Expected centroid: " + expected.centroid + "\n" +
                    "Actual centroid: " + actual.centroid);
            return false;
        }

        return true;
    }

    private static boolean containsSameCells(List<Cell> expected, List<Cell> actual, double tolerance) {
        if (expected.size() != actual.size()) {
            System.err.println("Number of expected cells: " + expected.size() + "\n" +
                    "Number of actual cells: " + actual.size());
            return false;
        }

        for (Cell ce : expected) {
            if (!contains(actual, ce, tolerance)) {
                System.err.println("Actual cell list does not contain expected : " + ce);
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
        if (expected.size() != actual.size()) {
            System.err.println("Number of expected nodes: " + expected.size() + "\n" +
                    "Number of actual nodes: " + actual.size());
            return false;
        }

        for (Node ne : expected) {
            if (!contains(actual, ne, tolerance)) {
                System.err.println("Actual node list does not contain expected : " + ne);
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
        if (expected.size() != actual.size()) {
            System.err.println("Number of expected faces: " + expected.size() + "\n" +
                    "Number of actual faces: " + actual.size());
            return false;
        }

        for (Face fe : expected) {
            if (!contains(actual, fe, tolerance)) {
                System.err.println("Actual face list does not contain expected : " + fe);
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
        if (containsSameNodes(List.of(expected.nodes), List.of(actual.nodes), tolerance)) {
            System.err.println("The face does not contain expected nodes.");
            return false;
        }

        // Has same vtk type
        if (expected.vtkType != actual.vtkType) {
            System.err.println("Expected face vtk type: " + expected.vtkType + "\n" +
                    "Actual face vtk type: " + actual.vtkType);
            return false;
        }

        // Has same surface
        int direction = compareSurfaces(expected.surface, actual.surface, tolerance);
        if (direction == 0) {
            System.err.println("Expected: " + expected.surface + "\n" +
                    "Actual: " + actual.surface);
            return false;
        }

        // Has same left and right cells
        if (direction == 1) {
            return sameCells(expected.left, actual.left, tolerance) && sameCells(expected.right, actual.right, tolerance);
        }
        if (direction == -1) {
            return sameCells(expected.left, actual.right, tolerance) && sameCells(expected.right, actual.left, tolerance);
        }

        // Has same length of arrays
        int numVars = expected.U.length;
        if (numVars != actual.U.length) {
            System.err.println("Number of expected face variables: " + numVars + "\n" +
                    "Number of actual face variables: " + actual.U.length);
            return false;
        }
        if (numVars != actual.flux.length) {
            System.err.println("Expected flux array length: " + numVars + "\n" +
                    "Actual flux array length: " + actual.flux.length);
            return false;
        }

        return true;
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
}
