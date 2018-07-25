package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import static org.junit.Assert.*;

public class PolyLineTest {

    @Test
    public void points() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)};
        PolyLine polyLine = new PolyLine(p);
        Point[] actualPoints = polyLine.points();

        for (int i = 0; i < p.length; i++) {
            assertEquals(p[i], actualPoints[i]);
        }
    }

    @Test
    public void vtkType() {
        PolyLine polyLine = new PolyLine(new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)});
        VTKType expectedVtkType = VTKType.VTK_POLY_LINE;

        assertEquals(expectedVtkType, polyLine.vtkType());
    }

    @Test
    public void length1() {
        PolyLine polyLine = new PolyLine(new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7),
                new Point(-10, 6, -12)});
        // Calculated using hand-held calculator
        double expectedLength = 5.196152423 + 3.31662479 + 23.36664289;

        assertEquals(0, (expectedLength - polyLine.length()) / expectedLength, 1e-8);
    }

    @Test
    public void length2() {
        PolyLine polyLine = new PolyLine(new Point[]{
                new Point(-166.02170217, -194.54201633, -4.58945093),
                new Point(-166.02170217, -44.84761406, 83.20591431),
                new Point(-166.02170217, 109.57666338, -56.89234746),
                new Point(-166.02170217, -139.92211725, 333.99627403),
                new Point(-123.05628372, -203.64316094, 333.99627403),
                new Point(-65.09865515, -116.43268044, 333.99627403),
                new Point(-15.50992500, -189.97660843, 333.99627403),
                new Point(-15.50992500, -126.04375365, 333.99627403),
                new Point(-15.50992500, -204.40739446, 211.07274670),
                new Point(-56.96011292, 22.78328611, 149.41584897),
                new Point(-15.50992500, 63.38026373, 0.33658401),
                new Point(-57.26960640, -20.32472339, 120.15437401),
                new Point(-40.02592481, 9.47184558, -80.52042910),
                new Point(-15.50992500, -23.95235846, -39.35067468),
                new Point(27.92664463, -49.53476901, -72.72644802),
                new Point(42.01352961, 23.02743056, -44.15365534),
                new Point(42.01352961, 23.02743056, 0),
                new Point(0, 0, 0)
        });
        // Measured using SolidWorks
        double expectedLength = 173.54088922 + 208.50510885 + 463.72788998 + 76.85309747 + 104.71272425
                + 88.70034669 + 63.93285478 + 145.77741174 + 239.02990704 + 159.97143467
                + 152.00887691 + 203.60637681 + 58.42225895 + 60.45773364 + 79.24719312
                + 44.15365534 + 47.91032486;

        assertEquals(0, (expectedLength - polyLine.length()) / expectedLength, 1e-8);
    }

    @Test
    public void area() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)};
        PolyLine polyLine = new PolyLine(p);

        try {
            polyLine.area();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expected exception
        }
    }

    @Test
    public void volume() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)};
        PolyLine polyLine = new PolyLine(p);

        try {
            polyLine.volume();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expected exception
        }
    }

    @Test
    public void centroid() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)};
        PolyLine polyLine = new PolyLine(p);

        try {
            polyLine.centroid();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expected exception
        }
    }

    @Test
    public void unitNormal() {
        Point[] p = new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)};
        PolyLine polyLine = new PolyLine(p);

        try {
            polyLine.unitNormal();
            fail("Expecting an ArithmeticException to be thrown.");
        } catch (ArithmeticException ex) {
            // OK: expected exception
        }
    }
}