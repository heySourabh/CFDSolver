package main.mesh.factory;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Hexahedron;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static main.util.TestHelper.*;
import static org.junit.Assert.assertEquals;

public class Structured3DMeshTest {

    private static BoundaryCondition dummyBC = new BoundaryCondition() {
        @Override
        public void setGhostCellValues(Face face) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public double[] convectiveFlux(Face face) {
            throw new UnsupportedOperationException("Not implemented");
        }
    };

    private static int numVars = 3;
    private static List<Node> expectedNodes;
    private static List<Cell> expectedCells;
    private static List<Face> expectedInternalFaces;
    private static List<Boundary> expectedBoundaries;

    @BeforeClass
    public static void setup() {
        int numXCells = 4;
        int numYCells = 3;
        int numZCells = 2;

        double minX = -2;
        double maxX = 0.5;
        double minY = 5;
        double maxY = 6;
        double minZ = 0;
        double maxZ = 3;

        double dx = (maxX - minX) / numXCells;
        double dy = (maxY - minY) / numYCells;
        double dz = (maxZ - minZ) / numZCells;

        Node[][][] nodeArray = new Node[numXCells + 1 + 2][numYCells + 1 + 2][numZCells + 1 + 2];
        for (int i = -1; i < numXCells + 2; i++) {
            double x = minX + i * dx;
            for (int j = -1; j < numYCells + 2; j++) {
                double y = minY + j * dy;
                for (int k = -1; k < numZCells + 2; k++) {
                    double z = minZ + k * dz;
                    nodeArray[i + 1][j + 1][k + 1] = new Node(x, y, z, numVars);
                }
            }
        }

        expectedNodes = new ArrayList<>();
        for (int i = 0; i < numXCells + 1; i++) {
            for (int j = 0; j < numYCells + 1; j++) {
                for (int k = 0; k < numZCells + 1; k++) {
                    expectedNodes.add(nodeArray[i + 1][j + 1][k + 1]);
                }
            }
        }

        Cell[][][] cellArray = new Cell[numXCells + 2][numYCells + 2][numZCells + 2];
        int index = 0;
        for (int i = 0; i < numXCells + 2; i++) {
            for (int j = 0; j < numYCells + 2; j++) {
                for (int k = 0; k < numZCells + 2; k++) {
                    Node n0 = nodeArray[i][j][k];
                    Node n1 = nodeArray[i + 1][j][k];
                    Node n2 = nodeArray[i + 1][j + 1][k];
                    Node n3 = nodeArray[i][j + 1][k];
                    Node n4 = nodeArray[i][j][k + 1];
                    Node n5 = nodeArray[i + 1][j][k + 1];
                    Node n6 = nodeArray[i + 1][j + 1][k + 1];
                    Node n7 = nodeArray[i][j + 1][k + 1];

                    Point p0 = n0.location();
                    Point p1 = n1.location();
                    Point p2 = n2.location();
                    Point p3 = n3.location();
                    Point p4 = n4.location();
                    Point p5 = n5.location();
                    Point p6 = n6.location();
                    Point p7 = n7.location();

                    Hexahedron hexahedron = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);
                    Shape shape = new Shape(hexahedron.volume(), hexahedron.centroid());
                    int cellID = -1;
                    if (i > 0 && i < numXCells + 1 && j > 0 && j < numYCells + 1 && k > 0 && k < numZCells + 1) {
                        cellID = index;
                    }
                    Cell cell = new Cell(new Node[]{n0, n1, n2, n3, n4, n5, n6, n7},
                            hexahedron.vtkType(), shape, numVars);
                    cell.setIndex(cellID);
                    cellArray[i][j][k] = cell;
                    if (i > 0 && i < numXCells + 1 && j > 0 && j < numYCells + 1 && k > 0 && k < numZCells + 1) {
                        index++;
                    }
                }
            }
        }

