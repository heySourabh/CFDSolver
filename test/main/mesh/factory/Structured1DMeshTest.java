package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Vertex;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Structured1DMeshTest {

    private static int numVars = 3;
    private static List<Node> expectedNodes;
    private static List<Face> expectedInternalFaces;
    private static List<Cell> expectedCells;
    private static List<Boundary> expectedBoundaries;

    private static BoundaryCondition dummyBC = new BoundaryCondition() {
        @Override
        public void setGhostCellValues(Face face) {
            throw new UnsupportedOperationException("No implemented.");
        }

        @Override
        public double[] convectiveFlux(Face face) {
            throw new UnsupportedOperationException("No implemented.");
        }
    };

    private static Mesh actualMesh;


    @BeforeAll
    static void setUp() throws FileNotFoundException {
        Node[] nodes = new Node[5];
        nodes[0] = new Node(-2.0, 0, 0);
        nodes[1] = new Node(-0.75, 0.25, 0);
        nodes[2] = new Node(0.5, 0.5, 0);
        nodes[3] = new Node(1.75, 0.75, 0);
        nodes[4] = new Node(3.0, 1.0, 0);
        expectedNodes = Arrays.asList(nodes);

        Cell[] cells = new Cell[nodes.length - 1];
        for (int i = 0; i < cells.length; i++) {
            Node[] cellNodes = {nodes[i], nodes[i + 1]};
            Geometry cellGeom = new Line(new Point(cellNodes[0].x, cellNodes[0].y, cellNodes[0].z),
                    new Point(cellNodes[1].x, cellNodes[1].y, cellNodes[1].z));
            Shape shape = new Shape(cellGeom.length() * 1.0 * 1.0, cellGeom.centroid());
            cells[i] = new Cell(i, cellNodes, VTKType.VTK_LINE, shape, numVars);
        }
        expectedCells = Arrays.asList(cells);

        Face[] internalFaces = new Face[nodes.length - 2];
        for (int i = 1; i < nodes.length - 1; i++) {
            Point leftPoint = new Point(nodes[i - 1].x, nodes[i - 1].y, nodes[i - 1].z);
            Point iPoint = new Point(nodes[i].x, nodes[i].y, nodes[i].z);
            Point rightPoint = new Point(nodes[i + 1].x, nodes[i + 1].y, nodes[i + 1].z);

            Geometry faceGeom = new Vertex(iPoint);

            Vector faceNormal = new Vector(leftPoint, iPoint).add(new Vector(iPoint, rightPoint));
            Vector unitNormal = faceNormal.unit();

            Surface surface = new Surface(1.0 * 1.0, faceGeom.centroid(), unitNormal);

            Cell left = cells[i - 1];
            Cell right = cells[i];

            internalFaces[i - 1] = new Face(new Node[]{nodes[i]}, VTKType.VTK_VERTEX, surface, left, right, numVars);
        }
        expectedInternalFaces = Arrays.asList(internalFaces);

        actualMesh = new Structured1DMesh(new File("test_data/mesh_structured_1d.dat"), numVars, dummyBC, dummyBC);
    }

    private static void assertNodeEquals(Node expected, Node actual) {
        Point p1 = new Point(expected.x, expected.y, expected.z);
        Point p2 = new Point(actual.x, actual.y, actual.z);
        assertEquals(0, p1.distance(p2), 1e-15);
    }

    private static void assertSurfaceEquals(Surface expected, Surface actual) {
        // Have same area
        assertEquals(expected.area, actual.area, 1e-15);

        // Have same centroid
        assertEquals(0, expected.centroid.distance(actual.centroid), 1e-15);

        // Have same unit normal
        assertEquals(0, expected.unitNormal.sub(actual.unitNormal).mag(), 1e-15);
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

        // Have same left and right cells ???? Will cause Stackoverflow when face neighbors are added!!
        assertCellEquals(expected.left, actual.left);
        assertCellEquals(expected.right, actual.right);

        // Have same number of variables
        assertEquals(numVars, actual.flux.length);
    }

    private static void assertCellEquals(Cell expected, Cell actual) {
        // Have same index
        assertEquals(expected.index, actual.index);

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
        assertEquals(expected.bc.getClass(), actual.bc.getClass());
    }

    @Test
    void cells() {
        List<Cell> actualCells = actualMesh.cells();
        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i));
        }
    }

    @Test
    void internalFaces() {
        List<Face> actualInternalFaces = actualMesh.internalFaces();
        assertEquals(expectedInternalFaces.size(), actualInternalFaces.size());
        for (int i = 0; i < expectedInternalFaces.size(); i++) {
            assertFaceEquals(expectedInternalFaces.get(i), actualInternalFaces.get(i));
        }
    }

    @Test
    void nodes() {
        List<Node> actualNodes = actualMesh.nodes();
        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), actualNodes.get(i));
        }
    }

    @Test
    void boundaries() {

        // xi min boundary
        Node ni = expectedNodes.get(0);
        Point pi = new Point(ni.x, ni.y, ni.z);

        Node nip1 = expectedNodes.get(1);
        Point pip1 = new Point(nip1.x, nip1.y, nip1.z);

        Vector faceNormal = new Vector(pip1, pi);
        Vector unitNormal = faceNormal.unit();

        Point pim1 = new Point(ni.x + faceNormal.x, ni.y + faceNormal.y, ni.z + faceNormal.z);
        Node ghostNode = new Node(pim1);
        Geometry ghostCellGeom = new Line(pim1, pi);
        Shape shape = new Shape(ghostCellGeom.length() * 1.0 * 1.0, ghostCellGeom.centroid());
        Cell ghostCell = new Cell(-1, new Node[]{ghostNode, ni}, VTKType.VTK_LINE, shape, numVars);

        Geometry faceGeom = new Vertex(pi);

        Surface surface = new Surface(1.0 * 1.0, faceGeom.centroid(), unitNormal);
        Face face = new Face(new Node[]{ni}, VTKType.VTK_VERTEX, surface, expectedCells.get(0), ghostCell, numVars);
        Boundary xi_minBoundary = new Boundary("xi min", List.of(face), dummyBC);

        int xi = expectedNodes.size();
        // xi max boundary
        ni = expectedNodes.get(xi - 1);
        pi = new Point(ni.x, ni.y, ni.z);

        Node nim1 = expectedNodes.get(xi - 2);
        pim1 = new Point(nim1.x, nim1.y, nim1.z);

        faceNormal = new Vector(pim1, pi);
        unitNormal = faceNormal.unit();

        pip1 = new Point(ni.x + faceNormal.x, ni.y + faceNormal.y, ni.z + faceNormal.z);
        ghostNode = new Node(pip1);
        ghostCellGeom = new Line(pi, pip1);
        shape = new Shape(ghostCellGeom.length() * 1.0 * 1.0, ghostCellGeom.centroid());
        ghostCell = new Cell(-1, new Node[]{ni, ghostNode}, VTKType.VTK_LINE, shape, numVars);

        faceGeom = new Vertex(pi);

        surface = new Surface(1.0 * 1.0, faceGeom.centroid(), unitNormal);
        face = new Face(new Node[]{ni}, VTKType.VTK_VERTEX, surface, expectedCells.get(xi - 2), ghostCell, numVars);
        Boundary xi_maxBoundary = new Boundary("xi max", List.of(face), dummyBC);

        expectedBoundaries = List.of(xi_minBoundary, xi_maxBoundary);

        assertEquals(expectedBoundaries.size(), actualMesh.boundaries().size());
        for (int i = 0; i < expectedBoundaries.size(); i++) {
            assertBoundaryEquals(expectedBoundaries.get(i), actualMesh.boundaries().get(i));
        }
    }
}
