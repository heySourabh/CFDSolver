package main.io;

import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ScalarAdvection;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class VTKWriterTest {

    @Test
    public void write() throws FileNotFoundException {
        File meshFile = new File("test/test_data/mesh_unstructured_2d.cfdu");
        GoverningEquations govEqn = new ScalarAdvection(1, 1, 1);
        Mesh mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(), Map.of());
        File vtkFile = new File("test/test_data/test_vtk.vtu");
        VTKWriter vtkWriter = new VTKWriter(mesh, govEqn);

        Random rnd = new Random(86);
        mesh.cellStream()
                .forEach(cell -> Arrays.fill(cell.U, rnd.nextDouble()));

        vtkWriter.write(vtkFile);
    }
}