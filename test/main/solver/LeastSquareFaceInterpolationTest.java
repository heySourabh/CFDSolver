package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.geom.factory.Polygon;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.mesh.Node;
import main.mesh.factory.Structured2DMesh;
import main.mesh.factory.Structured3DMesh;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static main.util.DoubleArray.copy;
import static main.util.TestHelper.assertVectorEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LeastSquareFaceInterpolationTest {

    private Mesh createMesh_3x3_cells(int numVars) throws IOException {
        File tempFile = new File("test/test_data/tempFile.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n" +
                    "-1.000     9.000      0.000     \n" +
                    "-1.000     4.333      0.000     \n" +
                    "-1.000     -0.333     0.000     \n" +
                    "-1.000     -5.000     0.000     \n" +
                    "1.000      9.000      0.000     \n" +
                    "1.000      4.333      0.000     \n" +
                    "1.000      -0.333     0.000     \n" +
                    "1.000      -5.000     0.000     \n" +
                    "3.000      9.000      0.000     \n" +
                    "3.000      4.333      0.000     \n" +
                    "3.000      -0.333     0.000     \n" +
                    "3.000      -5.000     0.000     \n" +
                    "5.000      9.000      0.000     \n" +
                    "5.000      4.333      0.000     \n" +
                    "5.000      -0.333     0.000     \n" +
                    "5.000      -5.000     0.000     \n");
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
            writer.write("" +
                    "dimension = 3\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n" +
                    "zeta = 4\n" +
                    "-1.000     9.000      3.000     \n" +
                    "-1.000     9.000      4.333     \n" +
                    "-1.000     9.000      5.667     \n" +
                    "-1.000     9.000      7.000     \n" +
                    "-1.000     4.333      3.000     \n" +
                    "-1.000     4.333      4.333     \n" +
                    "-1.000     4.333      5.667     \n" +
                    "-1.000     4.333      7.000     \n" +
                    "-1.000     -0.333     3.000     \n" +
                    "-1.000     -0.333     4.333     \n" +
                    "-1.000     -0.333     5.667     \n" +
                    "-1.000     -0.333     7.000     \n" +
                    "-1.000     -5.000     3.000     \n" +
                    "-1.000     -5.000     4.333     \n" +
                    "-1.000     -5.000     5.667     \n" +
                    "-1.000     -5.000     7.000     \n" +
                    "1.000      9.000      3.000     \n" +
                    "1.000      9.000      4.333     \n" +
                    "1.000      9.000      5.667     \n" +
                    "1.000      9.000      7.000     \n" +
                    "1.000      4.333      3.000     \n" +
                    "1.000      4.333      4.333     \n" +
                    "1.000      4.333      5.667     \n" +
                    "1.000      4.333      7.000     \n" +
                    "1.000      -0.333     3.000     \n" +
                    "1.000      -0.333     4.333     \n" +
                    "1.000      -0.333     5.667     \n" +
                    "1.000      -0.333     7.000     \n" +
                    "1.000      -5.000     3.000     \n" +
                    "1.000      -5.000     4.333     \n" +
                    "1.000      -5.000     5.667     \n" +
                    "1.000      -5.000     7.000     \n" +
                    "3.000      9.000      3.000     \n" +
                    "3.000      9.000      4.333     \n" +
                    "3.000      9.000      5.667     \n" +
                    "3.000      9.000      7.000     \n" +
                    "3.000      4.333      3.000     \n" +
                    "3.000      4.333      4.333     \n" +
                    "3.000      4.333      5.667     \n" +
                    "3.000      4.333      7.000     \n" +
                    "3.000      -0.333     3.000     \n" +
                    "3.000      -0.333     4.333     \n" +
                    "3.000      -0.333     5.667     \n" +
                    "3.000      -0.333     7.000     \n" +
                    "3.000      -5.000     3.000     \n" +
                    "3.000      -5.000     4.333     \n" +
                    "3.000      -5.000     5.667     \n" +
                    "3.000      -5.000     7.000     \n" +
                    "5.000      9.000      3.000     \n" +
                    "5.000      9.000      4.333     \n" +
                    "5.000      9.000      5.667     \n" +
                    "5.000      9.000      7.000     \n" +
                    "5.000      4.333      3.000     \n" +
                    "5.000      4.333      4.333     \n" +
                    "5.000      4.333      5.667     \n" +
                    "5.000      4.333      7.000     \n" +
                    "5.000      -0.333     3.000     \n" +
                    "5.000      -0.333     4.333     \n" +
                    "5.000      -0.333     5.667     \n" +
                    "5.000      -0.333     7.000     \n" +
                    "5.000      -5.000     3.000     \n" +
                    "5.000      -5.000     4.333     \n" +
                    "5.000      -5.000     5.667     \n" +
                    "5.000      -5.000     7.000     \n");
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
                assertVectorEquals(new Vector(0, 0, 0), face.gradient[var], 1e-12);
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
                assertVectorEquals(expectedPlanarGradient, face.gradient[var], 1e-12);
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
                assertVectorEquals(gradients[var], face.gradient[var], 1e-12);
            }
        }
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
}
