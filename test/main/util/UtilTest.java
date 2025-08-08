package main.util;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    @Test
    public void clipping_greater_value_returns_max_allowed_value() {
        double minAllowed = 1.0;
        double maxAllowed = 2.0;

        assertEquals(maxAllowed, Util.clip(2.5, minAllowed, maxAllowed), 1e-15);
    }

    @Test
    public void clipping_smaller_value_returns_min_allowed_value() {
        double minAllowed = -0.5;
        double maxAllowed = 1.0;

        assertEquals(minAllowed, Util.clip(-1.0, minAllowed, maxAllowed), 1e-15);
    }

    @Test
    public void clipping_allowed_value_just_returns_it() {
        double minAllowed = -0.5;
        double maxAllowed = 1.0;
        double value = 0.0;

        assertEquals(value, Util.clip(value, minAllowed, maxAllowed), 1e-15);
    }

    @Test
    public void clipping_at_infinities_just_returns_value() {
        double minAllowed = Double.NEGATIVE_INFINITY;
        double maxAllowed = Double.POSITIVE_INFINITY;
        Random rand = new Random(1245);
        double value = rand.nextDouble() * rand.nextInt();

        assertEquals(value, Util.clip(value, minAllowed, maxAllowed), 1e-15);
    }
}