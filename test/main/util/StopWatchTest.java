package main.util;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;

public class StopWatchTest {
    @Test
    public void test_stopwatch() {
        StopWatch stopWatch = StopWatch.start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        assertEquals(stopWatch.stop().toMillis() - 100, 0, 5);
    }

    @Test
    public void test_pause() {
        StopWatch stopWatch = StopWatch.start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(50).toNanos()); // does not matter as the watch is paused
        assertEquals(stopWatch.stop().toMillis() - 100, 0, 5);
    }

    @Test
    public void test_pause_continue() {
        StopWatch stopWatch = StopWatch.start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos()); // run
        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(50).toNanos()); // pause for this much time
        stopWatch.continueWatch();
        LockSupport.parkNanos(Duration.ofMillis(25).toNanos()); // again run for this much time
        assertEquals(stopWatch.stop().toMillis() - (100 + 25), 0, 5);
    }

    @Test
    public void test_pause_continue_pause() {
        StopWatch stopWatch = StopWatch.start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos()); // run

        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(50).toNanos()); // pause for this much time

        stopWatch.continueWatch();
        LockSupport.parkNanos(Duration.ofMillis(25).toNanos()); // again run for this much time

        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(75).toNanos()); // pause

        assertEquals(stopWatch.stop().toMillis() - (100 + 25), 0, 5);
    }

    @Test
    public void test_pause_continue_pause_continue() {
        StopWatch stopWatch = StopWatch.start();
        LockSupport.parkNanos(Duration.ofMillis(100).toNanos()); // run

        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(50).toNanos()); // pause for this much time

        stopWatch.continueWatch();
        LockSupport.parkNanos(Duration.ofMillis(25).toNanos()); // again run for this much time

        stopWatch.pauseWatch();
        LockSupport.parkNanos(Duration.ofMillis(75).toNanos()); // pause

        stopWatch.continueWatch();
        LockSupport.parkNanos(Duration.ofMillis(111).toNanos()); // again run for this much time

        assertEquals(stopWatch.stop().toMillis() - (100 + 25 + 111), 0, 5);
    }
}