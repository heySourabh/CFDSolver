package main.util;

import org.junit.Test;

import static main.util.TestHelper.assertThrows;
import static org.junit.Assert.*;

public class DoubleArrayTest {

    @Test
    public void multiply() {
        double[] a1 = {8.0, 3.0, 5.6};
        double[] a2 = {2.0, 1.0, 2.8};

        double[] expected = {16, 3, 15.68};
        assertArrayEquals(expected, DoubleArray.multiply(a1, a2), 1e-12);
    }

    @Test
    public void divide_element_wise() {
        double[] a1 = {8.0, 3.0, 5.6};
        double[] a2 = {2.0, 1.0, 2.8};

        double[] expected = {4, 3, 2};

        assertArrayEquals(expected, DoubleArray.divide(a1, a2), 1e-15);

        double[] arrayWithZero = {1, 0, 5};
        assertThrows(ArithmeticException.class, () -> DoubleArray.divide(a1, arrayWithZero));
    }

    @Test
    public void divide_scalar() {
        double[] array = {8.0, 3.0, 5.6};
        double denominator = 2.0;

        double[] expected = {4, 1.5, 2.8};

        assertArrayEquals(expected, DoubleArray.divide(array, denominator), 1e-15);

        double zero = 0.0;
        assertThrows(ArithmeticException.class, () -> DoubleArray.divide(array, zero));
    }
}