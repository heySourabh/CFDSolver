package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.util.TestHelper;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LeastSquareLinearInterpolatorTest {

    @Test
    public void interpolate() {
        Point[] points = {
                new Point(7, 8, 9),
                new Point(3, 8, 2),
                new Point(10, 2, 5),
                new Point(5, 8, 0)
        };

        LeastSquareLinearInterpolator interpolator = new LeastSquareLinearInterpolator(points);

        Point p0 = new Point(7, 8, 9);
        double u0 = 45.0;
        Vector expectedGradient = new Vector(2, 8, 6);
        double[] values = Arrays.stream(points)
                .mapToDouble(p -> new Vector(p0, p).dot(expectedGradient) + u0)
                .toArray();

        LinearFunction linearFunction = interpolator.interpolate(values);
        assertEquals(linearFunction.valueAt(points[1]), 45 + (3 - 7) * 2 + (2 - 9) * 6, 1e-12);
        TestHelper.assertVectorEquals(expectedGradient, linearFunction.gradient(), 1e-12);
    }
}