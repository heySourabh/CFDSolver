package main.mesh;

import main.geom.Point;
import main.geom.Vector;
import org.junit.Test;

import static org.junit.Assert.*;

public class SurfaceTest {

    @Test
    public void testToString() {
        double area = 6513.6541;
        Vector normal = new Vector(54, -354, 651).unit();
        Point centroid = new Point(-81, 8, 87);

        Surface surface = new Surface(area, centroid, normal);

        assertEquals("Surface{" +
                "area=" + area + ", " +
                "centroid=" + centroid + ", " +
                "unitNormal=" + normal +
                "}", surface.toString());
    }
}