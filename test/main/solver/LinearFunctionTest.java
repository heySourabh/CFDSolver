package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.util.TestHelper;
import org.junit.Test;

import static org.junit.Assert.*;

public class LinearFunctionTest {

    @Test
    public void gradient_returns_the_saved_gradient_vector() {
        Point point = new Point(58, 7, -8);
        double value = 2;
        Vector gradient = new Vector(12, 87, 36);
        LinearFunction function = new LinearFunction(point, value, gradient);

        TestHelper.assertVectorEquals(gradient, function.gradient(), 1e-20);
    }

    @Test
    public void valueAt_uses_the_gradient_and_value_parameter_to_calculate_interpolated_value() {
        Point point = new Point(58, 7, -8);
        double value = 2;
        Vector gradient = new Vector(12, 87, 36);
        LinearFunction function = new LinearFunction(point, value, gradient);

        Point valueAt = new Point(25, 7, 3);
        double expectedValue = 2 + 12 * (25 - 58) + 87 * (7 - 7.) + 36 * (3 - (-8));

        assertEquals(expectedValue, function.valueAt(valueAt), 1e-15);
    }
}