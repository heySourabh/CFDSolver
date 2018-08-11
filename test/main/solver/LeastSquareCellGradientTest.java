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

import static main.TestHelper.assertVectorEquals;
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

        DoubleArray.copy(new double[]{-40.0, -8.0, -28.0, 16.0, 36.0}, c0.U);
        DoubleArray.copy(new double[]{-7.0, 26.0, -38.0, 28.0, 40.0}, n1.U);
        DoubleArray.copy(new double[]{-18.0, -4.0, 25.0, 33.0, -5.0}, n2.U);
        DoubleArray.copy(new double[]{18.0, 45.0, 33.0, -19.0, 49.0}, n3.U);

        CellGradientCalculator gradientCalc = new LeastSquareCellGradient(mesh, new FaceNeighbors());
        Vector[] actualGradients = gradientCalc.forCell(c0);

        // Calculated using Maxima
        Vector[] expectedGradients = {
                new Vector(-0.1108005897734646, 0.1846678859321781, 0.4255757642320288),
                new Vector(-0.2165559817261568, 0.1795085069391851, 0.3599794280078989),
                new Vector(0.2616863785037125, 0.3539149984244665, 0.3250110263672676),
                new Vector(0.1124081898622044, -0.318800250056661, -0.0189374168086574),
                new Vector(-0.2908837194705362, 0.1407486411262102, -0.05441003538045309)
        };

        for (int var = 0; var < govEqn.numVars(); var++) {
            assertVectorEquals(expectedGradients[var], actualGradients[var], 1e-12);
        }
    }
}
