package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import static main.TestHelper.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class WedgeTest {

    @Test
    public void points() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);
        Point[] points = wedge.points();

        assertSame(p0, points[0]);
        assertSame(p1, points[1]);
        assertSame(p2, points[2]);
        assertSame(p3, points[3]);
        assertSame(p4, points[4]);
        assertSame(p5, points[5]);
    }

    @Test
    public void vtkType() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);

        assertEquals(VTKType.VTK_WEDGE, wedge.vtkType());
    }

    @Test
    public void length() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);
        assertThrows(ArithmeticException.class, wedge::length);
    }

    @Test
    public void area() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);
        assertThrows(ArithmeticException.class, wedge::area);
    }

    @Test
    public void volume() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);

        double expectedVolume = 60465.59351005;

        assertEquals(0, (expectedVolume - wedge.volume()) / expectedVolume, 1e-8);
    }

    @Test
    public void centroid() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);

        Point expectedCentroid = new Point(50.38771410, 47.09795133, -6.49904845);

        assertEquals(0, expectedCentroid.distance(wedge.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Point p0 = new Point(55.60487171, 13.88977409, -2.47153445);
        Point p1 = new Point(27.38285885, 32.21736354, 23.38451830);
        Point p2 = new Point(16.85939643, 39.05137994, -41.44573162);
        Point p3 = new Point(88.55868440, 51.88061744, -2.31303192);
        Point p4 = new Point(64.16816547, 71.77149403, 22.10309522);
        Point p5 = new Point(51.84832137, 75.72060593, -39.24010317);

        Wedge wedge = new Wedge(p0, p1, p2, p3, p4, p5);
        assertThrows(ArithmeticException.class, wedge::unitNormal);
    }
}