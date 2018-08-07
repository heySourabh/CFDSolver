package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.util.DoubleArray;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.junit.Assert.*;

public class LeastSquareCellGradientTest {

    @Test
    public void zero_gradients() throws FileNotFoundException {
        GoverningEquations govEqn = new EulerEquations(1.4, 387);
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());

        // Setup variable values
        mesh.cellStream()
                .forEach(cell -> DoubleArray.copy(new double[]{45, 78, 25, 134, -24}, cell.U));

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
    public void constant_gradients_faces() throws FileNotFoundException {
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
    public void constant_gradients_nodes() throws FileNotFoundException {
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

    private void assertVectorEquals(Vector expected, Vector actual, double tolerance) {
        assertArrayEquals(new double[]{expected.x, expected.y, expected.z},
                new double[]{actual.x, actual.y, actual.z}, tolerance);
    }
}
