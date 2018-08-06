package main.mesh.factory;

import main.TestHelper;
import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Quad;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static main.geom.VTKType.VTK_LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Structured2DMeshTest {

    private static final int numVars = 5;
    private static final int num_xi = 5, num_eta = 4;
    private static Point pa = new Point(0, 0, 0);
    private static Point pb = new Point(0, 3, 0);
    private static Point pc = new Point(4, 3, 0);
    private static Point pd = new Point(4, 0, 0);

    private static List<Cell> expectedCells;
    private static List<Face> expectedInternalFaces;
    private static List<Node> expectedNodes;
    private static List<Boundary> expectedBoundaries;

    private static BoundaryCondition dummyBC = new BoundaryCondition() {
        @Override
        public void setGhostCellValues(Face face, double time) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public double[] convectiveFlux(Face face, double time) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    };

    @BeforeClass
    public static void setUp() {
        Node[][] nodeArray = new Node[num_xi][num_eta];
        Cell[][] cellArray = new Cell[num_xi - 1][num_eta - 1];

        expectedNodes = new ArrayList<>();
        for (int i = 0; i < num_xi; i++) {
            for (int j = 0; j < num_eta; j++) {
                Node node = new Node(transform(i, j));
                nodeArray[i][j] = node;
                expectedNodes.add(node);
            }
        }

        expectedCells = new ArrayList<>();
        int cellIndex = 0;
        for (int i = 0; i < num_xi - 1; i++) {
            for (int j = 0; j < num_eta - 1; j++) {
                Node[] n = new Node[]{nodeArray[i][j], nodeArray[i + 1][j], nodeArray[i + 1][j + 1], nodeArray[i][j + 1]};
                Geometry cellGeom = new Quad(n[0].location(), n[1].location(),
                        n[2].location(), n[3].location());
                Cell cell = new Cell(cellIndex, n, VTKType.VTK_QUAD,
                        new Shape(cellGeom.area(), cellGeom.centroid()), numVars);
                cellArray[i][j] = cell;
                expectedCells.add(cell);
                cellIndex++;
            }
        }

        expectedInternalFaces = new ArrayList<>();
        // Vertical faces
        for (int i = 1; i < num_xi - 1; i++) {
            for (int j = 0; j < num_eta - 1; j++) {
                Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i][j + 1].location());
                Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i][j + 1]}, VTK_LINE,
                        new Surface(1, faceGeom.centroid(), new Vector(1, 0, 0)),
                        cellArray[i - 1][j], cellArray[i][j], numVars);
                expectedInternalFaces.add(face);
            }
        }

        // Horizontal faces
        for (int j = 1; j < num_eta - 1; j++) {
            for (int i = 0; i < num_xi - 1; i++) {
                Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i + 1][j].location());
                Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i + 1][j]}, VTK_LINE,
                        new Surface(1, faceGeom.centroid(), new Vector(0, 1, 0)),
                        cellArray[i][j - 1], cellArray[i][j], numVars);
                expectedInternalFaces.add(face);
            }
        }

        // Boundaries
        int i, j;
        Boundary xi_minBoundary = new Boundary("xi min", new ArrayList<>(), dummyBC);
        i = 0;
        for (j = 0; j < num_eta - 1; j++) {
            Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i][j + 1].location());
            Cell innerCell = cellArray[i][j];
            Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i][j + 1]}, VTK_LINE,
                    new Surface(1, faceGeom.centroid(), new Vector(-1, 0, 0)),
                    innerCell, null, numVars);
            face.right = Mesh.ghostCell(innerCell, face);
            xi_minBoundary.faces.add(face);
        }

        Boundary xi_maxBoundary = new Boundary("xi max", new ArrayList<>(), dummyBC);
        i = num_xi - 1;
        for (j = 0; j < num_eta - 1; j++) {
            Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i][j + 1].location());
            Cell innerCell = cellArray[i - 1][j];
            Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i][j + 1]}, VTK_LINE,
                    new Surface(1, faceGeom.centroid(), new Vector(1, 0, 0)),
                    innerCell, null, numVars);
            face.right = Mesh.ghostCell(innerCell, face);
            xi_maxBoundary.faces.add(face);
        }

        Boundary eta_minBoundary = new Boundary("eta min", new ArrayList<>(), dummyBC);
        j = 0;
        for (i = 0; i < num_xi - 1; i++) {
            Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i + 1][j].location());
            Cell innerCell = cellArray[i][j];
            Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i + 1][j]}, VTK_LINE,
                    new Surface(1, faceGeom.centroid(), new Vector(0, -1, 0)),
                    innerCell, null, numVars);
            face.right = Mesh.ghostCell(innerCell, face);
            eta_minBoundary.faces.add(face);
        }

        Boundary eta_maxBoundary = new Boundary("eta max", new ArrayList<>(), dummyBC);
        j = num_eta - 1;
        for (i = 0; i < num_xi - 1; i++) {
            Geometry faceGeom = new Line(nodeArray[i][j].location(), nodeArray[i + 1][j].location());
            Cell innerCell = cellArray[i][j - 1];
            Face face = new Face(new Node[]{nodeArray[i][j], nodeArray[i + 1][j]}, VTK_LINE,
                    new Surface(1, faceGeom.centroid(), new Vector(0, 1, 0)),
                    innerCell, null, numVars);
            face.right = Mesh.ghostCell(innerCell, face);
            eta_maxBoundary.faces.add(face);
        }

        expectedBoundaries = List.of(xi_minBoundary, xi_maxBoundary, eta_minBoundary, eta_maxBoundary);

        // Set node neighbors
        for (Cell cell : expectedCells) {
            for (Node node : cell.nodes) {
                node.neighbors.add(cell);
            }
        }
        for (Boundary bnd : expectedBoundaries) {
            for (Face face : bnd.faces) {
                Cell ghostCell = face.right;
                for (Node node : ghostCell.nodes) {
                    node.neighbors.add(ghostCell);
                }
            }
        }

        // Set cell faces
        for (Face face : expectedInternalFaces) {
            face.left.faces.add(face);
            face.right.faces.add(face);
        }
        for (Boundary bnd : expectedBoundaries) {
            for (Face face : bnd.faces) {
                face.left.faces.add(face);
                face.right.faces.add(face);
            }
        }
    }

    private static Point transform(int i, int j) {
        Vector va = pa.toVector();
        Vector vb = pb.toVector();
        Vector vc = pc.toVector();
        Vector vd = pd.toVector();

        double xi = i / (num_xi - 1.0);
        double eta = j / (num_eta - 1.0);

        // Using transfinite interpolation
        return xi_0(va, vb, vc, vd, xi, eta).mult((1 - xi))
                .add(xi_1(va, vb, vc, vd, xi, eta).mult(xi))
                .add(eta_0(va, vb, vc, vd, xi, eta).mult(1 - eta))
                .add(eta_1(va, vb, vc, vd, xi, eta).mult(eta))
                .sub(eta_0(va, vb, vc, vd, 0, eta).mult((1 - xi) * (1 - eta)))
                .sub(eta_1(va, vb, vc, vd, 0, eta).mult(eta * (1 - xi)))
                .sub(eta_0(va, vb, vc, vd, 1, eta).mult((1 - eta) * xi))
                .sub(eta_1(va, vb, vc, vd, 1, eta).mult(xi * eta))
                .toPoint();
    }

    private static Vector xi_0(Vector va, Vector vb, Vector vc, Vector vd, double xi, double eta) {
        return interpolate(va, vb, eta);
    }

    private static Vector xi_1(Vector va, Vector vb, Vector vc, Vector vd, double xi, double eta) {
        return interpolate(vd, vc, eta);
    }

    private static Vector eta_0(Vector va, Vector vb, Vector vc, Vector vd, double xi, double eta) {
        return interpolate(va, vd, xi);
    }

    private static Vector eta_1(Vector va, Vector vb, Vector vc, Vector vd, double xi, double eta) {
        return interpolate(vb, vc, xi);
    }

    private static Vector interpolate(Vector v1, Vector v2, double ratio) {
        return v1.mult(1 - ratio).add(v2.mult(ratio));
    }

    private static boolean hasEquivalentNodes(List<Node> ln1, List<Node> ln2) {
        if (ln1.size() != ln2.size()) return false;

        for (Node n1 : ln1) {
            boolean found = false;
            for (Node n2 : ln2) {
                if (equivalentNodes(n1, n2)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean equivalentNodes(Node n1, Node n2) {
        return n1.location().distance(n2.location()) < 1e-15;
    }

    private static boolean equivalentShapes(Shape s1, Shape s2) {
        return Math.abs(s1.volume - s2.volume) < 1e-15 &&
                s1.centroid.distance(s2.centroid) < 1e-15;
    }

    private static int hasEquivalentNeighbors(Face f1, Face f2) {
        boolean ll_rr = equivalentShapes(f1.left.shape, f2.left.shape) && equivalentShapes(f1.right.shape, f2.right.shape);
        boolean lr_rl = equivalentShapes(f1.left.shape, f2.right.shape) && equivalentShapes(f1.right.shape, f2.left.shape);

        if (ll_rr) return 1;
        if (lr_rl) return -1;
        return 0;
    }

    private static boolean hasEquivalentFaces(List<Face> lf1, List<Face> lf2) {
        if (lf1.size() != lf2.size()) return false;

        for (Face f1 : lf1) {
            boolean found = false;
            for (Face f2 : lf2) {
                if (equivalentFaces(f1, f2)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean equivalentFaces(Face f1, Face f2) {
        // Have same number of nodes
        if (f1.nodes.length != f2.nodes.length) return false;

        // Have nodes at same location
        if (!hasEquivalentNodes(List.of(f1.nodes), List.of(f2.nodes))) return false;

        // Have same left and right cell shapes
        int direction = hasEquivalentNeighbors(f1, f2);
        if (direction == 0) return false;

        // Have same surface
        Surface s1 = f1.surface;
        Surface s2 = f2.surface;
        if (Math.abs(s1.area - s2.area) > 1e-15) return false;
        if (s1.centroid.distance(s2.centroid) > 1e-15) return false;
        return s1.unitNormal
                .sub(s2.unitNormal.mult(direction))
                .mag() < 1e-15;
    }

    private static void assertPointEquals(Point expectedPoint, Point actualPoint) {
        assertEquals(0, expectedPoint.distance(actualPoint), 1e-15);
    }

    private static void assertNodeEquals(Node expectedNode, Node actualNode) {
        // Location
        assertPointEquals(expectedNode.location(), actualNode.location());

        // Neighbors
        assertEquals(expectedNode.neighbors.size(), actualNode.neighbors.size());
        for (int i = 0; i < expectedNode.neighbors.size(); i++) {
            assertShapeEquals(expectedNode.neighbors.get(i).shape, actualNode.neighbors.get(i).shape);
        }
    }

    private static void assertShapeEquals(Shape expectedShape, Shape actualShape) {
        // volume equals
        assertEquals(expectedShape.volume, actualShape.volume, 1e-15);

        // centroid equals
        assertPointEquals(expectedShape.centroid, actualShape.centroid);
    }

    private static void assertCellEquals(Cell expectedCell, Cell actualCell) {
        // index equal
        assertEquals(expectedCell.index, actualCell.index);

        // nodes equal
        assertEquals(expectedCell.nodes.length, actualCell.nodes.length);
        for (int i = 0; i < expectedCell.nodes.length; i++) {
            assertNodeEquals(expectedCell.nodes[i], actualCell.nodes[i]);
        }

        // faces equal
        assertEquals(expectedCell.faces.size(), actualCell.faces.size());
        assertTrue(hasEquivalentFaces(expectedCell.faces, actualCell.faces));

        // vtkType equal
        assertEquals(expectedCell.vtkType, actualCell.vtkType);

        // shape equal
        assertShapeEquals(expectedCell.shape, actualCell.shape);

        // U length equal
        assertEquals(expectedCell.U.length, actualCell.U.length);

        // residual length equal
        assertEquals(expectedCell.residual.length, actualCell.residual.length);
    }

    private static void assertBoundaryEquals(Boundary bnd1, Boundary bnd2) {
        // Has same name
        assertEquals(bnd1.name, bnd2.name);

        // Has same faces
        assertTrue(hasEquivalentFaces(bnd1.faces, bnd2.faces));

        // Has same bc
        assertEquals(bnd1.bc, bnd2.bc);
    }

    @Test
    public void cells() {
        try {
            Mesh actualMesh = new Structured2DMesh(new File("test/test_data/mesh_structured_2d.cfds"), numVars,
                    dummyBC, dummyBC, dummyBC, dummyBC);
            List<Cell> actualCells = actualMesh.cells();
            assertEquals(expectedCells.size(), actualCells.size());
            for (int i = 0; i < expectedCells.size(); i++) {
                assertCellEquals(expectedCells.get(i), actualCells.get(i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void internalFaces() {
        try {
            Mesh actualMesh = new Structured2DMesh(new File("test/test_data/mesh_structured_2d.cfds"), numVars,
                    dummyBC, dummyBC, dummyBC, dummyBC);
            List<Face> actualInternalFaces = actualMesh.internalFaces();
            assertEquals(expectedInternalFaces.size(), actualInternalFaces.size());
            assertTrue(hasEquivalentFaces(expectedInternalFaces, actualInternalFaces));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nodes() {
        try {
            Mesh actualMesh = new Structured2DMesh(new File("test/test_data/mesh_structured_2d.cfds"), numVars,
                    dummyBC, dummyBC, dummyBC, dummyBC);
            List<Node> actualNodes = actualMesh.nodes();
            assertEquals(expectedNodes.size(), actualNodes.size());
            for (int i = 0; i < expectedNodes.size(); i++) {
                assertNodeEquals(expectedNodes.get(i), actualNodes.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void boundaries() {
        try {
            Mesh actualMesh = new Structured2DMesh(new File("test/test_data/mesh_structured_2d.cfds"), numVars,
                    dummyBC, dummyBC, dummyBC, dummyBC);
            List<Boundary> actualBoundaries = actualMesh.boundaries();
            assertEquals(expectedBoundaries.size(), actualBoundaries.size());
            for (int i = 0; i < expectedBoundaries.size(); i++) {
                assertBoundaryEquals(expectedBoundaries.get(i), actualBoundaries.get(i));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void indexTest() throws FileNotFoundException {
        Mesh actualMesh = new Structured2DMesh(new File("test/test_data/mesh_structured_2d.cfds"), numVars,
                dummyBC, dummyBC, dummyBC, dummyBC);
        List<Cell> cells = actualMesh.cells();
        assertTrue(IntStream.range(0, cells.size())
                .allMatch(i -> cells.get(i).index == i));
    }

    @Test
    public void exceptionTest() {
        File doesNotExist = new File("test/test_data/doesNotExist.cfds");
        TestHelper.assertThrows(FileNotFoundException.class,
                () -> new Structured2DMesh(doesNotExist, numVars, dummyBC, dummyBC, dummyBC, dummyBC));
    }
}