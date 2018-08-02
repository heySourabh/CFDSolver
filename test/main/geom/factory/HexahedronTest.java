package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import static main.TestHelper.assertThrows;
import static org.junit.Assert.assertEquals;

/**
 * Test for Hexahedron geometry.
 * These volumes are created in SolidWorks software
 * and the expected volume and center of gravity values
 * are measured from the models in SolidWorks.
 */
public class HexahedronTest {

    @Test
    public void points() {
        Point[] p = {
                new Point(-5.9, 24, 3.6),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 24, 3.6),
                new Point(-5.9, 24, -41.4),
                new Point(-5.9, 0, -41.4),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 24, -41.4)
        };
        Hexahedron hexahedron = new Hexahedron(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);

        Point[] actualPoints = hexahedron.points();
        for (int i = 0; i < p.length; i++) {
            assertEquals(p[i], actualPoints[i]);
        }
    }

    @Test
    public void vtkType() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0));
        VTKType expectedVtkType = VTKType.VTK_HEXAHEDRON;

        assertEquals(expectedVtkType, hexahedron.vtkType());
    }

    @Test
    public void length() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0),
                new Point(0, 0, 0), new Point(0, 0, 0));
        assertThrows(ArithmeticException.class, hexahedron::length);
    }

    @Test
    public void area() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 24, 3.6),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 24, 3.6),
                new Point(-5.9, 24, -41.4),
                new Point(-5.9, 0, -41.4),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 24, -41.4));
        assertThrows(ArithmeticException.class, hexahedron::area);
    }

    @Test
    public void volume1() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 24, 3.6),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 24, 3.6),
                new Point(-5.9, 24, -41.4),
                new Point(-5.9, 0, -41.4),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 24, -41.4));
        double expectedVolume = 62640.0;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid1() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 24, 3.6),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 24, 3.6),
                new Point(-5.9, 24, -41.4),
                new Point(-5.9, 0, -41.4),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 24, -41.4));
        Point expectedCentroid = new Point(23.1, 12.0, -18.9);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void volume2() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5, -5, 5),
                new Point(-5, 5, 5),
                new Point(-5, 5, -5),
                new Point(-5, -5, -5),
                new Point(16, 0, 10),
                new Point(16, 10, 10),
                new Point(16, 10, 0),
                new Point(16, 0, 0));
        double expectedVolume = 2100;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid2() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5, -5, 5),
                new Point(-5, 5, 5),
                new Point(-5, 5, -5),
                new Point(-5, -5, -5),
                new Point(16, 0, 10),
                new Point(16, 10, 10),
                new Point(16, 10, 0),
                new Point(16, 0, 0));
        Point expectedCentroid = new Point(5.5, 2.5, 2.5);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void volume3() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 0, -41.4),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 0, -41.4),
                new Point(-5.9, 27, -36.4),
                new Point(-5.9, 27, 3.6),
                new Point(39.1, 27, 3.6),
                new Point(39.1, 27, -36.4));
        double expectedVolume = 59242.50;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid3() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 0, -41.4),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 0, -41.4),
                new Point(-5.9, 27, -36.4),
                new Point(-5.9, 27, 3.6),
                new Point(39.1, 27, 3.6),
                new Point(39.1, 27, -36.4));
        Point expectedCentroid = new Point(20.04996202, 12.66938853, -17.72690847);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void volume4() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-21.4, 27, -27.15),
                new Point(-5.9, 0, -41.4),
                new Point(-5.9, 0, 3.6),
                new Point(-21.4, 27, 12.85),
                new Point(23.6, 27, -27.15),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 0, 3.6),
                new Point(23.6, 27, 12.85));
        double expectedVolume = 59242.50;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid4() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-21.4, 27, -27.15),
                new Point(-5.9, 0, -41.4),
                new Point(-5.9, 0, 3.6),
                new Point(-21.4, 27, 12.85),
                new Point(23.6, 27, -27.15),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 0, 3.6),
                new Point(23.6, 27, 12.85));
        Point expectedCentroid = new Point(12.77679453, 12.66938853, -13.38646981);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void volume5() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-9.44148018, -16.85688821, 11.49772459),
                new Point(23.99430813, -46.13201505, -11.61136472),
                new Point(21.11285727, -60.97535829, 30.77226894),
                new Point(-12.00276983, -30.05097109, 49.17206562),
                new Point(31.61263554, -0.51473367, 20.01204002),
                new Point(76.90850173, -25.06879364, -0.63735817),
                new Point(74.02705087, -39.91213689, 41.74627549),
                new Point(29.05134590, -13.70881656, 57.68638105));
        double expectedVolume = 99724.8750;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid5() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-9.44148018, -16.85688821, 11.49772459),
                new Point(23.99430813, -46.13201505, -11.61136472),
                new Point(21.11285727, -60.97535829, 30.77226894),
                new Point(-12.00276983, -30.05097109, 49.17206562),
                new Point(31.61263554, -0.51473367, 20.01204002),
                new Point(76.90850173, -25.06879364, -0.63735817),
                new Point(74.02705087, -39.91213689, 41.74627549),
                new Point(29.05134590, -13.70881656, 57.68638105));
        Point expectedCentroid = new Point(30.61390777, -30.00581667, 24.22911134);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void volume6() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(72.60000000, -6.84846355, 32.21948781),
                new Point(-16, -0.97835194, 4.60278397),
                new Point(-16, 40.08835590, 1.00991259),
                new Point(72.60000000, 42.44156096, 27.90716944),
                new Point(-48.25000000, -9.23999050, 43.47073752),
                new Point(-35, 11.30540014, -53.18772590),
                new Point(-35, 35.16405644, -55.27508785),
                new Point(-48.25000000, 43.40027413, 38.86531112));
        double expectedVolume = 151419.29275255;

        assertEquals(0, (hexahedron.volume() - expectedVolume) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid6() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(72.60000000, -6.84846355, 32.21948781),
                new Point(-16, -0.97835194, 4.60278397),
                new Point(-16, 40.08835590, 1.00991259),
                new Point(72.60000000, 42.44156096, 27.90716944),
                new Point(-48.25000000, -9.23999050, 43.47073752),
                new Point(-35, 11.30540014, -53.18772590),
                new Point(-35, 35.16405644, -55.27508785),
                new Point(-48.25000000, 43.40027413, 38.86531112));
        Point expectedCentroid = new Point(-9.18930495, 18.66217152, 16.64570516);

        assertEquals(0, expectedCentroid.distance(hexahedron.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Hexahedron hexahedron = new Hexahedron(
                new Point(-5.9, 24, 3.6),
                new Point(-5.9, 0, 3.6),
                new Point(52.1, 0, 3.6),
                new Point(52.1, 24, 3.6),
                new Point(-5.9, 24, -41.4),
                new Point(-5.9, 0, -41.4),
                new Point(52.1, 0, -41.4),
                new Point(52.1, 24, -41.4));
        assertThrows(ArithmeticException.class, hexahedron::unitNormal);
    }
}
