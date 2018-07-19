package main.mesh;

import org.junit.Test;

import static main.mesh.Dimension.ONE_DIM;
import static main.mesh.Dimension.THREE_DIM;
import static main.mesh.Dimension.TWO_DIM;
import static org.junit.Assert.*;

public class DimensionTest {

    @Test
    public void getDimension() {
        Dimension dim = ONE_DIM;
        assertEquals(1, dim.dim);

        dim = TWO_DIM;
        assertEquals(2, dim.dim);

        dim = THREE_DIM;
        assertEquals(3, dim.dim);

        assertEquals(ONE_DIM, Dimension.getDimension(1));
        assertEquals(TWO_DIM, Dimension.getDimension(2));
        assertEquals(THREE_DIM, Dimension.getDimension(3));
        try {
            assertEquals(THREE_DIM, Dimension.getDimension(4));
            fail("Exception not thrown as expected.");
        } catch (IllegalArgumentException e) {
            // exception thrown as expected
        }
    }
}