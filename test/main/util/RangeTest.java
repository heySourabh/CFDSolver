package main.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RangeTest {
    @Test
    public void constructor_test() {
        Range range = new Range(-1, 1);
        assertEquals(-1, range.min(), 1e-15);
        assertEquals(1, range.max(), 1e-15);

        range = new Range(1, 5);
        assertEquals(1, range.min(), 1e-15);
        assertEquals(5, range.max(), 1e-15);

        range = new Range(-5, -1);
        assertEquals(-5, range.min(), 1e-15);
        assertEquals(-1, range.max(), 1e-15);
    }

    @Test
    public void test_reversed_arguments() {
        Range range = new Range(1, -1);
        assertEquals(-1, range.min(), 1e-15);
        assertEquals(1, range.max(), 1e-15);

        range = new Range(5, 1);
        assertEquals(1, range.min(), 1e-15);
        assertEquals(5, range.max(), 1e-15);

        range = new Range(-1, -5);
        assertEquals(-5, range.min(), 1e-15);
        assertEquals(-1, range.max(), 1e-15);
    }
}