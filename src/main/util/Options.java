package main.util;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.String.valueOf;

/**
 * A simple utility for reading command line arguments.
 */
public class Options {
    private final String[] args;

    public Options(String[] args) {
        this.args = args;
    }

    public String get(String arg) {
        String argName = (arg.startsWith("--") ? arg : ("--" + arg)).trim();
        List<String> option = Arrays.stream(args)
                .dropWhile(a -> !a.equals(argName))
                .limit(2)
                .toList();
        if (option.size() != 2) {
            throw new NoSuchElementException("Cannot find argument named: " + argName);
        }

        return option.get(1);
    }

    public String getOrDefault(String argName, String defaultValue) {
        try {
            return get(argName);
        } catch (NoSuchElementException ex) {
            return defaultValue;
        }
    }

    private int parseInt(String argName, String argValue) {
        try {
            return Integer.parseInt(argValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to parse value '" + argValue + "' as integer for the argument named '" + argName + "'");
        }
    }

    public int getInt(String argName) {
        return parseInt(argName, get(argName));
    }

    public int getIntOrDefault(String argName, int defaultValue) {
        return parseInt(argName, getOrDefault(argName, valueOf(defaultValue)));
    }

    private double parseDouble(String argName, String argValue) {
        try {
            return Double.parseDouble(argValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to parse value '" + argValue + "' as double for the argument named '" + argName + "'");
        }
    }

    public double getDouble(String argName) {
        return parseDouble(argName, get(argName));
    }

    public double getDoubleOrDefault(String argName, double defaultValue) {
        return parseDouble(argName, getOrDefault(argName, valueOf(defaultValue)));
    }
}
