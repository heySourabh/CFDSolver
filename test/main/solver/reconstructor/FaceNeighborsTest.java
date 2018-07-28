package main.solver.reconstructor;

import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FaceNeighborsTest {

    @Test
    public void get() throws FileNotFoundException {
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d.cfdu"), 5, Map.of());
        Neighbors faceNeighbors = new FaceNeighbors();

        Cell c0 = mesh.cells().get(0);
        Cell c1 = mesh.cells().get(1);
        Cell c2 = mesh.cells().get(2);
        Cell c5 = mesh.cells().get(5);
        List<Cell> expectedNeighs = List.of(c0, c2, c5);
        List<Cell> actualNeighs = faceNeighbors.getFor(c1);

        assertEquals(expectedNeighs.size(), actualNeighs.size());
        assertTrue(expectedNeighs.containsAll(actualNeighs));
    }
}