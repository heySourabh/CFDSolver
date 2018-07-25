package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import static org.junit.Assert.*;

public class PyramidTest {

    @Test
    public void points() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);

        Point[] points = pyramid.points();
        assertSame(p0, points[0]);
        assertSame(p1, points[1]);
        assertSame(p2, points[2]);
        assertSame(p3, points[3]);
        assertSame(p4, points[4]);
    }

    @Test
    public void vtkType() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);
        VTKType expectedVtkType = VTKType.VTK_PYRAMID;

        assertEquals(expectedVtkType, pyramid.vtkType());
    }

    @Test
    public void length() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);

        try {
            pyramid.length();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }

    @Test
    public void area() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);

        try {
            pyramid.area();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }

    @Test
    public void volume1() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);
        double expectedVolume = 25245.30479773;

        assertEquals(0, (expectedVolume - pyramid.volume()) / expectedVolume, 1e-8);
    }

    @Test
    public void volume2() {
        Point p0 = new Point(26.54862185, 27.67729656, 24.62868105);
        Point p1 = new Point(50.85265875, 25.55096885, 30.01284617);
        Point p2 = new Point(50.85265875, 25.55096885, -27.28829024);
        Point p3 = new Point(19.23072495, 28.31752958, -25.96277790);
        Point p4 = new Point(57.68756937, 70.12488434, -30.78560974);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);
        double expectedVolume = 22602.12286765;

        assertEquals(0, (expectedVolume - pyramid.volume()) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid1() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);
        Point expectedCentroid = new Point(44.11471866, 37.43343540, -0.86218454);

        assertEquals(0, expectedCentroid.distance(pyramid.centroid()), 1e-8);
    }

    @Test
    public void centroid2() {
        Point p0 = new Point(26.54862185, 27.67729656, 24.62868105);
        Point p1 = new Point(50.85265875, 25.55096885, 30.01284617);
        Point p2 = new Point(50.85265875, 25.55096885, -27.28829024);
        Point p3 = new Point(19.23072495, 28.31752958, -25.96277790);
        Point p4 = new Point(57.68756937, 70.12488434, -30.78560974);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);
        Point expectedCentroid = new Point(42.24712155, 37.59682897, -8.36044916);

        assertEquals(0, expectedCentroid.distance(pyramid.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Point p0 = new Point(27.07853524, 27.63093514, -26.81737955);
        Point p1 = new Point(27.07853524, 27.63093514, 27.51093247);
        Point p2 = new Point(57.93934901, 24.93096379, 27.51093247);
        Point p3 = new Point(57.93934901, 24.93096379, -26.81737955);
        Point p4 = new Point(48.93204823, 70.89089319, -4.48906753);

        Pyramid pyramid = new Pyramid(p0, p1, p2, p3, p4);

        try {
            pyramid.unitNormal();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }
}