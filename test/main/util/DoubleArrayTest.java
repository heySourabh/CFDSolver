package main.util;

import org.junit.Test;

import java.util.Random;

import static main.util.TestHelper.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void norm2Sqr_of_an_array_with_zeros_is_zero() {
        double[] array = {0, 0, 0, 0};
        assertEquals(0, DoubleArray.norm2Sqr(array), 1e-15);
    }

    @Test
    public void norm2Sqr_of_an_array_with_ones_is_its_length() {
        double[] array = {1, 1, 1, -1, 1, 1, 1};
        assertEquals(array.length, DoubleArray.norm2Sqr(array), 1e-15);
    }

    @Test
    public void norm2Sqr_of_arbitrary_an_array() {
        double[] array = {-5, 6, 3.2, -7, -6};
        double expectedResult = (5 * 5) + (6 * 6) + (3.2 * 3.2) + (7 * 7) + (6 * 6);
        assertEquals(expectedResult, DoubleArray.norm2Sqr(array), 1e-15);
    }

    @Test
    public void norm2_of_an_array_with_zeros_is_zero() {
        double[] array = {0, 0, 0, 0};
        assertEquals(0, DoubleArray.norm2(array), 1e-15);
    }

    @Test
    public void norm2_of_an_array_with_ones_is_sqrt_of_its_length() {
        double[] array = {1, -1, 1, 1, -1, 1, 1};
        assertEquals(Math.sqrt(array.length), DoubleArray.norm2(array), 1e-15);
    }

    @Test
    public void norm2_of_arbitrary_an_array() {
        double[] array = {-5, 6, 3.2, -7, -6};
        double expectedResult = Math.sqrt((5 * 5) + (6 * 6) + (3.2 * 3.2) + (7 * 7) + (6 * 6));
        assertEquals(expectedResult, DoubleArray.norm2(array), 1e-15);
    }

    @Test
    public void sum_of_an_arbitrary_array() {
        double[] array = {-2, 6, 7, -9};
        double expectedResult = -2 + 6 + 7 - 9;
        assertEquals(expectedResult, DoubleArray.sum(array), 1e-15);
    }

    @Test
    public void random_tests() {
        int seed = 123;
        Random generator = new Random(seed);
        double[] random_array = {
                generator.nextDouble(),
                generator.nextDouble(),
                generator.nextDouble()
        };

        assertArrayEquals(random_array, DoubleArray.random(random_array.length, new Random(seed)), 1e-15);

        double min = -2000;
        double max = 120.5;

        generator = new Random(seed);
        random_array = new double[]{
                min + generator.nextDouble() * (max - min),
                min + generator.nextDouble() * (max - min),
                min + generator.nextDouble() * (max - min),
                min + generator.nextDouble() * (max - min)
        };
        assertArrayEquals(random_array,
                DoubleArray.random(random_array.length, new Random(seed), min, max),
                1e-15);
    }

    @Test
    public void dot_product_of_two_vectors() {
        double[] v1 = {1, 2, -5, 6};
        double[] v2 = {5, 5, 5, -5};

        double dotProduct = 1. * 5 + 2 * 5 + (-5) * 5 + 6 * (-5);

        assertEquals(dotProduct, DoubleArray.dot(v1, v2), 1e-15);
    }

    @Test
    public void test_sqr() {
        double[] v = {5, 10, 1, 3, 2, 1.2};
        assertArrayEquals(new double[]{25, 100, 1, 9, 4, 1.44}, DoubleArray.sqr(v), 1e-15);
    }

    @Test
    public void test_sqrt() {
        double[] v = {25, 100, 9, 4};
        assertArrayEquals(new double[]{5, 10, 3, 2}, DoubleArray.sqrt(v), 1e-15);
    }
}
