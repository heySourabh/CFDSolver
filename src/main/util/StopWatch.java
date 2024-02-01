package main.util;

import java.time.Duration;

public class StopWatch {
    private long startMillis;
    private long pauseMillis;
    private boolean paused;

    private StopWatch(long startMillis) {
        this.startMillis = startMillis;
        this.paused = false;
    }

    public static StopWatch start() {
        return new StopWatch(System.currentTimeMillis());
    }

    public void pauseWatch() {
        this.paused = true;
        this.pauseMillis = System.currentTimeMillis();
    }

    public void continueWatch() {
        if (this.paused) {
            long currentMillis = System.currentTimeMillis();
            this.startMillis += currentMillis - this.pauseMillis;
            this.paused = false;
        }
    }

    public Duration stop() {
        if (this.paused) {
            return Duration.ofMillis(this.pauseMillis - this.startMillis);
        } else {
            return Duration.ofMillis(System.currentTimeMillis() - this.startMillis);
        }
    }

    public static Duration timeIt(Runnable process) {
        StopWatch stopWatch = StopWatch.start();
        process.run();
        return stopWatch.stop();
    }
}