        expectedCells = new ArrayList<>();
        for (int i = 0; i < numXCells; i++) {
            for (int j = 0; j < numYCells; j++) {
                for (int k = 0; k < numZCells; k++) {
                    expectedCells.add(cellArray[i + 1][j + 1][k + 1]);
                }
            }
        }

        for (int i = 0; i < cellArray.length; i++) {
            for (int j = 0; j < cellArray[0].length; j++) {
                for (int k = 0; k < cellArray[0][0].length; k++) {
                    int xL = cellArray.length - 1;
                    int yL = cellArray[0].length - 1;
                    int zL = cellArray[0][0].length - 1;
                    // skip 12 mesh edges
                    if ((i == 0 && j == 0) ||
                            (i == 0 && j == yL) ||
                            (i == xL && j == 0) ||
                            (i == xL && j == yL) ||
                            (i == 0 && k == 0) ||
                            (i == 0 && k == zL) ||
                            (i == xL && k == 0) ||
                            (i == xL && k == zL) ||
                            (j == 0 && k == 0) ||
                            (j == 0 && k == zL) ||
                            (j == yL && k == 0) ||
                            (j == yL && k == zL)) {
                        continue;
                    }

                    Cell cell = cellArray[i][j][k];
                    for (Node node : cell.nodes) {
                        node.neighbors.add(cell);
                    }
                }
            }
        }

        Face[][][] xConstFaces = new Face[numXCells + 1][numYCells][numZCells];
        for (int i = 0; i < numXCells + 1; i++) {
            for (int j = 0; j < numYCells; j++) {
                for (int k = 0; k < numZCells; k++) {
                    Cell left = i == 0 ? cellArray[i + 1][j + 1][k + 1] : cellArray[i][j + 1][k + 1];
                    Cell right = i == 0 ? cellArray[i][j + 1][k + 1] : cellArray[i + 1][j + 1][k + 1];
                    Node n0 = nodeArray[i + 1][j + 1][k + 1];
                    Node n1 = nodeArray[i + 1][j + 2][k + 1];
                    Node n2 = nodeArray[i + 1][j + 2][k + 2];
                    Node n3 = nodeArray[i + 1][j + 1][k + 2];

                    Point centroid = n0.location().toVector()
                            .add(new Vector(0, dy / 2.0, dz / 2.0))
                            .toPoint();
                    Surface surface = new Surface(dy * dz, centroid, i == 0 ? new Vector(-1, 0, 0) : new Vector(1, 0, 0));
                    Face face = new Face(new Node[]{n0, n1, n2, n3}, VTKType.VTK_QUAD, surface, left, right, numVars);
                    xConstFaces[i][j][k] = face;
                }
            }
        }

        Face[][][] yConstFaces = new Face[numXCells][numYCells + 1][numZCells];
        for (int i = 0; i < numXCells; i++) {
            for (int j = 0; j < numYCells + 1; j++) {
                for (int k = 0; k < numZCells; k++) {
                    Cell left = j == 0 ? cellArray[i + 1][j + 1][k + 1] : cellArray[i + 1][j][k + 1];
                    Cell right = j == 0 ? cellArray[i + 1][j][k + 1] : cellArray[i + 1][j + 1][k + 1];
                    Node n0 = nodeArray[i + 1][j + 1][k + 1];
                    Node n1 = nodeArray[i + 2][j + 1][k + 1];
                    Node n2 = nodeArray[i + 2][j + 1][k + 2];
                    Node n3 = nodeArray[i + 1][j + 1][k + 2];

                    Point centroid = n0.location().toVector()
                            .add(new Vector(dx / 2.0, 0, dz / 2.0))
                            .toPoint();
                    Surface surface = new Surface(dx * dz, centroid, j == 0 ? new Vector(0, -1, 0) : new Vector(0, 1, 0));
                    Face face = new Face(new Node[]{n0, n1, n2, n3}, VTKType.VTK_QUAD, surface, left, right, numVars);
                    yConstFaces[i][j][k] = face;
                }
            }
        }

