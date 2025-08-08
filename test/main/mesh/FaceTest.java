package main.mesh;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.util.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class FaceTest {
    @Test
    public void index_of_face_is_negativeOne_during_construction() {
        Cell left = new Cell(null, null, null, 2);
        Face face = new Face(null, null, null, left, null, 2);

        assertEquals(-1, face.index());
    }

    @Test
    public void setIndex_changes_the_index_of_the_face() {
        Cell left = new Cell(null, null, null, 2);
        Face face = new Face(null, null, null, left, null, 2);

        face.setIndex(25);

        assertEquals(25, face.index());
    }

    @Test
    public void index_of_face_is_can_be_set_only_once() {
        Cell left = new Cell(null, null, null, 2);
        Face face = new Face(null, null, null, left, null, 2);

        face.setIndex(5);

        TestHelper.assertThrows(IllegalStateException.class, () -> face.setIndex(1));
    }

    @Test
    public void faces_are_equal_if_they_have_same_nodes() {
        int numVars = 2;
        Node n0 = new Node(2, 6, 9, numVars);
        Node n1 = new Node(3, -9, 7, numVars);

        Cell left1 = new Cell(null, null, null, 2);
        Surface surface1 = new Surface(2.0, new Point(2, 5, 7), new Vector(1, 2, 4).unit());
        Face f1 = new Face(new Node[]{n0, n1}, null, surface1, left1, null, numVars);

        Cell left2 = new Cell(null, null, null, 2);
        Surface surface2 = new Surface(2.0, new Point(2, 5, 7), new Vector(1, 2, 4).mult(-1).unit());
        Face f2 = new Face(new Node[]{n1, n0}, null, surface2, left2, null, numVars);

        assertEquals(f1, f2);
    }

    @Test
    public void if_faces_are_equal_then_their_right_cells_are_replaced_and_normals_are_averaged() {
        int numVars = 2;
        Node n0 = new Node(2, 6, 9, numVars);
        Node n1 = new Node(3, -9, 7, numVars);

        Cell left1 = new Cell(null, null, null, 2);
        Vector normal1 = new Vector(1, 2, 4).unit();
        Surface surface1 = new Surface(2.0, new Point(2, 5, 7), normal1);
        Face f1 = new Face(new Node[]{n0, n1}, null, surface1, left1, null, numVars);

        Cell left2 = new Cell(null, null, null, 2);
        Vector normal2 = new Vector(1.2, 1.6, 4.2).mult(-1).unit();
        Surface surface2 = new Surface(2.0, new Point(2, 5, 7), normal2);
        Face f2 = new Face(new Node[]{n1, n0}, null, surface2, left2, null, numVars);

        assertEquals(f1, f2);

        assertSame(f1.right, f2.left);
        assertSame(f2.right, f1.left);

        Vector avgNormal = normal1.sub(normal2).mult(0.5).unit();
        TestHelper.assertVectorEquals(avgNormal, f1.surface.unitNormal(), 1e-15);
        TestHelper.assertVectorEquals(f1.surface.unitNormal(), f2.surface.unitNormal().mult(-1), 1e-15);
    }

    @Test
    public void hashCode_of_face_returns_sum_of_node_hashCodes() {
        int numVars = 2;
        Node n0 = new Node(2, 6, 9, numVars);
        Node n1 = new Node(3, -9, 7, numVars);
        Cell left = new Cell(null, null, null, numVars);
        Face face = new Face(new Node[]{n0, n1}, null, null, left, null, numVars);

        assertEquals(n0.hashCode() + n1.hashCode(), face.hashCode());
    }

    @Test
    public void test_toString() {
        int numVars = 2;
        Node n0 = new Node(2, 6, 9, numVars);
        Node n1 = new Node(3, -9, 7, numVars);

        Cell left = new Cell(null, null, null, 2);
        Cell right = new Cell(null, null, null, 2);
        Vector normal = new Vector(0.8, 0.6, 0).unit();
        Surface surface = new Surface(2.0, new Point(2, 5, 7), normal);
        Face face = new Face(new Node[]{n0, n1}, VTKType.VTK_LINE, surface, left, right, numVars);

        assertEquals(
                "Face{vtkType=VTK_LINE, " +
                "surface=Surface{area=2.0, " +
                "centroid=Point{x=2.0, y=5.0, z=7.0}, " +
                "unitNormal=Vector{x=0.8, y=0.6, z=0.0}}}",
                face.toString());
    }
}
