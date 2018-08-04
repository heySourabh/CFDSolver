package main.util;

import main.TestHelper;
import org.junit.Test;

import static main.TestHelper.assertThrows;
import static org.junit.Assert.*;

public class DoubleArrayTest {

    @Test
    public void divide() {
        double[] a1 = {8.0, 3.0, 5.6};
        double[] a2 = {2.0, 1.0, 2.8};

        double[] expected = {4, 3, 2};

        assertArrayEquals(expected, DoubleArray.divide(a1, a2), 1e-15);

        double[] divByZero = {1, 0, 5};
        assertThrows(ArithmeticException.class, () -> DoubleArray.divide(a1, divByZero));
    }
}