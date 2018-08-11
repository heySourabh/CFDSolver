package main;

import main.geom.Vector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class TestHelper {
    public static void assertThrows(Class<? extends Throwable> ex, RunnableWithException code) {
        try {
            code.run();
            fail("Expecting an exception to be thrown, but no exception is thrown.");
        } catch (Exception e) {
            if (e.getClass() != ex) {
                fail("Exception: " + e.toString() + "\n" +
                        "Expected \"" + ex.toString() + "\", but received \"" + e.getClass().toString());
            }
        }
    }

    public static void assertVectorEquals(Vector expected, Vector actual, double tolerance) {
        assertArrayEquals(new double[]{expected.x, expected.y, expected.z},
                new double[]{actual.x, actual.y, actual.z}, tolerance);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
