package main.mesh;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.util.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CellTest {

    @Test
    public void the_index_is_negativeOne_by_default() {
        assertEquals(-1, new Cell(null, null, null, 2).index());
    }

    @Test
    public void setIndex_modifies_the_index() {
        Cell cell = new Cell(null, null, null, 3);
        cell.setIndex(2);
        assertEquals(2, cell.index());
    }

    @Test
    public void setIndex_throws_exception_when_called_more_than_once_with_non_negative_index() {
        Cell cell = new Cell(null, null, null, 3);
        cell.setIndex(1);

        TestHelper.assertThrows(IllegalStateException.class, () -> cell.setIndex(1));
    }

    @Test
    public void test_toString() {
        int numVars = 4;
        Node[] nodes = {
                new Node(1, 3, 5, numVars),
                new Node(6, 7, 4, numVars)
        };
        Cell cell = new Cell(nodes, VTKType.VTK_QUAD, new Shape(1.5, new Point(5, 7, 4)), 3);
        cell.faces.addAll(List.of(
                new Face(new Node[]{new Node(0, 6, 3, numVars)}, VTKType.VTK_LINE,
                        new Surface(2.3, new Point(7, 8, 9), new Vector(3.5, 9, 2).unit()),
                        cell, null, 4)
        ));
        cell.setIndex(9);
        assertEquals("Cell{\n" +
                     "index=9\n" +
                     "nodes=[Node{x=1.0, y=3.0, z=5.0}, Node{x=6.0, y=7.0, z=4.0}]\n" +
                     "faces=[Face{vtkType=VTK_LINE, surface=Surface{area=2.3, centroid=Point{x=7.0, y=8.0, z=9.0}, unitNormal=Vector{x=0.3549140885943757, y=0.9126362278141088, z=0.20280805062535753}}}]\n" +
                     "vtkType=VTK_QUAD\n" +
                     "shape=Shape{volume=1.5, centroid=Point{x=5.0, y=7.0, z=4.0}}\n" +
                     "}",
                cell.toString());
    }
}