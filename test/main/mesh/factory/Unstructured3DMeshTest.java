package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.factory.*;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import main.util.TestHelper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static main.util.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Unstructured3DMeshTest {
    private static List<Node> expectedNodes;
    private static List<Cell> expectedCells;
    private static List<Face> expectedInternalFaces;
    private static List<Boundary> expectedBoundaries;

    private static List<Set<Cell>> expectedNodeNeighbors;

    private static final int numVars = 3;
    private static final BoundaryCondition dummyBC = new BoundaryCondition() {
        @Override
        public void setGhostCellValues(Face face) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public double[] convectiveFlux(Face face) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    };

    private static void setupMesh() {
        List<Point> points = List.of(
                new Point(1.000000, 1.000000, -1.000000),   // 0
                new Point(-1.000000, 1.000000, -1.000000),   // 1
                new Point(-1.000000, -1.000000, -1.000000),   // 2
                new Point(1.000000, -1.000000, -1.000000),   // 3
                new Point(1.000000, 1.000000, 1.500000),   // 4
                new Point(-1.000000, 1.000000, 1.000000),   // 5
                new Point(-1.000000, -1.000000, 1.000000),   // 6
                new Point(1.000000, -1.000000, 1.500000),   // 7
                new Point(-0.242535606, 0.0, 2.22014260),   // 8
                new Point(-0.252343774, 1.37377357, 2.25937533),   // 9
                new Point(-1.17149854, -1.70710683, 1.68599439),   // 10
                new Point(0.828501403, -1.70710683, 2.18599439),   // 11
                new Point(-0.414034188, -0.707106829, 2.90613699)   // 12
        );

        expectedNodes = points.stream()
                .map(p -> new Node(p, numVars))
                .toList();

        int[][] conn = {
                {0, 1, 2, 3, 4, 5, 6, 7}, // hexahedron
                {4, 5, 6, 7, 8}, // pyramid
                {4, 5, 8, 9}, // tetrahedron
                {7, 6, 8, 11, 10, 12}  // wedge
        };

        Geometry[] cellGeom = {
                new Hexahedron(points.get(conn[0][0]), points.get(conn[0][1]), points.get(conn[0][2]), points.get(conn[0][3]),
                        points.get(conn[0][4]), points.get(conn[0][5]), points.get(conn[0][6]), points.get(conn[0][7])),
                new Pyramid(points.get(conn[1][0]), points.get(conn[1][1]), points.get(conn[1][2]),
                        points.get(conn[1][3]), points.get(conn[1][4])),
                new Tetra(points.get(conn[2][0]), points.get(conn[2][1]), points.get(conn[2][2]), points.get(conn[2][3])),
                new Wedge(points.get(conn[3][0]), points.get(conn[3][1]), points.get(conn[3][2]), points.get(conn[3][3]),
                        points.get(conn[3][4]), points.get(conn[3][5]))
        };

        expectedCells = new ArrayList<>();
        for (int i = 0; i < cellGeom.length; i++) {
            Node[] cellNodes = Arrays.stream(conn[i])
                    .mapToObj(j -> expectedNodes.get(j))
                    .toArray(Node[]::new);
            Geometry geom = cellGeom[i];
            Shape shape = new Shape(geom.volume(), geom.centroid());
            Cell cell = new Cell(cellNodes, geom.vtkType(), shape, numVars);
            cell.setIndex(i);
            expectedCells.add(cell);
        }

        expectedNodeNeighbors = new ArrayList<>();
        for (int i = 0; i < expectedNodes.size(); i++) {
            expectedNodeNeighbors.add(new HashSet<>());
        }

        Face f_01 = createFace(expectedCells.get(0), expectedCells.get(1), 4, 5, 6, 7);
        Face f_12 = createFace(expectedCells.get(1), expectedCells.get(2), 4, 5, 8);
        Face f_13 = createFace(expectedCells.get(1), expectedCells.get(3), 7, 8, 6);
        expectedInternalFaces = List.of(f_01, f_12, f_13);

        // expectedNodeNeighbors also get updated below
        expectedBoundaries = new ArrayList<>();
        // Hexahedron boundary faces
        Cell hexCell = expectedCells.get(0);
        List<Face> hexBndFaces = List.of(
                createBoundaryFace(hexCell, 0, 4, 7, 3),
                createBoundaryFace(hexCell, 0, 1, 5, 4),
                createBoundaryFace(hexCell, 1, 2, 6, 5),
                createBoundaryFace(hexCell, 3, 7, 6, 2),
                createBoundaryFace(hexCell, 3, 2, 1, 0)
        );
        expectedBoundaries.add(new Boundary("Hexahedron Boundary", hexBndFaces, dummyBC));
        hexCell.faces.add(f_01);
        hexCell.faces.addAll(hexBndFaces);

        Cell pyramidCell = expectedCells.get(1);
        List<Face> pyramidBndFaces = List.of(
                createBoundaryFace(pyramidCell, 4, 8, 7),
                createBoundaryFace(pyramidCell, 5, 6, 8)
        );
        expectedBoundaries.add(new Boundary("Pyramid Boundary", pyramidBndFaces, dummyBC));
        pyramidCell.faces.add(f_01);
        pyramidCell.faces.add(f_12);
        pyramidCell.faces.add(f_13);
        pyramidCell.faces.addAll(pyramidBndFaces);

        Cell tetraCell = expectedCells.get(2);
        List<Face> tetraBndFaces = List.of(
                createBoundaryFace(tetraCell, 4, 9, 8),
                createBoundaryFace(tetraCell, 5, 8, 9),
                createBoundaryFace(tetraCell, 4, 5, 9)
        );
        expectedBoundaries.add(new Boundary("Tetrahedron Boundary", tetraBndFaces, dummyBC));
        tetraCell.faces.add(f_12);
        tetraCell.faces.addAll(tetraBndFaces);

        Cell wedgeCell = expectedCells.get(3);
        List<Face> wedgeBndFaces = List.of(
                createBoundaryFace(wedgeCell, 10, 11, 12),
                createBoundaryFace(wedgeCell, 10, 6, 7, 11),
                createBoundaryFace(wedgeCell, 10, 12, 8, 6),
                createBoundaryFace(wedgeCell, 11, 7, 8, 12)
        );
        expectedBoundaries.add(new Boundary("Wedge Boundary", wedgeBndFaces, dummyBC));
        wedgeCell.faces.add(f_13);
        wedgeCell.faces.addAll(wedgeBndFaces);

        for (int i = 0; i < expectedNodes.size(); i++) {
            expectedNodes.get(i).neighbors.addAll(expectedNodeNeighbors.get(i));
        }
    }

    private static Face createFace(Cell leftCell, Cell rightCell, int... faceNodeIndices) {
        Node[] nodes = Arrays.stream(faceNodeIndices)
                .mapToObj(i -> expectedNodes.get(i))
                .toArray(Node[]::new);

        Geometry geometry = switch (faceNodeIndices.length) {
            case 3 -> new Triangle(nodes[0].location(), nodes[1].location(), nodes[2].location());
            case 4 -> new Quad(nodes[0].location(), nodes[1].location(), nodes[2].location(), nodes[3].location());
            default -> throw new UnsupportedOperationException("Unknown face geometry.");
        };
        Surface surface = new Surface(geometry.area(), geometry.centroid(), geometry.unitNormal());
        updateNodeNeighbors(leftCell, rightCell, faceNodeIndices);

        return new Face(nodes, geometry.vtkType(), surface, leftCell, rightCell, numVars);
    }

    private static Face createBoundaryFace(Cell internalCell, int... faceNodeIndices) {
        Face face = createFace(internalCell, null, faceNodeIndices);
        Cell ghostCell = Mesh.ghostCell(internalCell, face);
        ghostCell.faces.add(face);
        face.right = ghostCell;
        updateNodeNeighbors(internalCell, ghostCell, faceNodeIndices);

        return face;
    }

    private static void updateNodeNeighbors(Cell leftCell, Cell rightCell, int... faceNodeIndices) {
        for (int nodeIndex : faceNodeIndices) {
            if (leftCell != null) {
                expectedNodeNeighbors.get(nodeIndex).add(leftCell);
            }
            if (rightCell != null) {
                expectedNodeNeighbors.get(nodeIndex).add(rightCell);
            }
        }
    }

    @Test
    public void cells() throws FileNotFoundException {
        setupMesh();
        File meshFile = new File("test/test_data/unstructured_3d_mesh/mesh.cfdu");
        Unstructured3DMesh mesh = new Unstructured3DMesh(meshFile, numVars, Map.of(
                "Wedge Boundary", dummyBC,
                "Hexahedron Boundary", dummyBC,
                "Tetrahedron Boundary", dummyBC,
                "Pyramid Boundary", dummyBC
        ));

        assertEquals(expectedCells.size(), mesh.cells().size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), mesh.cells().get(i), 1e-6);
        }
    }

    @Test
    public void internalFaces() throws FileNotFoundException {
        setupMesh();
        File meshFile = new File("test/test_data/unstructured_3d_mesh/mesh.cfdu");
        Unstructured3DMesh mesh = new Unstructured3DMesh(meshFile, numVars, Map.of(
                "Wedge Boundary", dummyBC,
                "Hexahedron Boundary", dummyBC,
                "Tetrahedron Boundary", dummyBC,
                "Pyramid Boundary", dummyBC
        ));

        assertFaceListEquals(expectedInternalFaces, mesh.internalFaces(), 1e-6);
    }

    @Test
    public void nodes() throws FileNotFoundException {
        setupMesh();
        File meshFile = new File("test/test_data/unstructured_3d_mesh/mesh.cfdu");
        Unstructured3DMesh mesh = new Unstructured3DMesh(meshFile, numVars, Map.of(
                "Wedge Boundary", dummyBC,
                "Hexahedron Boundary", dummyBC,
                "Tetrahedron Boundary", dummyBC,
                "Pyramid Boundary", dummyBC
        ));

        assertEquals(expectedNodes.size(), mesh.nodes().size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            assertNodeEquals(expectedNodes.get(i), mesh.nodes().get(i), 1e-6);
        }
    }

    @Test
    public void boundaries() throws FileNotFoundException {
        setupMesh();
        File meshFile = new File("test/test_data/unstructured_3d_mesh/mesh.cfdu");
        Unstructured3DMesh mesh = new Unstructured3DMesh(meshFile, numVars, Map.of(
                "Wedge Boundary", dummyBC,
                "Hexahedron Boundary", dummyBC,
                "Tetrahedron Boundary", dummyBC,
                "Pyramid Boundary", dummyBC
        ));

        TestHelper.assertBoundaryListEquals(expectedBoundaries, mesh.boundaries(), 1e-6);
    }

    @Test
    public void indexTest() throws FileNotFoundException {
        setupMesh();
        File meshFile = new File("test/test_data/unstructured_3d_mesh/mesh.cfdu");
        Unstructured3DMesh actualMesh = new Unstructured3DMesh(meshFile, numVars, Map.of(
                "Wedge Boundary", dummyBC,
                "Hexahedron Boundary", dummyBC,
                "Tetrahedron Boundary", dummyBC,
                "Pyramid Boundary", dummyBC
        ));
        List<Cell> cells = actualMesh.cells();
        assertTrue(IntStream.range(0, cells.size())
                .allMatch(i -> cells.get(i).index() == i));

        List<Face> allFaceList = Stream.concat(actualMesh.internalFaceStream(),
                actualMesh.boundaryStream().flatMap(b -> b.faces.stream())).toList();
        assertTrue(IntStream.range(0, allFaceList.size())
                .allMatch(i -> allFaceList.get(i).index() == i));
    }
}