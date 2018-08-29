package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import org.junit.Test;

import static main.util.TestHelper.assertThrows;
import static org.junit.Assert.assertEquals;

/**
 * Test for Polygon geometry.
 * The non-convex polygon is created in SolidWorks software on an arbitrary plane.
 * The expected area, centroid and normal values
 * are measured from a sketch in SolidWorks.
 */
public class PolygonTest {

    @Test
    public void points() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(1, 4, 5),
                new Point(-5, 4, 3)};
        Polygon polygon = new Polygon(p);

        Point[] actualPoints = polygon.points();

        for (int i = 0; i < p.length; i++) {
            assertEquals(p[i], actualPoints[i]);
        }
    }

    @Test
    public void vtkType() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(1, 2, 3),
                new Point(1, 4, 5),
                new Point(-5, 4, 3)
        });
        VTKType expectedVtkType = VTKType.VTK_POLYGON;
        assertEquals(expectedVtkType, polygon.vtkType());
    }

    @Test
    public void length() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(1, 2, 3),
                new Point(1, 4, 5),
                new Point(-5, 4, 3)
        });
        assertThrows(ArithmeticException.class, polygon::length);
    }

    @Test
    public void area() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(-52.53629977, 32.69717636, -23.46507803),
                new Point(-10.89582092, -8.42551076, -55.18888104),
                new Point(38.85242487, -10.11055088, -4.97560461),
                new Point(47.08662181, 4.15149657, 30.34098003),
                new Point(27.56095052, 27.98460803, 53.66730578),
                new Point(23.94910928, 8.00191402, 12.68268771),
                new Point(-18.31327865, 39.94189721, 26.68555204),
                new Point(-30.99926440, 28.13367138, -8.84724722)}
        );
        double expectedArea = 5818.39407537;

        assertEquals(0, (expectedArea - polygon.area()) / expectedArea, 1e-8);
    }

    @Test
    public void volume() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(1, 2, 3),
                new Point(1, 4, 5),
                new Point(-5, 4, 3)
        });
        assertThrows(ArithmeticException.class, polygon::volume);
    }

    @Test
    public void centroid() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(-10.89582092, -8.42551076, -55.18888104),
                new Point(38.85242487, -10.11055088, -4.97560461),
                new Point(47.08662181, 4.15149657, 30.34098003),
                new Point(27.56095052, 27.98460803, 53.66730578),
                new Point(23.94910928, 8.00191402, 12.68268771),
                new Point(-18.31327865, 39.94189721, 26.68555204),
                new Point(-30.99926440, 28.13367138, -8.84724722),
                new Point(-52.53629977, 32.69717636, -23.46507803)}
        );
        Point expectedCentroid = new Point(1.00027384, 10.86675117, -6.60371219);

        assertEquals(0, expectedCentroid.distance(polygon.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Polygon polygon = new Polygon(new Point[]{
                new Point(-10.89582092, -8.42551076, -55.18888104),
                new Point(38.85242487, -10.11055088, -4.97560461),
                new Point(47.08662181, 4.15149657, 30.34098003),
                new Point(27.56095052, 27.98460803, 53.66730578),
                new Point(23.94910928, 8.00191402, 12.68268771),
                new Point(-18.31327865, 39.94189721, 26.68555204),
                new Point(-30.99926440, 28.13367138, -8.84724722),
                new Point(-52.53629977, 32.69717636, -23.46507803)}
        );

        // The polygon points are in clockwise-direction for the measured normal,
        // therefore normal must in opposite direction.
        Vector expectedUnitNormal = new Vector(0.4531538935, 0.7848855672, -0.4226182617).mult(-1);
        Vector actualUnitNormal = polygon.unitNormal();
        Point componentsExpected = new Point(expectedUnitNormal.x, expectedUnitNormal.y, expectedUnitNormal.z);
        Point componentsResult = new Point(actualUnitNormal.x, actualUnitNormal.y, actualUnitNormal.z);

        assertEquals(0, componentsExpected.distance(componentsResult), 1e-8);
        assertEquals(1, actualUnitNormal.mag(), 1e-8);
    }
}