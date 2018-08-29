package main.mesh.factory;

import main.util.TestHelper;
import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Polygon;
import main.geom.factory.Quad;
import main.geom.factory.Triangle;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class Unstructured2DMeshTest {

    private static List<Node> expectedNodes;
    private static List<Cell> expectedCells;
    private static List<Face> expectedInternalFaces;
    private static List<Boundary> expectedBoundaries;

    private static int numVars = 3;

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
        List<Point> points = List.of(
                new Point(222.86, 427.14, 0.0),
                new Point(324.26, 383.86, 0.0),
                new Point(372.75, 432.85, 0.0),
                new Point(229.81, 551.54, 0.0),
                new Point(434.37, 575.28, 0.0),
                new Point(333.35, 732.36, 0.0),
                new Point(458.61, 486.89, 0.0),
                new Point(545.48, 417.19, 0.0),
                new Point(518.21, 661.65, 0.0),
                new Point(477.30, 743.47, 0.0),
                new Point(500.53, 548.01, 0.0)
        );
        expectedNodes = points.stream()
                .map(Node::new)
                .collect(toList());

        int[][] con = {
                {0, 1, 2, 3},
                {3, 2, 4},
                {2, 6, 7, 10, 4},
                {4, 10, 7, 8},
                {4, 8, 9, 5},
                {3, 4, 5}
        };
        Geometry[] cellGeom = {
                new Quad(points.get(con[0][0]), points.get(con[0][1]), points.get(con[0][2]), points.get(con[0][3])),
                new Triangle(points.get(con[1][0]), points.get(con[1][1]), points.get(con[1][2])),
                new Polygon(new Point[]{points.get(con[2][0]), points.get(con[2][1]), points.get(con[2][2]), points.get(con[2][3]), points.get(con[2][4])}),
                new Quad(points.get(con[3][0]), points.get(con[3][1]), points.get(con[3][2]), points.get(con[3][3])),
                new Quad(points.get(con[4][0]), points.get(con[4][1]), points.get(con[4][2]), points.get(con[4][3])),
                new Triangle(points.get(con[5][0]), points.get(con[5][1]), points.get(con[5][2]))
        };

        expectedCells = new ArrayList<>();
        for (int i = 0; i < con.length; i++) {
            Node[] n = Arrays.stream(con[i])
                    .mapToObj(j -> expectedNodes.get(j))
                    .toArray(Node[]::new);
            Geometry geom = cellGeom[i];
            Shape shape = new Shape(geom.area() * 1.0, geom.centroid());
            expectedCells.add(new Cell(i, n, geom.vtkType(), shape, numVars));
        }

        expectedInternalFaces = new ArrayList<>();
        Face f_23 = createFace(expectedNodes.get(2), expectedNodes.get(3),
                expectedCells.get(0), expectedCells.get(1));
        expectedInternalFaces.add(f_23);

        Face f_24 = createFace(expectedNodes.get(2), expectedNodes.get(4),
                expectedCells.get(1), expectedCells.get(2));
        expectedInternalFaces.add(f_24);

        Face f_34 = createFace(expectedNodes.get(3), expectedNodes.get(4),
                expectedCells.get(5), expectedCells.get(1));
        expectedInternalFaces.add(f_34);

        Face f_45 = createFace(expectedNodes.get(4), expectedNodes.get(5),
                expectedCells.get(5), expectedCells.get(4));
        expectedInternalFaces.add(f_45);

        Face f_48 = createFace(expectedNodes.get(4), expectedNodes.get(8),
                expectedCells.get(4), expectedCells.get(3));
        expectedInternalFaces.add(f_48);

        Face f_410 = createFace(expectedNodes.get(4), expectedNodes.get(10),
                expectedCells.get(3), expectedCells.get(2));
        expectedInternalFaces.add(f_410);

        Face f_710 = createFace(expectedNodes.get(7), expectedNodes.get(10),
                expectedCells.get(2), expectedCells.get(3));
        expectedInternalFaces.add(f_710);

        Node n0, n1, n2, n3, n4, n5, n6, n7, n8, n9, n10;
        double volume;
        Point centroid;
        List<Face> blueBoundaryFaces = new ArrayList<>();
        n0 = expectedNodes.get(0);
        n1 = new Node(117.27, 395.424, 0.0);
        n2 = new Node(74.5388, 449.511, 0.0);
        n3 = expectedNodes.get(3);
        volume = 12836.432349999997;
        centroid = new Point(165.41106041983, 461.740326228124, 0.0);
        Cell ghostCell_03 = new Cell(-1, new Node[]{n0, n1, n2, n3},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_03 = createFace(n3, n0, expectedCells.get(0), ghostCell_03);
        blueBoundaryFaces.add(f_03);

        n3 = expectedNodes.get(3);
        n4 = new Node(146.746, 739.978, 0.0);
        n5 = expectedNodes.get(5);
        volume = 17265.249800000005;
        centroid = new Point(236.6352558568723, 674.6258537509463, 0.0);
        Cell ghostCell_35 = new Cell(-1, new Node[]{n3, n4, n5},
                VTKType.VTK_TRIANGLE, new Shape(volume, centroid), numVars);
        Face f_35 = createFace(n5, n3, expectedCells.get(5), ghostCell_35);
        blueBoundaryFaces.add(f_35);

        n4 = new Node(409.07, 903.081, 0.0);
        n8 = new Node(505.171, 830.598, 0.0);
        n9 = expectedNodes.get(9);
        n5 = expectedNodes.get(5);
        volume = 17063.591850000008;
        centroid = new Point(424.01573307823, 802.94285443000, 0.0);
        Cell ghostCell_59 = new Cell(-1, new Node[]{n4, n8, n9, n5},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_59 = createFace(n9, n5, expectedCells.get(4), ghostCell_59);
        blueBoundaryFaces.add(f_59);

        List<Face> greenBoundaryFaces = new ArrayList<>();
        n0 = expectedNodes.get(0);
        n1 = expectedNodes.get(1);
        n2 = new Node(322.429, 314.955, 0.0);
        n3 = new Node(137.839, 336.063, 0.0);
        volume = 12836.432349999997;
        centroid = new Point(244.813042114931, 363.771023553490, 0.0);
        Cell ghostCell_01 = new Cell(-1, new Node[]{n0, n1, n2, n3},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_01 = createFace(n0, n1, expectedCells.get(0), ghostCell_01);
        greenBoundaryFaces.add(f_01);

        n0 = new Node(368.578, 282.909, 0.0);
        n1 = expectedNodes.get(1);
        n2 = expectedNodes.get(2);
        n3 = new Node(492.9, 291.135, 0.0);
        volume = 12836.432349999997;
        centroid = new Point(395.9370872904792, 344.1387053406701, 0.0);
        Cell ghostCell_12 = new Cell(-1, new Node[]{n0, n1, n2, n3},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_12 = createFace(n1, n2, expectedCells.get(0), ghostCell_12);
        greenBoundaryFaces.add(f_12);

        n2 = expectedNodes.get(2);
        n6 = expectedNodes.get(6);
        n7 = new Node(433.34, 595.361, 0.0);
        n10 = new Node(531.849, 498.25, 0.0);
        n4 = new Node(527.82, 426.804, 0.0);
        volume = 11158.635499999997;
        centroid = new Point(473.330094313819, 482.678303375872, 0.0);
        Cell ghostCell_26 = new Cell(-1, new Node[]{n2, n6, n7, n10, n4},
                VTKType.VTK_POLYGON, new Shape(volume, centroid), numVars);
        Face f_26 = createFace(n2, n6, expectedCells.get(2), ghostCell_26);
        greenBoundaryFaces.add(f_26);

        n2 = new Node(540.478, 593.516, 0.0);
        n6 = expectedNodes.get(6);
        n7 = expectedNodes.get(7);
        n10 = new Node(408.028, 432.72, 0.0);
        n4 = new Node(367.067, 491.398, 0.0);
        volume = 11158.635499999997;
        centroid = new Point(444.43192205819, 481.110582128155, 0.0);
        Cell ghostCell_67 = new Cell(-1, new Node[]{n2, n6, n7, n10, n4},
                VTKType.VTK_POLYGON, new Shape(volume, centroid), numVars);
        Face f_67 = createFace(n6, n7, expectedCells.get(2), ghostCell_67);
        greenBoundaryFaces.add(f_67);

        List<Face> redBoundaryFaces = new ArrayList<>();
        n4 = new Node(619.022, 595.878, 0.0);
        n10 = new Node(560.497, 554.699, 0.0);
        n7 = expectedNodes.get(7);
        n8 = expectedNodes.get(8);
        volume = 7710.785800000003;
        centroid = new Point(554.11333761658, 575.413715290550, 0.0);
        Cell ghostCell_78 = new Cell(-1, new Node[]{n4, n10, n7, n8},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_78 = createFace(n7, n8, expectedCells.get(3), ghostCell_78);
        redBoundaryFaces.add(f_78);

        n4 = new Node(637.61, 676.9, 0.0);
        n8 = expectedNodes.get(8);
        n9 = expectedNodes.get(9);
        n5 = new Node(572.558, 851.964, 0.0);
        volume = 17063.591850000008;
        centroid = new Point(556.9722373381545, 738.1250853163604, 0.0);
        Cell ghostCell_89 = new Cell(-1, new Node[]{n4, n8, n9, n5},
                VTKType.VTK_QUAD, new Shape(volume, centroid), numVars);
        Face f_89 = createFace(n8, n9, expectedCells.get(4), ghostCell_89);
        redBoundaryFaces.add(f_89);

        expectedBoundaries = List.of(
                new Boundary("Blue Boundary", blueBoundaryFaces, dummyBC),
                new Boundary("Green Boundary", greenBoundaryFaces, dummyBC),
                new Boundary("Red Boundary", redBoundaryFaces, dummyBC));

        expectedNodes.get(0).neighbors.add(expectedCells.get(0));
        expectedNodes.get(1).neighbors.add(expectedCells.get(0));
        expectedNodes.get(2).neighbors.add(expectedCells.get(0));
        expectedNodes.get(2).neighbors.add(expectedCells.get(1));
        expectedNodes.get(2).neighbors.add(expectedCells.get(2));
        expectedNodes.get(3).neighbors.add(expectedCells.get(0));
        expectedNodes.get(3).neighbors.add(expectedCells.get(1));
        expectedNodes.get(3).neighbors.add(expectedCells.get(5));
        expectedNodes.get(4).neighbors.add(expectedCells.get(1));
        expectedNodes.get(4).neighbors.add(expectedCells.get(2));
        expectedNodes.get(4).neighbors.add(expectedCells.get(3));
        expectedNodes.get(4).neighbors.add(expectedCells.get(4));
        expectedNodes.get(4).neighbors.add(expectedCells.get(5));
        expectedNodes.get(5).neighbors.add(expectedCells.get(4));
        expectedNodes.get(5).neighbors.add(expectedCells.get(5));
        expectedNodes.get(6).neighbors.add(expectedCells.get(2));
        expectedNodes.get(7).neighbors.add(expectedCells.get(2));
        expectedNodes.get(7).neighbors.add(expectedCells.get(3));
        expectedNodes.get(8).neighbors.add(expectedCells.get(3));
        expectedNodes.get(8).neighbors.add(expectedCells.get(4));
        expectedNodes.get(9).neighbors.add(expectedCells.get(4));
        expectedNodes.get(10).neighbors.add(expectedCells.get(2));
        expectedNodes.get(10).neighbors.add(expectedCells.get(3));

        // add ghost cells
        expectedNodes.get(0).neighbors.add(ghostCell_03);
        expectedNodes.get(0).neighbors.add(ghostCell_01);
        expectedNodes.get(1).neighbors.add(ghostCell_01);
        expectedNodes.get(1).neighbors.add(ghostCell_12);
        expectedNodes.get(2).neighbors.add(ghostCell_12);
        expectedNodes.get(2).neighbors.add(ghostCell_26);
        expectedNodes.get(3).neighbors.add(ghostCell_03);
        expectedNodes.get(3).neighbors.add(ghostCell_35);
        expectedNodes.get(5).neighbors.add(ghostCell_35);
        expectedNodes.get(5).neighbors.add(ghostCell_59);
        expectedNodes.get(6).neighbors.add(ghostCell_26);
        expectedNodes.get(6).neighbors.add(ghostCell_67);
        expectedNodes.get(7).neighbors.add(ghostCell_67);
        expectedNodes.get(7).neighbors.add(ghostCell_78);
        expectedNodes.get(8).neighbors.add(ghostCell_78);
        expectedNodes.get(8).neighbors.add(ghostCell_89);
        expectedNodes.get(9).neighbors.add(ghostCell_59);
        expectedNodes.get(9).neighbors.add(ghostCell_89);

        // Add cell faces
        expectedCells.get(0).faces.add(f_01);
        expectedCells.get(0).faces.add(f_12);
        expectedCells.get(0).faces.add(f_23);
        expectedCells.get(0).faces.add(f_03);
        expectedCells.get(1).faces.add(f_24);
        expectedCells.get(1).faces.add(f_34);
        expectedCells.get(1).faces.add(f_23);
        expectedCells.get(2).faces.add(f_24);
        expectedCells.get(2).faces.add(f_26);
        expectedCells.get(2).faces.add(f_67);
        expectedCells.get(2).faces.add(f_710);
        expectedCells.get(2).faces.add(f_410);
        expectedCells.get(3).faces.add(f_410);
        expectedCells.get(3).faces.add(f_710);
        expectedCells.get(3).faces.add(f_78);
        expectedCells.get(3).faces.add(f_48);
        expectedCells.get(4).faces.add(f_48);
        expectedCells.get(4).faces.add(f_89);
        expectedCells.get(4).faces.add(f_59);
        expectedCells.get(4).faces.add(f_45);
        expectedCells.get(5).faces.add(f_45);
        expectedCells.get(5).faces.add(f_35);
        expectedCells.get(5).faces.add(f_34);
    }

    private static Face createFace(Node n0, Node n1, Cell left, Cell right) {
        Geometry faceGeom = new Line(n0.location(), n1.location());
        Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), lineNormal(n0, n1));

        return new Face(new Node[]{n0, n1}, faceGeom.vtkType(), surface, left, right, numVars);
    }

    private static Vector lineNormal(Node n0, Node n1) {
        Vector cellNormal = new Vector(0, 0, 1);
        Vector lineTangent = new Vector(n0.location(), n1.location()).unit();
        return lineTangent.cross(cellNormal).unit();
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
        return n1.location().distance(n2.location()) < 1e-8;
    }

    private static boolean equivalentShapes(Shape s1, Shape s2) {
        return Math.abs(s1.volume - s2.volume) < 1e-8 &&
                s1.centroid.distance(s2.centroid) < 1e-8;
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
            if (!found) {
                return false;
            }
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
        if (Math.abs(s1.area - s2.area) > 1e-8) return false;
        if (s1.centroid.distance(s2.centroid) > 1e-8) return false;
        return s1.unitNormal
                .sub(s2.unitNormal.mult(direction))
                .mag() < 1e-8;
    }

    private static void assertPointEquals(Point expectedPoint, Point actualPoint) {
        if (expectedPoint.distance(actualPoint) > 1e-8) {
            System.out.println(expectedPoint + ", " + actualPoint);
        }
        assertEquals(0, expectedPoint.distance(actualPoint), 1e-8);
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
        assertEquals(expectedShape.volume, actualShape.volume, 1e-8);

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

    private Boundary searchBoundary(Boundary query, List<Boundary> BoundaryList) {
        for (Boundary elem : BoundaryList) {
            if (query.name.equals(elem.name))
                return elem;
        }
        return null;
    }

    private static void assertBoundaryEquals(Boundary bnd1, Boundary bnd2) {
        // Has same name
        assertEquals(bnd1.name, bnd2.name);

        // Has same faces
        assertTrue(hasEquivalentFaces(bnd1.faces, bnd2.faces));

        // Has same bc
        assertEquals(bnd1.bc().orElseThrow(), bnd2.bc().orElseThrow());
    }

    @Test
    public void cells() {
        try {
            Unstructured2DMesh mesh = new Unstructured2DMesh(new File("test/test_data/mesh_unstructured_2d.cfdu"), numVars,
                    Map.of("Blue Boundary", dummyBC,
                            "Green Boundary", dummyBC,
                            "Red Boundary", dummyBC));
            List<Cell> actualCells = mesh.cells();
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
            Unstructured2DMesh mesh = new Unstructured2DMesh(new File("test/test_data/mesh_unstructured_2d.cfdu"), numVars,
                    Map.of("Blue Boundary", dummyBC,
                            "Green Boundary", dummyBC,
                            "Red Boundary", dummyBC));
            assertEquals(expectedInternalFaces.size(), mesh.internalFaces().size());
            assertTrue(hasEquivalentFaces(expectedInternalFaces, mesh.internalFaces()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void nodes() {
        try {
            Unstructured2DMesh mesh = new Unstructured2DMesh(new File("test/test_data/mesh_unstructured_2d.cfdu"), numVars,
                    Map.of("Blue Boundary", dummyBC,
                            "Green Boundary", dummyBC,
                            "Red Boundary", dummyBC));


            assertEquals(expectedNodes.size(), mesh.nodes().size());
            for (int i = 0; i < expectedNodes.size(); i++) {
                assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void boundaries() {
        try {
            Unstructured2DMesh mesh = new Unstructured2DMesh(new File("test/test_data/mesh_unstructured_2d.cfdu"), numVars,
                    Map.of("Blue Boundary", dummyBC,
                            "Green Boundary", dummyBC,
                            "Red Boundary", dummyBC));

            List<Boundary> actualBoundaries = mesh.boundaries();

            assertEquals(expectedBoundaries.size(), actualBoundaries.size());

            for (Boundary expectedBoundary : expectedBoundaries) {
                Boundary actualBoundary = searchBoundary(expectedBoundary, actualBoundaries);
                if (actualBoundary == null) {
                    fail("Unable to locate the expected boundary: " + expectedBoundary.name);
                }
                assertBoundaryEquals(expectedBoundary, actualBoundary);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void indexTest() throws FileNotFoundException {
        Unstructured2DMesh actualMesh = new Unstructured2DMesh(new File("test/test_data/mesh_unstructured_2d.cfdu"), numVars,
                Map.of("Blue Boundary", dummyBC,
                        "Green Boundary", dummyBC,
                        "Red Boundary", dummyBC));
        List<Cell> cells = actualMesh.cells();
        assertTrue(IntStream.range(0, cells.size())
                .allMatch(i -> cells.get(i).index == i));
    }

    @Test
    public void exceptionTest() {
        File doesNotExist = new File("test/test_data/doesNotExist.cfdu");
        TestHelper.assertThrows(FileNotFoundException.class,
                () -> new Unstructured2DMesh(doesNotExist, numVars, Map.of()));
    }
}