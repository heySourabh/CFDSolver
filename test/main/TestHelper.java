package main;

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

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
