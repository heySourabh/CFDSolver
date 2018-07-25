package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import static org.junit.Assert.*;

public class TetraTest {

    @Test
    public void points() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        Point[] points = tetra.points();

        assertSame(p0, points[0]);
        assertSame(p1, points[1]);
        assertSame(p2, points[2]);
        assertSame(p3, points[3]);
    }

    @Test
    public void vtkType() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        VTKType expectedVtkType = VTKType.VTK_TETRA;

        assertEquals(expectedVtkType, tetra.vtkType());
    }

    @Test
    public void length() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        try {
            tetra.length();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }

    @Test
    public void area() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        try {
            tetra.area();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }

    @Test
    public void volume1() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        double expectedVolume = 18161.16925624;

        assertEquals(0, (expectedVolume - tetra.volume()) / expectedVolume, 1e-8);
    }

    @Test
    public void volume2() {
        Point p0 = new Point(-12.37881651, 19.13810406, 33.14816859);
        Point p1 = new Point(-28.49736977, 2.32764886, 4.03160609);
        Point p2 = new Point(17.56173243, -5.65730861, -9.79874594);
        Point p3 = new Point(-9.41440573, 24.77449016, -33.08932431);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        double expectedVolume = 11437.75219223;

        assertEquals(0, (expectedVolume - tetra.volume()) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid1() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        Point expectedCentroid = new Point(0.13792602, 29.50000000, -7.06847639);

        assertEquals(0, expectedCentroid.distance(tetra.centroid()), 1e-8);
    }

    @Test
    public void centroid2() {
        Point p0 = new Point(-12.37881651, 19.13810406, 33.14816859);
        Point p1 = new Point(-28.49736977, 2.32764886, 4.03160609);
        Point p2 = new Point(17.56173243, -5.65730861, -9.79874594);
        Point p3 = new Point(-9.41440573, 24.77449016, -33.08932431);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        Point expectedCentroid = new Point(-8.18221489, 10.14573362, -1.42707389);

        assertEquals(0, expectedCentroid.distance(tetra.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Point p0 = new Point(-1.31049078, 20, -36.98055193);
        Point p1 = new Point(-3.02155309, 58, -11.31461721);
        Point p2 = new Point(-28.49736977, 20, 4.65529772);
        Point p3 = new Point(33.38111771, 20, 15.36596587);
        Tetra tetra = new Tetra(p0, p1, p2, p3);

        try {
            tetra.unitNormal();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expecting an exception
        }
    }
}