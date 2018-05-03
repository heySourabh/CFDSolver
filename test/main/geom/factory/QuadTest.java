package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuadTest {

    @Test
    void points() {
        Point p0 = new Point(-33.96370350, 21.50426152, -12.73844588);
        Point p1 = new Point(18.80281320, 29.97229549, -13.27749442);
        Point p2 = new Point(-19.85934057, 33.31053014, -17.29041133);
        Point p3 = new Point(-26.17979609, 39.69737547, -20.64449113);

        Quad quad = new Quad(p0, p1, p2, p3);

        Point[] points = quad.points();
        assertSame(p0, points[0]);
        assertSame(p1, points[1]);
        assertSame(p2, points[2]);
        assertSame(p3, points[3]);
    }

    @Test
    void vtkType() {
        Point p0 = new Point(-33.96370350, 21.50426152, -12.73844588);
        Point p1 = new Point(18.80281320, 29.97229549, -13.27749442);
        Point p2 = new Point(-19.85934057, 33.31053014, -17.29041133);
        Point p3 = new Point(-26.17979609, 39.69737547, -20.64449113);

        Quad quad = new Quad(p0, p1, p2, p3);
        VTKType expectedVtkType = VTKType.VTK_QUAD;

        assertEquals(expectedVtkType, quad.vtkType());
    }

    @Test
    void area() {
        Point p0 = new Point(-33.96370350, 21.50426152, -12.73844588);
        Point p1 = new Point(18.80281320, 29.97229549, -13.27749442);
        Point p2 = new Point(-19.85934057, 33.31053014, -17.29041133);
        Point p3 = new Point(-26.17979609, 39.69737547, -20.64449113);

        Quad quad = new Quad(p0, p1, p2, p3);
        double expectedArea = 368.66218945;

        assertEquals(0, (expectedArea - quad.area()) / expectedArea, 1e-8);
    }

    @Test
    void centroid() {
        Point p0 = new Point(-33.96370350, 21.50426152, -12.73844588);
        Point p1 = new Point(18.80281320, 29.97229549, -13.27749442);
        Point p2 = new Point(-19.85934057, 33.31053014, -17.29041133);
        Point p3 = new Point(-26.17979609, 39.69737547, -20.64449113);

        Quad quad = new Quad(p0, p1, p2, p3);
        Point expectedCentroid = new Point(-15.36906678, 29.06135016, -15.04070421);

        assertEquals(0, expectedCentroid.distance(quad.centroid()), 1e-8);
    }

    @Test
    void unitNormal() {
        Point p0 = new Point(-33.96370350, 21.50426152, -12.73844588);
        Point p1 = new Point(18.80281320, 29.97229549, -13.27749442);
        Point p2 = new Point(-19.85934057, 33.31053014, -17.29041133);
        Point p3 = new Point(-26.17979609, 39.69737547, -20.64449113);

        Quad quad = new Quad(p0, p1, p2, p3);
        Vector expectedUnitNormal = new Vector(-0.57923642, 4.18629964, 9.06307787).unit();
        Vector actualUnitNormal = quad.unitNormal();

        Point expectedNormalCoordinates = new Point(expectedUnitNormal.x, expectedUnitNormal.y, expectedUnitNormal.z);
        Point actualNormalCoordinates = new Point(actualUnitNormal.x, actualUnitNormal.y, actualUnitNormal.z);

        assertEquals(0, expectedNormalCoordinates.distance(actualNormalCoordinates), 1e-8);
    }
}