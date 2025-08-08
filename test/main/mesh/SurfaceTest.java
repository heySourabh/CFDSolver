package main.mesh;

import main.geom.Point;
import main.geom.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testCalculationOfUnitTangents() {
        Vector unitNormal = new Vector(1, 0, 0).unit();
        Surface surface = new Surface(2.0, null, unitNormal);
        Vector[] tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(0, 1, 0).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(0, 0, 1).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(0, 1, 1).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(1, 1, 1).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(1, 2, 1).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);

        // Another test
        unitNormal = new Vector(2, 3, -7).unit();

        surface = new Surface(2.0, null, unitNormal);
        tangents = new Vector[]{
                surface.unitTangent1(),
                surface.unitTangent2()
        };

        assertEquals(1, tangents[0].mag(), 1e-15);
        assertEquals(1, tangents[1].mag(), 1e-15);
        assertEquals(0, tangents[0].dot(tangents[1]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[0]), 1e-15);
        assertEquals(0, unitNormal.dot(tangents[1]), 1e-15);
    }
}