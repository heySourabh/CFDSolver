package main.util;

public class Util {
    public static double clip(double value, double minClip, double maxClip) {
        return value < minClip ? minClip
                : value > maxClip ? maxClip
                : value;
    }
}
