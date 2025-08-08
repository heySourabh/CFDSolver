package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.geom.factory.Polygon;
import main.mesh.*;
import main.mesh.factory.Structured2DMesh;
import main.mesh.factory.Structured3DMesh;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static main.util.DoubleArray.copy;
import static main.util.TestHelper.assertVectorEquals;
import static org.junit.jupiter.api.Assertions.*;

public class LeastSquareFaceInterpolationTest {

    private Mesh createMesh_3x3_cells(int numVars) throws IOException {
        File tempFile = new File("test/test_data/tempFile.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("""
                    dimension = 2
                    mode = ASCII
                    xi = 4
                    eta = 4
                    -1.000     9.000      0.000    \s
                    -1.000     4.333      0.000    \s
                    -1.000     -0.333     0.000    \s
                    -1.000     -5.000     0.000    \s
                    1.000      9.000      0.000    \s
                    1.000      4.333      0.000    \s
                    1.000      -0.333     0.000    \s
                    1.000      -5.000     0.000    \s
                    3.000      9.000      0.000    \s
                    3.000      4.333      0.000    \s
                    3.000      -0.333     0.000    \s
                    3.000      -5.000     0.000    \s
                    5.000      9.000      0.000    \s
                    5.000      4.333      0.000    \s
                    5.000      -0.333     0.000    \s
                    5.000      -5.000     0.000    \s
                    """);
        }

        Mesh mesh = new Structured2DMesh(tempFile, numVars, null, null, null, null);

        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary mesh file: " + tempFile);
        }

        return mesh;
    }

    private Mesh createMesh_3x3x3_cells(int numVars) throws IOException {
        File tempFile = new File("test/test_data/tempFile.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("""
                    dimension = 3
                    mode = ASCII
                    xi = 4
                    eta = 4
                    zeta = 4
                    -1.000     9.000      3.000    \s
                    -1.000     9.000      4.333    \s
                    -1.000     9.000      5.667    \s
                    -1.000     9.000      7.000    \s
                    -1.000     4.333      3.000    \s
                    -1.000     4.333      4.333    \s
                    -1.000     4.333      5.667    \s
                    -1.000     4.333      7.000    \s
                    -1.000     -0.333     3.000    \s
                    -1.000     -0.333     4.333    \s
                    -1.000     -0.333     5.667    \s
                    -1.000     -0.333     7.000    \s
                    -1.000     -5.000     3.000    \s
                    -1.000     -5.000     4.333    \s
                    -1.000     -5.000     5.667    \s
                    -1.000     -5.000     7.000    \s
                    1.000      9.000      3.000    \s
                    1.000      9.000      4.333    \s
                    1.000      9.000      5.667    \s
                    1.000      9.000      7.000    \s
                    1.000      4.333      3.000    \s
                    1.000      4.333      4.333    \s
                    1.000      4.333      5.667    \s
                    1.000      4.333      7.000    \s
                    1.000      -0.333     3.000    \s
                    1.000      -0.333     4.333    \s
                    1.000      -0.333     5.667    \s
                    1.000      -0.333     7.000    \s
                    1.000      -5.000     3.000    \s
                    1.000      -5.000     4.333    \s
                    1.000      -5.000     5.667    \s
                    1.000      -5.000     7.000    \s
                    3.000      9.000      3.000    \s
                    3.000      9.000      4.333    \s
                    3.000      9.000      5.667    \s
                    3.000      9.000      7.000    \s
                    3.000      4.333      3.000    \s
                    3.000      4.333      4.333    \s
                    3.000      4.333      5.667    \s
                    3.000      4.333      7.000    \s
                    3.000      -0.333     3.000    \s
                    3.000      -0.333     4.333    \s
                    3.000      -0.333     5.667    \s
                    3.000      -0.333     7.000    \s
                    3.000      -5.000     3.000    \s
                    3.000      -5.000     4.333    \s
                    3.000      -5.000     5.667    \s
                    3.000      -5.000     7.000    \s
                    5.000      9.000      3.000    \s
                    5.000      9.000      4.333    \s
                    5.000      9.000      5.667    \s
                    5.000      9.000      7.000    \s
                    5.000      4.333      3.000    \s
                    5.000      4.333      4.333    \s
                    5.000      4.333      5.667    \s
                    5.000      4.333      7.000    \s
                    5.000      -0.333     3.000    \s
                    5.000      -0.333     4.333    \s
                    5.000      -0.333     5.667    \s
                    5.000      -0.333     7.000    \s
                    5.000      -5.000     3.000    \s
                    5.000      -5.000     4.333    \s
                    5.000      -5.000     5.667    \s
                    5.000      -5.000     7.000    \s
                    """);
        }

        Mesh mesh = new Structured3DMesh(tempFile, numVars,
                null, null, null, null, null, null);

        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary mesh file: " + tempFile);
        }

        return mesh;
    }

    @Test
    public void for_constant_distribution_the_value_at_face_is_constant_and_gradient_is_zero() throws IOException {
        int numVars = 2;
        Mesh mesh = createMesh_3x3_cells(numVars);
        LeastSquareFaceInterpolation ls = new LeastSquareFaceInterpolation(mesh);

        List<Face> centralCellFaces = mesh.cells().get(4).faces;
        assertEquals(4, centralCellFaces.size());

        double[] constU = new double[]{1, -3};
        mesh.cellStream().forEach(cell -> copy(constU, cell.U));

        ls.setupAllFaces();

        for (Face face : centralCellFaces) {
            assertArrayEquals(constU, face.U, 1e-12);
            for (int var = 0; var < numVars; var++) {
                assertVectorEquals(new Vector(0, 0, 0), face.gradientU[var], 1e-12);
            }
        }
    }

    @Test
    public void for_2d_linear_dist_the_value_at_face_is_linearly_interpolated_and_gradient_is_constant() throws IOException {
        int numVars = 2;
        Mesh mesh = createMesh_3x3_cells(numVars);
        LeastSquareFaceInterpolation ls = new LeastSquareFaceInterpolation(mesh);

        Cell cell0 = mesh.cells().get(4);
        List<Face> centralCellFaces = cell0.faces;
        Vector planarNormal = calculatePlanarNormal(cell0);
        assertEquals(4, centralCellFaces.size());

        double[] U0 = new double[]{1, -3};
        Point p0 = cell0.shape.centroid;
        Vector[] gradients = {
                new Vector(-54, -8, 9),
                new Vector(2, 7, -6)
        };
        mesh.cellStream().forEach(cell -> copy(calculateValue(p0, U0, gradients, cell.shape.centroid), cell.U));

        ls.setupAllFaces();

        for (Face face : centralCellFaces) {
            assertArrayEquals(calculateValue(p0, U0, gradients, face.surface.centroid), face.U, 1e-12);
            for (int var = 0; var < numVars; var++) {
                Vector expectedPlanarGradient = removeVectorComponent(gradients[var], planarNormal);
                assertVectorEquals(expectedPlanarGradient, face.gradientU[var], 1e-12);
            }
        }
    }

    @Test
    public void for_3d_linear_dist_the_value_at_face_is_linearly_interpolated_and_gradient_is_constant() throws IOException {
        int numVars = 2;
        Mesh mesh = createMesh_3x3x3_cells(numVars);
        LeastSquareFaceInterpolation ls = new LeastSquareFaceInterpolation(mesh);

        Cell cell0 = mesh.cells().get(13);
        List<Face> centralCellFaces = cell0.faces;
        assertEquals(6, centralCellFaces.size());

        double[] U0 = new double[]{1, -3};
        Point p0 = cell0.shape.centroid;
        Vector[] gradients = {
                new Vector(-54, -8, 9),
                new Vector(2, 7, -6)
        };
        mesh.cellStream().forEach(cell -> copy(calculateValue(p0, U0, gradients, cell.shape.centroid), cell.U));

        ls.setupAllFaces();

        for (Face face : centralCellFaces) {
            assertArrayEquals(calculateValue(p0, U0, gradients, face.surface.centroid), face.U, 1e-12);
            for (int var = 0; var < numVars; var++) {
                assertVectorEquals(gradients[var], face.gradientU[var], 1e-12);
            }
        }
    }

    @Test
    public void face_of_a_boundary_cannot_have_neighbors_as_ghost_cells_belonging_to_different_boundary() {
        int numVars = 2;
        Mesh mesh = create2DMesh(3, 4, numVars);
        Boundary xi_min = mesh.boundaries().get(0);
        Boundary xi_max = mesh.boundaries().get(1);
        Boundary eta_min = mesh.boundaries().get(2);
        Boundary eta_max = mesh.boundaries().get(3);

        // xi min
        LeastSquareFaceInterpolation lsi = new LeastSquareFaceInterpolation(mesh);
        List<Cell> expectedNeighCells = List.of(
                xi_min.faces.get(0).left, xi_min.faces.get(0).right,
                xi_min.faces.get(1).left, xi_min.faces.get(1).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_min.faces.get(0)))));

        expectedNeighCells = List.of(
                xi_min.faces.get(2).left, xi_min.faces.get(2).right,
                xi_min.faces.get(3).left, xi_min.faces.get(3).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_min.faces.get(3)))));


        // xi max
        lsi = new LeastSquareFaceInterpolation(mesh);
        expectedNeighCells = List.of(
                xi_max.faces.get(0).left, xi_max.faces.get(0).right,
                xi_max.faces.get(1).left, xi_max.faces.get(1).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_max.faces.get(0)))));

        expectedNeighCells = List.of(
                xi_max.faces.get(2).left, xi_max.faces.get(2).right,
                xi_max.faces.get(3).left, xi_max.faces.get(3).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_max.faces.get(3)))));

        // eta min
        lsi = new LeastSquareFaceInterpolation(mesh);
        expectedNeighCells = List.of(
                eta_min.faces.get(0).left, eta_min.faces.get(0).right,
                eta_min.faces.get(1).left, eta_min.faces.get(1).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_min.faces.get(0)))));

        expectedNeighCells = List.of(
                eta_min.faces.get(1).left, eta_min.faces.get(1).right,
                eta_min.faces.get(2).left, eta_min.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_min.faces.get(2)))));

        // eta max
        lsi = new LeastSquareFaceInterpolation(mesh);
        expectedNeighCells = List.of(
                eta_max.faces.get(0).left, eta_max.faces.get(0).right,
                eta_max.faces.get(1).left, eta_max.faces.get(1).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_max.faces.get(0)))));

        expectedNeighCells = List.of(
                eta_max.faces.get(1).left, eta_max.faces.get(1).right,
                eta_max.faces.get(2).left, eta_max.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_max.faces.get(2)))));
    }

    @Test
    public void face_of_a_boundary_will_have_neighbors_as_ghost_cells_belonging_to_same_boundary() {
        int numVars = 2;
        Mesh mesh = create2DMesh(3, 4, numVars);
        Boundary xi_min = mesh.boundaries().get(0);
        Boundary xi_max = mesh.boundaries().get(1);
        Boundary eta_min = mesh.boundaries().get(2);
        Boundary eta_max = mesh.boundaries().get(3);

        // xi min
        LeastSquareFaceInterpolation lsi = new LeastSquareFaceInterpolation(mesh);
        List<Cell> expectedNeighCells = List.of(
                xi_min.faces.get(0).left, xi_min.faces.get(0).right,
                xi_min.faces.get(1).left, xi_min.faces.get(1).right,
                xi_min.faces.get(2).left, xi_min.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_min.faces.get(1)))));

        expectedNeighCells = List.of(
                xi_min.faces.get(1).left, xi_min.faces.get(1).right,
                xi_min.faces.get(2).left, xi_min.faces.get(2).right,
                xi_min.faces.get(3).left, xi_min.faces.get(3).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_min.faces.get(2)))));

        // xi max
        expectedNeighCells = List.of(
                xi_max.faces.get(0).left, xi_max.faces.get(0).right,
                xi_max.faces.get(1).left, xi_max.faces.get(1).right,
                xi_max.faces.get(2).left, xi_max.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_max.faces.get(1)))));

        expectedNeighCells = List.of(
                xi_max.faces.get(1).left, xi_max.faces.get(1).right,
                xi_max.faces.get(2).left, xi_max.faces.get(2).right,
                xi_max.faces.get(3).left, xi_max.faces.get(3).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(xi_max.faces.get(2)))));

        // eta min
        expectedNeighCells = List.of(
                eta_min.faces.get(0).left, eta_min.faces.get(0).right,
                eta_min.faces.get(1).left, eta_min.faces.get(1).right,
                eta_min.faces.get(2).left, eta_min.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_min.faces.get(1)))));

        // eta max
        expectedNeighCells = List.of(
                eta_max.faces.get(0).left, eta_max.faces.get(0).right,
                eta_max.faces.get(1).left, eta_max.faces.get(1).right,
                eta_max.faces.get(2).left, eta_max.faces.get(2).right);
        assertTrue(containsSameCells(expectedNeighCells, List.of(lsi.getNeighbours(eta_max.faces.get(1)))));
    }

    @Test
    public void internal_faces_have_all_the_surrounding_neighbors() {
        int numVars = 2;
        Mesh mesh = create2DMesh(3, 4, numVars);

        Boundary xi_min = mesh.boundaries().get(0);
        Boundary xi_max = mesh.boundaries().get(1);
        Boundary eta_min = mesh.boundaries().get(2);
        Boundary eta_max = mesh.boundaries().get(3);

        LeastSquareFaceInterpolation lsi = new LeastSquareFaceInterpolation(mesh);

        Face f0 = mesh.internalFaces().get(0);
        Face f1 = mesh.internalFaces().get(1);
        Face f2 = mesh.internalFaces().get(2);
        Face f3 = mesh.internalFaces().get(3);
        Face f4 = mesh.internalFaces().get(4);
        Face f5 = mesh.internalFaces().get(5);
        Face f6 = mesh.internalFaces().get(6);
        Face f7 = mesh.internalFaces().get(7);
        Face f8 = mesh.internalFaces().get(8);
        Face f9 = mesh.internalFaces().get(9);
        Face f10 = mesh.internalFaces().get(10);
        Face f11 = mesh.internalFaces().get(11);
        Face f12 = mesh.internalFaces().get(12);
        Face f13 = mesh.internalFaces().get(13);
        Face f14 = mesh.internalFaces().get(14);
        Face f15 = mesh.internalFaces().get(15);
        Face f16 = mesh.internalFaces().get(16);

        // f0
        List<Cell> expectedNeighbours = List.of(
                f0.left, f0.right,
                f5.left, f5.right,
                eta_min.faces.get(0).right, eta_min.faces.get(1).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f0))));

        // f1
        expectedNeighbours = List.of(
                f1.left, f1.right,
                f6.left, f6.right,
                eta_min.faces.get(1).right, eta_min.faces.get(2).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f1))));

        // f2
        expectedNeighbours = List.of(
                f2.left, f2.right,
                f3.left, f3.right,
                xi_min.faces.get(0).right, xi_min.faces.get(1).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f2))));

        // f3
        expectedNeighbours = List.of(
                f3.left, f3.right,
                f2.left, f2.right,
                f4.left, f4.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f3))));

        // f4
        expectedNeighbours = List.of(
                f4.left, f4.right,
                f3.left, f3.right,
                xi_max.faces.get(0).right, xi_max.faces.get(1).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f4))));

        // f5
        expectedNeighbours = List.of(
                f5.left, f5.right,
                f0.left, f0.right,
                f10.left, f10.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f5))));

        // f6
        expectedNeighbours = List.of(
                f6.left, f6.right,
                f1.left, f1.right,
                f11.left, f11.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f6))));

        // f7
        expectedNeighbours = List.of(
                f7.left, f7.right,
                f8.left, f8.right,
                xi_min.faces.get(1).right, xi_min.faces.get(2).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f7))));

        // f8
        expectedNeighbours = List.of(
                f8.left, f8.right,
                f7.left, f7.right,
                f9.left, f9.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f8))));

        // f9
        expectedNeighbours = List.of(
                f9.left, f9.right,
                f8.left, f8.right,
                xi_max.faces.get(1).right, xi_max.faces.get(2).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f9))));

        // f10
        expectedNeighbours = List.of(
                f10.left, f10.right,
                f5.left, f5.right,
                f15.left, f15.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f10))));

        // f11
        expectedNeighbours = List.of(
                f11.left, f11.right,
                f6.left, f6.right,
                f16.left, f16.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f11))));

        // f12
        expectedNeighbours = List.of(
                f12.left, f12.right,
                f13.left, f13.right,
                xi_min.faces.get(2).right, xi_min.faces.get(3).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f12))));

        // f13
        expectedNeighbours = List.of(
                f13.left, f13.right,
                f12.left, f12.right,
                f14.left, f14.right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f13))));

        // f14
        expectedNeighbours = List.of(
                f14.left, f14.right,
                f13.left, f13.right,
                xi_max.faces.get(2).right, xi_max.faces.get(3).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f14))));

        // f15
        expectedNeighbours = List.of(
                f15.left, f15.right,
                f10.left, f10.right,
                eta_max.faces.get(0).right, eta_max.faces.get(1).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f15))));

        // f16
        expectedNeighbours = List.of(
                f16.left, f16.right,
                f11.left, f11.right,
                eta_max.faces.get(1).right, eta_max.faces.get(2).right
        );
        assertTrue(containsSameCells(expectedNeighbours, List.of(lsi.getNeighbours(f16))));
    }

    private Vector calculatePlanarNormal(Cell cell) {
        Polygon poly = new Polygon(Arrays.stream(cell.nodes)
                .map(Node::location)
                .toArray(Point[]::new));
        return poly.unitNormal();
    }

    private Vector removeVectorComponent(Vector vector, Vector remove) {
        Vector unitRemove = remove.unit();
        Vector componentToRemove = unitRemove.mult(vector.dot(unitRemove));
        return vector.sub(componentToRemove);
    }

    private double[] calculateValue(Point p0, double[] U0, Vector[] gradients, Point calculateAt) {
        int numVars = U0.length;
        double[] U = new double[numVars];
        Vector dr = new Vector(p0, calculateAt);
        for (int var = 0; var < numVars; var++) {
            U[var] = U0[var] + gradients[var].dot(dr);
        }

        return U;
    }

    private Mesh create2DMesh(int numXCells, int numYCells, int numVars) {
        int numXNodes = numXCells + 1;
        int numYNodes = numYCells + 1;
        double minX = -1.2, minY = 0;
        double Lx = 1.5;
        double Ly = 2.8;
        double maxX = minX + Lx;
        double maxY = minY + Ly;
        File tempMeshFile = new File("test/test_data/tempMesh.cfds");

        try (FileWriter fileWriter = new FileWriter(tempMeshFile);
             PrintWriter writer = new PrintWriter(fileWriter)) {
            writer.write("dimension = 2\n");
            writer.write("mode = ASCII\n");
            writer.printf("xi = %d\n", numXNodes);
            writer.printf("eta = %d\n", numYNodes);
            for (int i = 0; i < numXNodes; i++) {
                double x = minX + i / (numXNodes - 1.0) * (maxX - minX);
                for (int j = 0; j < numYNodes; j++) {
                    double y = minY + j / (numYNodes - 1.0) * (maxY - minY);
                    writer.printf("%-20.15f %-20.15f %-20.15f\n", x, y, 0.0);
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to create mesh.");
        }

        Mesh mesh = null;
        try {
            mesh = new Structured2DMesh(tempMeshFile, numVars, null, null, null, null);
            if (!tempMeshFile.delete()) {
                System.out.println("Unable to delete temporary file: " + tempMeshFile);
            }

            // sort the internal faces
            mesh.internalFaces().sort(this::comparePositionOfFaces);
        } catch (FileNotFoundException e) {
            System.out.println("Mesh file is not found.");
        }

        return mesh;
    }

    private int comparePositionOfFaces(Face f1, Face f2) {
        Vector dist = new Vector(f1.surface.centroid, f2.surface.centroid);
        int dx = compareValue(dist.x);
        int dy = compareValue(dist.y);
        int dz = compareValue(dist.z);

        if (dz == 0) {
            if (dy == 0) {
                return dx;
            } else return dy;
        } else return dz;
    }

    private int compareValue(double v) {
        if (Math.abs(v) < 1e-12) return 0;
        return v > 0 ? -1 : 1;
    }

    private boolean containsSameCells(List<Cell> cellList1, List<Cell> cellList2) {
        if (cellList1.size() != cellList2.size()) return false;

        return cellList1.containsAll(cellList2);
    }
}