        Face[][][] zConstFaces = new Face[numXCells][numYCells][numZCells + 1];
        for (int i = 0; i < numXCells; i++) {
            for (int j = 0; j < numYCells; j++) {
                for (int k = 0; k < numZCells + 1; k++) {
                    Cell left = k == 0 ? cellArray[i + 1][j + 1][k + 1] : cellArray[i + 1][j + 1][k];
                    Cell right = k == 0 ? cellArray[i + 1][j + 1][k] : cellArray[i + 1][j + 1][k + 1];
                    Node n0 = nodeArray[i + 1][j + 1][k + 1];
                    Node n1 = nodeArray[i + 2][j + 1][k + 1];
                    Node n2 = nodeArray[i + 2][j + 2][k + 1];
                    Node n3 = nodeArray[i + 1][j + 2][k + 1];

                    Point centroid = n0.location().toVector()
                            .add(new Vector(dx / 2.0, dy / 2.0, 0))
                            .toPoint();
                    Surface surface = new Surface(dx * dy, centroid, k == 0 ? new Vector(0, 0, -1) : new Vector(0, 0, 1));
                    Face face = new Face(new Node[]{n0, n1, n2, n3}, VTKType.VTK_QUAD, surface, left, right, numVars);
                    zConstFaces[i][j][k] = face;
                }
            }
        }

        for (int i = 1; i < cellArray.length - 1; i++) {
            for (int j = 1; j < cellArray[0].length - 1; j++) {
                for (int k = 1; k < cellArray[0][0].length - 1; k++) {
                    List<Face> cellFaces = cellArray[i][j][k].faces;
                    cellFaces.add(xConstFaces[i - 1][j - 1][k - 1]);
                    cellFaces.add(xConstFaces[i][j - 1][k - 1]);
                    cellFaces.add(yConstFaces[i - 1][j - 1][k - 1]);
                    cellFaces.add(yConstFaces[i - 1][j][k - 1]);
                    cellFaces.add(zConstFaces[i - 1][j - 1][k - 1]);
                    cellFaces.add(zConstFaces[i - 1][j - 1][k]);
                }
            }
        }

        expectedInternalFaces = new ArrayList<>();
        for (int i = 1; i < xConstFaces.length - 1; i++) {
            for (int j = 0; j < xConstFaces[0].length; j++) {
                for (int k = 0; k < xConstFaces[0][0].length; k++) {
                    expectedInternalFaces.add(xConstFaces[i][j][k]);
                }
            }
        }
        for (int i = 0; i < yConstFaces.length; i++) {
            for (int j = 1; j < yConstFaces[0].length - 1; j++) {
                for (int k = 0; k < yConstFaces[0][0].length; k++) {
                    expectedInternalFaces.add(yConstFaces[i][j][k]);
                }
            }
        }
        for (int i = 0; i < zConstFaces.length; i++) {
            for (int j = 0; j < zConstFaces[0].length; j++) {
                for (int k = 1; k < zConstFaces[0][0].length - 1; k++) {
                    expectedInternalFaces.add(zConstFaces[i][j][k]);
                }
            }
        }


