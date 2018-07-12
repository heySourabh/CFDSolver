package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import org.junit.Test;

import static org.junit.Assert.*;

public class TriangleStripTest {

    @Test
    public void points() {
        Point p0 = new Point(-166.02170217, -194.54201633, -4.58945093);
        Point p1 = new Point(-166.02170217, -44.84761406, 83.20591431);
        Point p2 = new Point(-166.02170217, 109.57666338, -56.89234746);
        Point p3 = new Point(-166.02170217, -139.92211725, 333.99627403);
        Point p4 = new Point(-123.05628372, -203.64316094, 333.99627403);
        Point p5 = new Point(-65.09865515, -116.43268044, 333.99627403);
        Point p6 = new Point(-15.50992500, -189.97660843, 333.99627403);
        Point p7 = new Point(-15.50992500, -126.04375365, 333.99627403);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5, p6, p7);
        Point[] points = triangleStrip.points();

        assertSame(p0, points[0]);
        assertSame(p1, points[1]);
        assertSame(p2, points[2]);
        assertSame(p3, points[3]);
        assertSame(p4, points[4]);
        assertSame(p5, points[5]);
        assertSame(p6, points[6]);
        assertSame(p7, points[7]);
    }

    @Test
    public void vtkType() {
        Point p0 = new Point(-166.02170217, -194.54201633, -4.58945093);
        Point p1 = new Point(-166.02170217, -44.84761406, 83.20591431);
        Point p2 = new Point(-166.02170217, 109.57666338, -56.89234746);
        Point p3 = new Point(-166.02170217, -139.92211725, 333.99627403);
        Point p4 = new Point(-123.05628372, -203.64316094, 333.99627403);
        Point p5 = new Point(-65.09865515, -116.43268044, 333.99627403);
        Point p6 = new Point(-15.50992500, -189.97660843, 333.99627403);
        Point p7 = new Point(-15.50992500, -126.04375365, 333.99627403);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5, p6, p7);

        VTKType expectedVtkType = VTKType.VTK_TRIANGLE_STRIP;

        assertEquals(expectedVtkType, triangleStrip.vtkType());
    }

    @Test
    public void area() {
        Point p0 = new Point(-166.02170217, -194.54201633, -4.58945093);
        Point p1 = new Point(-166.02170217, -44.84761406, 83.20591431);
        Point p2 = new Point(-166.02170217, 109.57666338, -56.89234746);
        Point p3 = new Point(-166.02170217, -139.92211725, 333.99627403);
        Point p4 = new Point(-123.05628372, -203.64316094, 333.99627403);
        Point p5 = new Point(-65.09865515, -116.43268044, 333.99627403);
        Point p6 = new Point(-15.50992500, -189.97660843, 333.99627403);
        Point p7 = new Point(-15.50992500, -126.04375365, 333.99627403);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5, p6, p7);

        double expectedArea = 17264.83069806 + 12704.17372792 + 15948.16832033
                + 3720.07768497 + 4293.54432305 + 1585.17454141;

        assertEquals(0, (expectedArea - triangleStrip.area()) / expectedArea, 1e-8);
    }

    @Test
    public void area1() {
        Point p0 = new Point(136.88, 499.52, 10.12);
        Point p1 = new Point(191.42, 666.20, 5.67);
        Point p2 = new Point(360.12, 444.97, -5.78);
        Point p3 = new Point(382.34, 502.05, 6.12);
        Point p4 = new Point(410.63, 445.48, 12.72);
        Point p5 = new Point(480.33, 565.69, -8.18);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5);

        double expectedArea = 32816.85202103886;

        assertEquals(0, (expectedArea - triangleStrip.area()) / expectedArea, 1e-8);
    }

    @Test
    public void centroid() {
        Point p0 = new Point(136.88, 499.52, 10.12);
        Point p1 = new Point(191.42, 666.20, 5.67);
        Point p2 = new Point(360.12, 444.97, -5.78);
        Point p3 = new Point(382.34, 502.05, 6.12);
        Point p4 = new Point(410.63, 445.48, 12.72);
        Point p5 = new Point(480.33, 565.69, -8.18);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5);
        Point expectedCentroid = new Point(277.2848292532742, 530.0153252030082, 3.106921815674788);

        assertEquals(0, expectedCentroid.distance(triangleStrip.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Point p0 = new Point(136.88, 499.52, 10.12);
        Point p1 = new Point(191.42, 666.20, 5.67);
        Point p2 = new Point(360.12, 444.97, -5.78);
        Point p3 = new Point(382.34, 502.05, 6.12);
        Point p4 = new Point(410.63, 445.48, 12.72);
        Point p5 = new Point(480.33, 565.69, -8.18);

        TriangleStrip triangleStrip = new TriangleStrip(p0, p1, p2, p3, p4, p5);
        Vector expectedUnitNormal = new Vector(-0.00389333195169523, 0.01961925748991702, -0.9997999433395954);

        assertEquals(1, triangleStrip.unitNormal().mag(), 1e-8);
        assertEquals(0, expectedUnitNormal.sub(triangleStrip.unitNormal()).mag(), 1e-8);
    }
}