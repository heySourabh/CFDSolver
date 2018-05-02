package main.geom.factory;

import main.geom.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Hexahedron geometry.
 * These volumes are created in SolidWorks software
 * and the volume and center of gravity values are measured from the models.
 */
class HexahedronTest {

    @Test
    void volume1() {
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

        assertEquals(expectedVolume, hexahedron.volume(), 1e-12);
    }

    @Test
    void centroid1() {
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

        assertEquals(expectedCentroid, hexahedron.centroid());
    }

    @Test
    void volume2() {
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

        assertEquals(expectedVolume, hexahedron.volume(), 1e-12);
    }

    @Test
    void centroid2() {
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

        assertEquals(expectedCentroid, hexahedron.centroid());
    }

    @Test
    void volume3() {
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

        assertEquals(expectedVolume, hexahedron.volume(), 1e-12);
    }

    @Test
    void centroid3() {
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

        assertEquals(expectedCentroid, hexahedron.centroid());
    }

    @Test
    void volume4() {
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

        assertEquals(expectedVolume, hexahedron.volume(), 1e-8);
    }

    @Test
    void centroid4() {
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

        assertEquals(expectedCentroid, hexahedron.centroid());
    }

    @Test
    void volume5() {
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

        assertEquals(expectedVolume, hexahedron.volume(), 1e-4);
    }

    @Test
    void centroid5() {
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

        assertEquals(expectedCentroid, hexahedron.centroid());
    }
}
