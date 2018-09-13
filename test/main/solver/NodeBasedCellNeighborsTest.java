package main.solver;

import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.mesh.Node;
import main.mesh.factory.Unstructured2DMesh;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class NodeBasedCellNeighborsTest {

    @Test
    public void getFor() throws FileNotFoundException {
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d.cfdu"), 5, Map.of());

        Node n0 = mesh.nodes().get(0);
        Node n1 = mesh.nodes().get(1);
        Node n2 = mesh.nodes().get(2);
        Node n3 = mesh.nodes().get(3);
        Node n5 = mesh.nodes().get(5);
        Node n6 = mesh.nodes().get(6);

        Cell c0 = mesh.cells().get(0);
        Cell c2 = mesh.cells().get(2);
        Cell c3 = mesh.cells().get(3);
        Cell c4 = mesh.cells().get(4);
        Cell c5 = mesh.cells().get(5);
        Cell c03 = ghostCellFor(n0, n3, mesh).orElseThrow();
        Cell c12 = ghostCellFor(n1, n2, mesh).orElseThrow();
        Cell c26 = ghostCellFor(n2, n6, mesh).orElseThrow();
        Cell c35 = ghostCellFor(n3, n5, mesh).orElseThrow();
        List<Cell> expectedNeighs = List.of(c0, c2, c3, c4, c5, c03, c12, c26, c35);

        List<Cell> actualNeighs = new NodeBasedCellNeighbors().calculateFor(mesh.cells().get(1));

        assertEquals(expectedNeighs.size(), actualNeighs.size());
        assertTrue(expectedNeighs.containsAll(actualNeighs));
    }

    private Optional<Cell> ghostCellFor(Node n1, Node n2, Mesh mesh) {
        return mesh.boundaryStream()
                .flatMap(boundary -> boundary.faces.stream())
                .filter(face -> madeUpOf(face, n1, n2))
                .findAny()
                .map(face -> face.right);
    }

    private boolean madeUpOf(Face face, Node n1, Node n2) {
        return List.of(face.nodes).containsAll(List.of(n1, n2));
    }
}