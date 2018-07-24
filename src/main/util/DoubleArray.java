package main.util;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class DoubleArray {
    public static double[] add(double[] a1, double[] a2) {
        double[] result = new double[a1.length];

        for (int i = 0; i < a1.length; i++) {
            result[i] = a1[i] + a2[i];
        }

        return result;
    }

    public static void increment(double[] array, double[] increment) {
        for (int i = 0; i < array.length; i++) {
            array[i] += increment[i];
        }
    }

    /**
     * a1 - a2
     *
     * @param a1 array
     * @param a2 array
     * @return new array (a1 - a2)
     */
    public static double[] subtract(double[] a1, double[] a2) {
        double[] result = new double[a1.length];

        for (int i = 0; i < a1.length; i++) {
            result[i] = a1[i] - a2[i];
        }

        return result;
    }

    public static void decrement(double[] array, double[] increment) {
        for (int i = 0; i < array.length; i++) {
            array[i] -= increment[i];
        }
    }

    public static double[] multiply(double[] a, double s) {
        double[] result = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * s;
        }

        return result;
    }

    public static double[] abs(double[] array) {
        return apply(array, Math::abs);
    }

    public static double[] sqr(double[] array) {
        return apply(array, e -> e * e);
    }

    public static double[] apply(double[] a1, double[] a2, DoubleBinaryOperator function) {
        double[] result = new double[a1.length];

        for (int i = 0; i < a1.length; i++) {
            result[i] = function.applyAsDouble(a1[i], a2[i]);
        }

        return result;
    }

    public static double[] apply(double[] array, DoubleUnaryOperator function) {
        double[] newArray = new double[array.length];

        for (int i = 0; i < array.length; i++) {
            newArray[i] = function.applyAsDouble(array[i]);
        }

        return newArray;
    }

    public static void copy(double[] from, double[] to) throws ArrayIndexOutOfBoundsException {
        System.arraycopy(from, 0, to, 0, from.length);
    }

    public static double[] copyOf(double[] array) {
        double[] newArray = new double[array.length];
        copy(array, newArray);

        return newArray;
    }

    public static double[] zeros(int length) {
        return new double[length];
    }

    public static double[] ones(int length) {
        double[] ones = new double[length];

        Arrays.fill(ones, 1.0);

        return ones;
    }

    public static double[] random(int length) {
        return random(length, new Random());
    }

    public static double[] random(int length, Random generator) {
        double[] rDoubles = new double[length];

        for (int i = 0; i < length; i++) {
            rDoubles[i] = generator.nextDouble();
        }

        return rDoubles;
    }

    public static double[] newFilledArray(int length, double value) {
        double[] array = new double[length];

        Arrays.fill(array, value);

        return array;
    }
}