        Boundary xiMinBoundary = new Boundary("xi min", new ArrayList<>(), dummyBC);
        Boundary xiMaxBoundary = new Boundary("xi max", new ArrayList<>(), dummyBC);
        Boundary etaMinBoundary = new Boundary("eta min", new ArrayList<>(), dummyBC);
        Boundary etaMaxBoundary = new Boundary("eta max", new ArrayList<>(), dummyBC);
        Boundary zetaMinBoundary = new Boundary("zeta min", new ArrayList<>(), dummyBC);
        Boundary zetaMaxBoundary = new Boundary("zeta max", new ArrayList<>(), dummyBC);
        expectedBoundaries = List.of(xiMinBoundary, xiMaxBoundary, etaMinBoundary,
                etaMaxBoundary, zetaMinBoundary, zetaMaxBoundary);
        // Add boundary faces to ghost cells
        for (int j = 0; j < numYCells; j++) {
            for (int k = 0; k < numZCells; k++) {
                int i = 0;
                Cell ghost = cellArray[i][j + 1][k + 1];
                Face bFace = xConstFaces[i][j][k];
                xiMinBoundary.faces.add(bFace);
                ghost.faces.add(bFace);

                i = numXCells;
                ghost = cellArray[i + 1][j + 1][k + 1];
                bFace = xConstFaces[i][j][k];
                xiMaxBoundary.faces.add(bFace);
                ghost.faces.add(bFace);
            }
        }
        for (int i = 0; i < numXCells; i++) {
            for (int k = 0; k < numZCells; k++) {
                int j = 0;
                Cell ghost = cellArray[i + 1][j][k + 1];
                Face bFace = yConstFaces[i][j][k];
                etaMinBoundary.faces.add(bFace);
                ghost.faces.add(bFace);

                j = numYCells;
                ghost = cellArray[i + 1][j + 1][k + 1];
                bFace = yConstFaces[i][j][k];
                etaMaxBoundary.faces.add(bFace);
                ghost.faces.add(bFace);
            }
        }
        for (int i = 0; i < numXCells; i++) {
            for (int j = 0; j < numYCells; j++) {
                int k = 0;
                Cell ghost = cellArray[i + 1][j + 1][k];
                Face bFace = zConstFaces[i][j][k];
                zetaMinBoundary.faces.add(bFace);
                ghost.faces.add(bFace);

                k = numZCells;
                ghost = cellArray[i + 1][j + 1][k + 1];
                bFace = zConstFaces[i][j][k];
                zetaMaxBoundary.faces.add(bFace);
                ghost.faces.add(bFace);
            }
        }
    }

    @Test
    public void nodes() throws FileNotFoundException {
        File file = new File("test/test_data/mesh_structured_3d.cfds");
        Mesh mesh = new Structured3DMesh(file, numVars, null, null,
                null, null,
                null, null);
        List<Node> actualNodes = mesh.nodes();

        assertEquals(expectedNodes.size(), actualNodes.size());
        for (int i = 0; i < expectedNodes.size(); i++) {
            Node expectedNode = expectedNodes.get(i);
            Node actualNode = actualNodes.get(i);
            assertNodeEquals(expectedNode, actualNode, 1e-12);
        }
    }

    @Test
    public void cells() throws FileNotFoundException {
        File file = new File("test/test_data/mesh_structured_3d.cfds");
        Mesh mesh = new Structured3DMesh(file, numVars, null, null,
                null, null,
                null, null);
        List<Cell> actualCells = mesh.cells();

        assertEquals(expectedCells.size(), actualCells.size());
        for (int i = 0; i < expectedCells.size(); i++) {
            assertCellEquals(expectedCells.get(i), actualCells.get(i), 1e-12);
        }
    }

    @Test
    public void internalFaces() throws FileNotFoundException {
        File file = new File("test/test_data/mesh_structured_3d.cfds");
        Mesh mesh = new Structured3DMesh(file, numVars, null, null,
                null, null,
                null, null);
        List<Face> actualInternalFaces = mesh.internalFaces();

        assertFaceListEquals(expectedInternalFaces, actualInternalFaces, 1e-12);
    }

    @Test
    public void boundaries() throws FileNotFoundException {
        File file = new File("test/test_data/mesh_structured_3d.cfds");
        Mesh mesh = new Structured3DMesh(file, numVars, dummyBC, dummyBC, dummyBC, dummyBC, dummyBC, dummyBC);

        List<Boundary> actualBoundaries = mesh.boundaries();

        assertBoundaryListEquals(expectedBoundaries, actualBoundaries, 1e-12);
    }
}
