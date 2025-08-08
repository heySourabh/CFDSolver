package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.jupiter.api.Test;

import static main.util.TestHelper.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineTest {

    @Test
    public void points() {
        Point[] p = {new Point(5, 6, 7), new Point(8.5, 6, 7)};
        Line l = new Line(p[0], p[1]);

        Point[] actualPoints = l.points();
        for (int i = 0; i < p.length; i++) {
            assertEquals(p[i], actualPoints[i]);
        }
    }

    @Test
    public void vtkType() {
        Line line = new Line(new Point(0, 0, 0), new Point(0, 0, 0));
        VTKType expectedVtkType = VTKType.VTK_LINE;
        assertEquals(expectedVtkType, line.vtkType());
    }

    @Test
    public void length() {
        Line l1 = new Line(new Point(5, 6, 7), new Point(8.5, 6, 7));
        double expLength1 = 3.5;
        assertEquals(expLength1, l1.length(), 1e-15);

        Line l2 = new Line(new Point(5, 10, 7), new Point(5, 6, 7));
        double expLength2 = 4;
        assertEquals(expLength2, l2.length(), 1e-12);

        Line l3 = new Line(new Point(5, 6, 1), new Point(5, 6, 7));
        double expLength3 = 6;
        assertEquals(expLength3, l3.length(), 1e-12);

        Line l4 = new Line(new Point(5, 6, 6), new Point(8, 11, 1));
        double expLength4 = 7.681145747868;
        assertEquals(expLength4, l4.length(), 1e-12);
    }

    @Test
    public void area() {
        Line l = new Line(new Point(5, 6, 6), new Point(8, 11, 1));
        assertThrows(ArithmeticException.class, l::area);
    }

    @Test
    public void volume() {
        Line l = new Line(new Point(5, 6, 6), new Point(8, 11, 1));
        assertThrows(ArithmeticException.class, l::volume);
    }

    @Test
    public void centroid() {
        Line l1 = new Line(new Point(5, 6, 7), new Point(8.5, 6, 7));
        Point expCentroid1 = new Point(6.75, 6, 7);
        assertEquals(0, expCentroid1.distance(l1.centroid()), 1e-8);

        Line l2 = new Line(new Point(5, 10, 7), new Point(5, 6, 7));
        Point expCentroid2 = new Point(5, 8, 7);
        assertEquals(0, expCentroid2.distance(l2.centroid()), 1e-8);

        Line l3 = new Line(new Point(5, 6, 1), new Point(5, 6, 7));
        Point expCentroid3 = new Point(5, 6, 4);
        assertEquals(0, expCentroid3.distance(l3.centroid()), 1e-8);

        Line l4 = new Line(new Point(5, 6, 6), new Point(8, 11, 1));
        Point expCentroid4 = new Point(6.5, 8.5, 3.5);
        assertEquals(0, expCentroid4.distance(l4.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Line l = new Line(new Point(5, 6, 6), new Point(8, 11, 1));
        assertThrows(ArithmeticException.class, l::unitNormal);
    }
}