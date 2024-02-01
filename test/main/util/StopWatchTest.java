package main.util;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertEquals;

public class StopWatchTest {
    @Test
    public void test_stopwatch() {
        StopWatch stopWatch = StopWatch.start();
        simulateRunningProcessForMillis(100);
        assertEquals(stopWatch.stop().toMillis() - 100, 0, 5);
    }

    @Test
    public void test_pause() {
        StopWatch stopWatch = StopWatch.start();
        simulateRunningProcessForMillis(100);
        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(50); // does not matter as the watch is paused
        assertEquals(stopWatch.stop().toMillis() - 100, 0, 5);
    }

    @Test
    public void test_pause_continue() {
        StopWatch stopWatch = StopWatch.start();
        simulateRunningProcessForMillis(100); // run
        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(50); // pause for this much time
        stopWatch.continueWatch();
        simulateRunningProcessForMillis(25); // again run for this much time
        assertEquals(stopWatch.stop().toMillis() - (100 + 25), 0, 5);
    }

    @Test
    public void test_pause_continue_pause() {
        StopWatch stopWatch = StopWatch.start();
        simulateRunningProcessForMillis(100); // run

        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(50); // pause for this much time

        stopWatch.continueWatch();
        simulateRunningProcessForMillis(25); // again run for this much time

        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(75); // pause

        assertEquals(stopWatch.stop().toMillis() - (100 + 25), 0, 5);
    }

    @Test
    public void test_pause_continue_pause_continue() {
        StopWatch stopWatch = StopWatch.start();
        simulateRunningProcessForMillis(100); // run

        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(50); // pause for this much time

        stopWatch.continueWatch();
        simulateRunningProcessForMillis(25); // again run for this much time

        stopWatch.pauseWatch();
        simulateRunningProcessForMillis(75); // pause

        stopWatch.continueWatch();
        simulateRunningProcessForMillis(111); // again run for this much time

        assertEquals(stopWatch.stop().toMillis() - (100 + 25 + 111), 0, 5);
    }

    @Test
    public void test_process_duration() {
        Runnable process = () -> simulateRunningProcessForMillis(124);
        assertEquals(StopWatch.timeIt(process).toMillis() - 124, 0, 5);
    }

    private static void simulateRunningProcessForMillis(long millis) {
        LockSupport.parkNanos(Duration.ofMillis(millis).toNanos());
    }
}