package main.mesh.factory;

import main.util.TestHelper;
import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Vertex;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Structured1DMeshTest {

    private static int numVars = 3;
    private static List<Node> expectedNodes;
    private static List<Face> expectedInternalFaces;
    private static List<Cell> expectedCells;
    private static List<Boundary> expectedBoundaries;

    private static BoundaryCondition dummyBC = new BoundaryCondition() {
        @Override
        public void setGhostCellValues(Face face) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public double[] convectiveFlux(Face face) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    };

    private static Mesh actualMesh;


    @BeforeClass
    public static void setUp() throws FileNotFoundException {
        Node[] nodes = new Node[7];
        nodes[0] = new Node(-3.25, -0.25, 0.0, numVars); // Ghost node
        nodes[1] = new Node(-2.0, 0, 0, numVars);
        nodes[2] = new Node(-0.75, 0.25, 0, numVars);
        nodes[3] = new Node(0.5, 0.5, 0, numVars);
        nodes[4] = new Node(1.75, 0.75, 0, numVars);
        nodes[5] = new Node(3.0, 1.0, 0, numVars);
        nodes[6] = new Node(4.25, 1.25, 0.0, numVars); // Ghost node
        expectedNodes = Arrays.asList(nodes).subList(1, nodes.length - 1);

        Cell[] cells = new Cell[nodes.length - 1];
        for (int i = 0; i < cells.length; i++) {
            Node[] cellNodes = {nodes[i], nodes[i + 1]};
            Geometry cellGeom = new Line(new Point(cellNodes[0].x, cellNodes[0].y, cellNodes[0].z),
                    new Point(cellNodes[1].x, cellNodes[1].y, cellNodes[1].z));
            Shape shape = new Shape(cellGeom.length() * 1.0 * 1.0, cellGeom.centroid());
            int index = (i == 0 || i == cells.length - 1) ? -1 : i - 1;
            cells[i] = new Cell(cellNodes, VTKType.VTK_LINE, shape, numVars);
            cells[i].setIndex(index);
        }
        expectedCells = Arrays.asList(cells).subList(1, cells.length - 1);

        Face[] faces = new Face[nodes.length - 2];
        for (int i = 1; i < nodes.length - 1; i++) {
            Point leftPoint = new Point(nodes[i - 1].x, nodes[i - 1].y, nodes[i - 1].z);
            Point iPoint = new Point(nodes[i].x, nodes[i].y, nodes[i].z);
            Point rightPoint = new Point(nodes[i + 1].x, nodes[i + 1].y, nodes[i + 1].z);

            Geometry faceGeom = new Vertex(iPoint);

            Vector faceNormal = new Vector(leftPoint, iPoint).add(new Vector(iPoint, rightPoint));
            if (i == 1) {
                faceNormal = faceNormal.mult(-1);
            }
            Vector unitNormal = faceNormal.unit();

            Surface surface = new Surface(1.0 * 1.0, faceGeom.centroid(), unitNormal);

            Cell left = cells[i - 1];
            Cell right = cells[i];
            if (i == 1) {
                left = cells[i];
                right = cells[i - 1];
            }

            faces[i - 1] = new Face(new Node[]{nodes[i]}, VTKType.VTK_VERTEX, surface, left, right, numVars);
        }
        expectedInternalFaces = Arrays.asList(faces).subList(1, faces.length - 1);

        Boundary xi_minBoundary = new Boundary("xi min", List.of(faces[0]), dummyBC);
        Boundary xi_maxBoundary = new Boundary("xi max", List.of(faces[faces.length - 1]), dummyBC);
        expectedBoundaries = Arrays.asList(xi_minBoundary, xi_maxBoundary);

        // Setup node neighbours
        for (int i = 1; i < nodes.length - 1; i++) {
            if (i == 1) {
                nodes[i].neighbors.add(cells[i]);
                nodes[i].neighbors.add(cells[i - 1]);
            } else {
                nodes[i].neighbors.add(cells[i - 1]);
                nodes[i].neighbors.add(cells[i]);
            }
        }

        // Setup faces of cells
        for (int i = 1; i < cells.length - 1; i++) {
            if (i == 1) {
                cells[i].faces.add(faces[i]);
                cells[i].faces.add(faces[i - 1]);
            } else {
                cells[i].faces.add(faces[i - 1]);
                cells[i].faces.add(faces[i]);
            }
        }

        actualMesh = new Structured1DMesh(new File("test/test_data/mesh_structured_1d.cfds"), numVars, dummyBC, dummyBC);
    }

    private static void assertNodeEquals(Node expected, Node actual) {
        // Has same location
        Point expectedPoint = new Point(expected.x, expected.y, expected.z);
        Point actualPoint = new Point(actual.x, actual.y, actual.z);
        assertEquals(0, expectedPoint.distance(actualPoint), 1e-15);

        // Has same neighbors
        assertEquals(expected.neighbors.size(), actual.neighbors.size());
        for (int i = 0; i < expected.neighbors.size(); i++) {
            Cell expectedCell = expected.neighbors.get(i);
            Cell actualCell = actual.neighbors.get(i);
            assertShapeEquals(expectedCell.shape, actualCell.shape);
        }
    }

    private static void assertSurfaceEquals(Surface expected, Surface actual) {
        // Have same area
        assertEquals(expected.area, actual.area, 1e-15);

        // Have same centroid
        assertEquals(0, expected.centroid.distance(actual.centroid), 1e-15);

        // Have same unit normal
        assertEquals(0, expected.unitNormal().sub(actual.unitNormal()).mag(), 1e-15);
    }

    private static void assertShapeEquals(Shape expected, Shape actual) {
        // volume equals
        assertEquals(expected.volume, actual.volume, 1e-15);

        // centroid equals
        assertEquals(0, expected.centroid.distance(actual.centroid), 1e-15);
    }

    private static void assertFaceEquals(Face expected, Face actual) {
        // Have same nodes in same order
        assertEquals(expected.nodes.length, actual.nodes.length);
        for (int i = 0; i < expected.nodes.length; i++) {
            assertNodeEquals(expected.nodes[i], actual.nodes[i]);
        }

        // Have same vtk type
        assertEquals(expected.vtkType, actual.vtkType);

        // Have same surface
        assertSurfaceEquals(expected.surface, actual.surface);

        // Have same left and right cells
        assertShapeEquals(expected.left.shape, actual.left.shape);
        assertShapeEquals(expected.right.shape, actual.right.shape);

        // Have same number of variables
        assertEquals(numVars, actual.flux.length);
    }

    private static void assertCellEquals(Cell expected, Cell actual) {
        // Have same index
        assertEquals(expected.index(), actual.index());

        // Have same nodes in same order
        assertEquals(expected.nodes.length, actual.nodes.length);
        for (int i = 0; i < expected.nodes.length; i++) {
            assertNodeEquals(expected.nodes[i], actual.nodes[i]);
        }

        // Have same faces
        assertEquals(expected.faces.size(), actual.faces.size());
        for (int i = 0; i < expected.faces.size(); i++) {
            assertFaceEquals(expected.faces.get(i), actual.faces.get(i));
        }

        // Have same vtk type
        assertEquals(expected.vtkType, actual.vtkType);

        // Have same shape
        assertShapeEquals(expected.shape, actual.shape);

        // Have same number of variables
        assertEquals(numVars, actual.U.length);
        assertEquals(numVars, actual.residual.length);
    }

    private static void assertBoundaryEquals(Boundary expected, Boundary actual) {
        // Have same name
        assertEquals(expected.name, actual.name);

        // Have same faces
        assertEquals(expected.faces.size(), actual.faces.size());
        for (int i = 0; i < expected.faces.size(); i++) {
            assertFaceEquals(expected.faces.get(i), actual.faces.get(i));
        }

        // Have same boundary condition
        assertEquals(expected.bc().orElseThrow().getClass(), actual.bc().orElseThrow().getClass());
    }

    @Test
    public void cells() {
        List<Cell> actualCells = actualMesh.cells();
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i));
        }
    }

    @Test
    public void internalFaces() {
        List<Face> actualInternalFaces = actualMesh.internalFaces();
        assertEquals(expectedInternalFaces.size(), actualInternalFaces.size());
        for (int i = 0; i < expectedInternalFaces.size(); i++) {
            assertFaceEquals(expectedInternalFaces.get(i), actualInternalFaces.get(i));
        }
    }

    @Test
    public void nodes() {
        List<Node> actualNodes = actualMesh.nodes();
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i));
        }
    }

    @Test
    public void boundaries() {
        assertEquals(expectedBoundaries.size(), actualMesh.boundaries().size());
        for (int i = 0; i < expectedBoundaries.size(); i++) {
            assertBoundaryEquals(expectedBoundaries.get(i), actualMesh.boundaries().get(i));
        }
    }

    @Test
    public void indexTest() {
        List<Cell> cells = actualMesh.cells();
        assertTrue(IntStream.range(0, cells.size())
                .allMatch(i -> cells.get(i).index() == i));

        List<Face> allFaceList = Stream.concat(actualMesh.internalFaceStream(),
                actualMesh.boundaryStream().flatMap(b -> b.faces.stream())).collect(Collectors.toList());
        assertTrue(IntStream.range(0, allFaceList.size())
                .allMatch(i -> allFaceList.get(i).index() == i));
    }

    @Test
    public void exceptionTest() {
        File doesNotExist = new File("test/test_data/doesNotExist.cfds");
        TestHelper.assertThrows(FileNotFoundException.class,
                () -> new Structured1DMesh(doesNotExist, numVars, dummyBC, dummyBC));
    }
}
