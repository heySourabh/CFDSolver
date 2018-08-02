package main.mesh;

import org.junit.Test;

import static main.TestHelper.assertThrows;
import static main.mesh.Dimension.*;
import static org.junit.Assert.assertEquals;

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
        assertThrows(IllegalArgumentException.class, () -> Dimension.getDimension(4));
    }
}