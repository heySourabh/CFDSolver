package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.mesh.factory.Structured1DMesh;
import main.mesh.factory.Structured3DMesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.util.DoubleArray;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static main.util.DoubleArray.copy;
import static main.util.TestHelper.assertVectorEquals;
import static org.junit.Assert.assertEquals;

public class LeastSquareCellGradientTest {

    @Test
    public void zero_gradients() throws FileNotFoundException {
        GoverningEquations govEqn = new EulerEquations(1.4, 387);
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());

        // Setup variable values
        mesh.cellStream()
                .forEach(cell -> copy(new double[]{45, 78, 25, 134, -24}, cell.U));

        CellGradientCalculator gradientCalc = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = gradientCalc.forCell(mesh.cells().get(1));

        Vector[] expectedGradients = new Vector[]{
                new Vector(0, 0, 0), new Vector(0, 0, 0), new Vector(0, 0, 0),
                new Vector(0, 0, 0), new Vector(0, 0, 0)
        };

        for (int i = 0; i < expectedGradients.length; i++) {
            assertEquals(0, expectedGradients[i].sub(actualGradients[i]).mag(), 1e-12);
        }
    }

    @Test
    public void constant_gradients_faceNeigh() throws FileNotFoundException {
        GoverningEquations govEqn = new EulerEquations(1.4, 387);
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());

        Vector[] expectedGradients = new Vector[]{
                new Vector(0.0, -8.0, 0.0), new Vector(-4.0, -4.0, 0.0),
                new Vector(7.0, -3.0, 0.0), new Vector(9.0, -5.0, 0.0),
                new Vector(-4.0, 0.0, 0.0)
        };

        Cell c0 = mesh.cells().get((1));
        double[] U0 = {-25, 75, 38, -416, 546};
        mesh.cellStream().forEach(cell -> {
            Vector distVector = new Vector(c0.shape.centroid, cell.shape.centroid);
            for (int var = 0; var < U0.length; var++) {
                cell.U[var] = U0[var] + expectedGradients[var].dot(distVector);
            }
        });

        CellGradientCalculator gradientCalc = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = gradientCalc.forCell(c0);

        assertEquals(expectedGradients.length, actualGradients.length);
        for (int i = 0; i < expectedGradients.length; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void constant_gradients_nodeNeigh() throws FileNotFoundException {
        GoverningEquations govEqn = new EulerEquations(1.4, 387);
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());

        Vector[] expectedGradients = new Vector[]{
                new Vector(0.0, -8.0, 0.0), new Vector(-4.0, -4.0, 0.0),
                new Vector(7.0, -3.0, 0.0), new Vector(9.0, -5.0, 0.0),
                new Vector(-4.0, 0.0, 0.0)
        };

        Cell c0 = mesh.cells().get((1));
        double[] U0 = {-25, 75, 38, -416, 546};
        mesh.nodeStream().flatMap(node -> node.neighbors.stream()).distinct().forEach(cell -> {
            Vector distVector = new Vector(c0.shape.centroid, cell.shape.centroid);
            for (int var = 0; var < U0.length; var++) {
                cell.U[var] = U0[var] + expectedGradients[var].dot(distVector);
            }
        });

        CellGradientCalculator gradientCalc = new LeastSquareCellGradient(mesh, new NodeNeighbors());
        Vector[] actualGradients = gradientCalc.forCell(c0);

        assertEquals(expectedGradients.length, actualGradients.length);
        for (int i = 0; i < expectedGradients.length; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void weighted_gradients_faceNeigh() throws FileNotFoundException {
        GoverningEquations govEqn = new EulerEquations(1.4, 387);
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());

        Cell c0 = mesh.cells().get(1);
        Cell n1 = mesh.cells().get(0);
        Cell n2 = mesh.cells().get(2);
        Cell n3 = mesh.cells().get(5);

        copy(new double[]{-40.0, -8.0, -28.0, 16.0, 36.0}, c0.U);
        copy(new double[]{-7.0, 26.0, -38.0, 28.0, 40.0}, n1.U);
        copy(new double[]{-18.0, -4.0, 25.0, 33.0, -5.0}, n2.U);
        copy(new double[]{18.0, 45.0, 33.0, -19.0, 49.0}, n3.U);

        CellGradientCalculator gradientCalc = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = gradientCalc.forCell(c0);

        // Calculated using Maxima
        Vector[] expectedGradients = {
                new Vector(-0.1272357437913819, 0.198453997165226, 0.0),
                new Vector(-0.2304578949454582, 0.19116968892656, 0.0),
                new Vector(0.2491348971346862, 0.3644434131934601, 0.0),
                new Vector(0.1131395270014642, -0.3194137091784741, 0.0),
                new Vector(-0.288782478312662, 0.1389860811709867, 0.0)
        };

        for (int var = 0; var < govEqn.numVars(); var++) {
            assertVectorEquals(expectedGradients[var], actualGradients[var], 1e-12);
        }
    }

    @Test
    public void one_dim_cells_x_direction() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "-2.0      0        0\n" +
                    "-0.75     0        0\n" +
                    "0.5       0        0\n" +
                    "1.2       0        0\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary file: " + tempFile);
        }

        Cell cell = mesh.cells().get(1);
        Cell neigh1 = mesh.cells().get(0);
        Cell neigh2 = mesh.cells().get(2);

        copy(new double[]{-5, 3, 10}, neigh1.U);
        copy(new double[]{2, 5, 8}, cell.U);
        copy(new double[]{2, 3, -2}, neigh2.U);

        LeastSquareCellGradient cellGradient = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = cellGradient.forCell(cell);

        Point p1 = neigh1.shape.centroid;
        Point p2 = cell.shape.centroid;
        Point p3 = neigh2.shape.centroid;
        Vector[] expectedGradients = {
                oneDimGradient(-5, 2, 2, p1, p2, p3),
                oneDimGradient(3, 5, 3, p1, p2, p3),
                oneDimGradient(10, 8, -2, p1, p2, p3)
        };

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void one_dim_cells_y_direction() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "0        -2.0      0\n" +
                    "0        -0.75     0\n" +
                    "0        0.5       0\n" +
                    "0        1.2       0\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary file: " + tempFile);
        }

        Cell cell = mesh.cells().get(1);
        Cell neigh1 = mesh.cells().get(0);
        Cell neigh2 = mesh.cells().get(2);

        copy(new double[]{-5, 3, 10}, neigh1.U);
        copy(new double[]{2, 5, 8}, cell.U);
        copy(new double[]{2, 3, -2}, neigh2.U);

        LeastSquareCellGradient cellGradient = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = cellGradient.forCell(cell);

        Point p1 = neigh1.shape.centroid;
        Point p2 = cell.shape.centroid;
        Point p3 = neigh2.shape.centroid;
        Vector[] expectedGradients = {
                oneDimGradient(-5, 2, 2, p1, p2, p3),
                oneDimGradient(3, 5, 3, p1, p2, p3),
                oneDimGradient(10, 8, -2, p1, p2, p3)
        };

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void one_dim_cells_z_direction() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "0       0      -2.0\n" +
                    "0       0      -0.75\n" +
                    "0       0      0.5\n" +
                    "0       0      1.2\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary file: " + tempFile);
        }

        Cell cell = mesh.cells().get(1);
        Cell neigh1 = mesh.cells().get(0);
        Cell neigh2 = mesh.cells().get(2);

        copy(new double[]{-5, 3, 10}, neigh1.U);
        copy(new double[]{2, 5, 8}, cell.U);
        copy(new double[]{2, 3, -2}, neigh2.U);

        LeastSquareCellGradient cellGradient = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = cellGradient.forCell(cell);

        Point p1 = neigh1.shape.centroid;
        Point p2 = cell.shape.centroid;
        Point p3 = neigh2.shape.centroid;
        Vector[] expectedGradients = {
                oneDimGradient(-5, 2, 2, p1, p2, p3),
                oneDimGradient(3, 5, 3, p1, p2, p3),
                oneDimGradient(10, 8, -2, p1, p2, p3)
        };

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void one_dim_cells_xy_direction() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "2       -1          0\n" +
                    "3        0          0\n" +
                    "4        1          0\n" +
                    "5        2          0\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary file: " + tempFile);
        }

        Cell cell = mesh.cells().get(1);
        Cell neigh1 = mesh.cells().get(0);
        Cell neigh2 = mesh.cells().get(2);

        copy(new double[]{-5, 3, 10}, neigh1.U);
        copy(new double[]{2, 5, 8}, cell.U);
        copy(new double[]{2, 3, -2}, neigh2.U);

        LeastSquareCellGradient cellGradient = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = cellGradient.forCell(cell);

        Point p1 = neigh1.shape.centroid;
        Point p2 = cell.shape.centroid;
        Point p3 = neigh2.shape.centroid;
        Vector[] expectedGradients = {
                oneDimGradient(-5, 2, 2, p1, p2, p3),
                oneDimGradient(3, 5, 3, p1, p2, p3),
                oneDimGradient(10, 8, -2, p1, p2, p3)
        };

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void one_dim_cells_xyz_direction() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "2       -1          3\n" +
                    "3        0          2\n" +
                    "4        1          1\n" +
                    "5        2          0\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete()) {
            System.out.println("Unable to delete temporary file: " + tempFile);
        }

        Cell cell = mesh.cells().get(1);
        Cell neigh1 = mesh.cells().get(0);
        Cell neigh2 = mesh.cells().get(2);

        copy(new double[]{-5, 3, 10}, neigh1.U);
        copy(new double[]{2, 5, 8}, cell.U);
        copy(new double[]{2, 3, -2}, neigh2.U);

        LeastSquareCellGradient cellGradient = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = cellGradient.forCell(cell);

        Point p1 = neigh1.shape.centroid;
        Point p2 = cell.shape.centroid;
        Point p3 = neigh2.shape.centroid;
        Vector[] expectedGradients = {
                oneDimGradient(-5, 2, 2, p1, p2, p3),
                oneDimGradient(3, 5, 3, p1, p2, p3),
                oneDimGradient(10, 8, -2, p1, p2, p3)
        };

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    @Test
    public void threeDim_structured_mesh() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 3\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n" +
                    "zeta = 4\n" +
                    "2.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "2.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "2.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "2.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "2.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "2.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "2.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "3.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "3.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "3.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "3.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "4.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "4.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "4.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "4.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "5.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "5.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "5.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "5.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    6.000000000000000   \n");
        }

        int numVars = 2;
        Structured3DMesh mesh = new Structured3DMesh(tempFile, numVars,
                null, null, null, null, null, null);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Cell cell = mesh.cells().get(13);

        Vector[] expectedGradients = {
                new Vector(-1.50, -0.86, 1.92),
                new Vector(9.80, -3.61, -5.48)
        };

        double[] U0 = {4.31, -7.18};
        FaceNeighbors neighborsCalc = new FaceNeighbors();
        List<Cell> neighbors = neighborsCalc.calculateFor(cell);
        for (Cell neigh : neighbors) {
            Vector dr = new Vector(cell.shape.centroid, neigh.shape.centroid);
            double[] U = new double[expectedGradients.length];
            for (int i = 0; i < expectedGradients.length; i++) {
                U[i] = U0[i] + expectedGradients[i].dot(dr);
            }
            DoubleArray.copy(U, neigh.U);
        }

        CellGradientCalculator gradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalc);
        Vector[] actualGradients = gradientCalculator.forCell(cell);

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    private Vector oneDimGradient(double value1, double value2, double value3, Point p1, Point p2, Point p3) {
        double du1 = value1 - value2;
        double du2 = value3 - value2;

        double r1 = new Vector(p2, p1).mag();
        double r2 = new Vector(p2, p3).mag();

        double du_dr = 0.5 * (du2 / r2 - du1 / r1);

        Vector n = new Vector(p1, p3).unit();

        return n.mult(du_dr);
    }
}
