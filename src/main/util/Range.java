package main.util;

public record Range(double min, double max) {
    public Range {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
    }
}
