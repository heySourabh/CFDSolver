package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolyLineTest {

    @Test
    void vtkType() {
        PolyLine polyLine = new PolyLine(new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7)});
        VTKType expectedVtkType = VTKType.VTK_POLY_LINE;

        assertEquals(expectedVtkType, polyLine.vtkType());
    }

    @Test
    void length() {
        PolyLine polyLine = new PolyLine(new Point[]{
                new Point(1, 2, 3),
                new Point(4, 5, 6),
                new Point(3, 2, 7),
                new Point(-10, 6, -12)});
        // Calculated using hand-held calculator
        double expectedLength = 5.196152423 + 3.31662479 + 23.36664289;

        assertEquals(expectedLength, polyLine.length(), 1e-8);
    }
}