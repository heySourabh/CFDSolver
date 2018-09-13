package main.solver;

import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FaceBasedCellNeighborsTest {

    @Test
    public void get() throws FileNotFoundException {
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d.cfdu"), 5, Map.of());

        Cell c0 = mesh.cells().get(0);
        Cell c1 = mesh.cells().get(1);
        Cell c2 = mesh.cells().get(2);
        Cell c5 = mesh.cells().get(5);
        List<Cell> expectedNeighs = List.of(c0, c2, c5);

        List<Cell> actualNeighs = new FaceBasedCellNeighbors().calculateFor(c1);

        assertEquals(expectedNeighs.size(), actualNeighs.size());
        assertTrue(expectedNeighs.containsAll(actualNeighs));
    }
}