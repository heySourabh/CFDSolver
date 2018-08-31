package main.geom;

import main.geom.GeometryHelper.TriGeom;
import main.util.TestHelper;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeometryHelperTest {

    @Test
    public void signedVolume_ForCounterClockwiseTetrahedron_IsPositive() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p2, p3),
                new TriGeom(p0, p1, p3),
                new TriGeom(p0, p2, p1),
                new TriGeom(p0, p3, p2)
        };

        double expectedSignedVolume = 7035.37857914;
        double actualSignedVolume = GeometryHelper.signedVolume(triangles);
        assertEquals(0,
                Math.abs(expectedSignedVolume - actualSignedVolume) / Math.abs(expectedSignedVolume),
                1e-8);
    }

    @Test
    public void signedVolume_ForClockwiseTetrahedron_IsNegative() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p3, p2),
                new TriGeom(p0, p3, p1),
                new TriGeom(p0, p1, p2),
                new TriGeom(p0, p2, p3)
        };

        double expectedSignedVolume = -7035.37857914;
        double actualSignedVolume = GeometryHelper.signedVolume(triangles);
        assertEquals(0,
                Math.abs(expectedSignedVolume - actualSignedVolume) / Math.abs(expectedSignedVolume),
                1e-8);
    }

    @Test
    public void volume_ForCounterClockwiseTetrahedron_IsPositive() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p2, p3),
                new TriGeom(p0, p1, p3),
                new TriGeom(p0, p2, p1),
                new TriGeom(p0, p3, p2)
        };

        double expectedVolume = 7035.37857914;
        double actualVolume = GeometryHelper.volume(triangles);

        assertEquals(0,
                Math.abs(expectedVolume - actualVolume) / Math.abs(expectedVolume),
                1e-8);
    }

    @Test
    public void volume_ForClockwiseTetrahedron_IsPositive() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p3, p2),
                new TriGeom(p0, p3, p1),
                new TriGeom(p0, p1, p2),
                new TriGeom(p0, p2, p3)
        };

        double expectedVolume = 7035.37857914;
        double actualVolume = GeometryHelper.volume(triangles);

        assertEquals(0,
                Math.abs(expectedVolume - actualVolume) / Math.abs(expectedVolume),
                1e-8);
    }

    @Test
    public void centroid_ForCounterClockwiseTetrahedron() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p2, p3),
                new TriGeom(p0, p1, p3),
                new TriGeom(p0, p2, p1),
                new TriGeom(p0, p3, p2)
        };
        Point expectedCentroid = new Point(31.70241991, 8.86764951, -7.7785319);
        Point actualCentroid = GeometryHelper.centroid(triangles);

        TestHelper.assertPointEquals(expectedCentroid, actualCentroid, 1e-8);
    }

    @Test
    public void centroid_ForClockwiseTetrahedron() {
        Point p0 = new Point(0.0, 35.47059805, 0.0);
        Point p1 = new Point(27.9765685, 0.0, -18.04096473);
        Point p2 = new Point(50.4624086, 0.0, -33.72876015);
        Point p3 = new Point(48.37070254, 0.0, 20.6555973);
        TriGeom[] triangles = {
                new TriGeom(p1, p3, p2),
                new TriGeom(p0, p3, p1),
                new TriGeom(p0, p1, p2),
                new TriGeom(p0, p2, p3)
        };
        Point expectedCentroid = new Point(31.70241991, 8.86764951, -7.7785319);
        Point actualCentroid = GeometryHelper.centroid(triangles);

        TestHelper.assertPointEquals(expectedCentroid, actualCentroid, 1e-8);
    }
}