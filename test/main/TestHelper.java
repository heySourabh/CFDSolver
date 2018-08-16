package main;

import main.geom.Point;
import main.geom.Vector;

import static org.junit.Assert.assertEquals;
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
        assertEquals("x-component of vector", expected.x, actual.x, tolerance);
        assertEquals("y-component of vector", expected.y, actual.y, tolerance);
        assertEquals("z-component of vector", expected.z, actual.z, tolerance);
    }

    public static void assertPointEquals(Point expected, Point actual, double tolerance) {
        assertEquals("x-component of point", expected.x, actual.x, tolerance);
        assertEquals("y-component of point", expected.y, actual.y, tolerance);
        assertEquals("z-component of point", expected.z, actual.z, tolerance);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
