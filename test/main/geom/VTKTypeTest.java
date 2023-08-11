package main.geom;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VTKTypeTest {

    @Test
    public void numPoints() {
        assertEquals(1, VTKType.VTK_VERTEX.numPoints());
        assertEquals(0, VTKType.VTK_POLY_VERTEX.numPoints());
        assertEquals(2, VTKType.VTK_LINE.numPoints());
        assertEquals(0, VTKType.VTK_POLY_LINE.numPoints());
        assertEquals(3, VTKType.VTK_TRIANGLE.numPoints());
        assertEquals(0, VTKType.VTK_TRIANGLE_STRIP.numPoints());
        assertEquals(0, VTKType.VTK_POLYGON.numPoints());
        assertEquals(4, VTKType.VTK_PIXEL.numPoints());
        assertEquals(4, VTKType.VTK_QUAD.numPoints());
        assertEquals(4, VTKType.VTK_TETRA.numPoints());
        assertEquals(8, VTKType.VTK_VOXEL.numPoints());
        assertEquals(8, VTKType.VTK_HEXAHEDRON.numPoints());
        assertEquals(6, VTKType.VTK_WEDGE.numPoints());
        assertEquals(5, VTKType.VTK_PYRAMID.numPoints());
        assertEquals(3, VTKType.VTK_QUADRATIC_EDGE.numPoints());
        assertEquals(6, VTKType.VTK_QUADRATIC_TRIANGLE.numPoints());
        assertEquals(8, VTKType.VTK_QUADRATIC_QUAD.numPoints());
        assertEquals(10, VTKType.VTK_QUADRATIC_TETRA.numPoints());
        assertEquals(20, VTKType.VTK_QUADRATIC_HEXAHEDRON.numPoints());
    }

    @Test
    public void getIDTest() {
        assertEquals(VTKType.VTK_VERTEX, VTKType.get(1));
        assertEquals(VTKType.VTK_POLY_VERTEX, VTKType.get(2));
        assertEquals(VTKType.VTK_LINE, VTKType.get(3));
        assertEquals(VTKType.VTK_POLY_LINE, VTKType.get(4));
        assertEquals(VTKType.VTK_TRIANGLE, VTKType.get(5));
        assertEquals(VTKType.VTK_TRIANGLE_STRIP, VTKType.get(6));
        assertEquals(VTKType.VTK_POLYGON, VTKType.get(7));
        assertEquals(VTKType.VTK_PIXEL, VTKType.get(8));
        assertEquals(VTKType.VTK_QUAD, VTKType.get(9));
        assertEquals(VTKType.VTK_TETRA, VTKType.get(10));
        assertEquals(VTKType.VTK_VOXEL, VTKType.get(11));
        assertEquals(VTKType.VTK_HEXAHEDRON, VTKType.get(12));
        assertEquals(VTKType.VTK_WEDGE, VTKType.get(13));
        assertEquals(VTKType.VTK_PYRAMID, VTKType.get(14));
        assertEquals(VTKType.VTK_QUADRATIC_EDGE, VTKType.get(21));
        assertEquals(VTKType.VTK_QUADRATIC_TRIANGLE, VTKType.get(22));
        assertEquals(VTKType.VTK_QUADRATIC_QUAD, VTKType.get(23));
        assertEquals(VTKType.VTK_QUADRATIC_TETRA, VTKType.get(24));
        assertEquals(VTKType.VTK_QUADRATIC_HEXAHEDRON, VTKType.get(25));

        assertThrows(NoSuchElementException.class, () -> VTKType.get(26));
    }
}
