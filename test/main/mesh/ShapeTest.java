package main.mesh;

import main.geom.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShapeTest {

    @Test
    public void testToString() {
        double volume = 584.87;
        Point centroid = new Point(-654, 65.4, 5);
        Shape shape = new Shape(volume, centroid);

        assertEquals("Shape{" +
                     "volume=" + volume + ", " +
                     "centroid=" + centroid +
                     "}", shape.toString());
    }
}