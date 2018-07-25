package main.geom;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class PointTest {

    @Test
    public void toVector() {
        Random rnd = new Random(124);
        double x = rnd.nextDouble() * rnd.nextInt();
        double y = rnd.nextDouble() * rnd.nextInt();
        double z = rnd.nextDouble() * rnd.nextInt();

        Point p = new Point(x, y, z);
        Vector expectedVector = new Vector(x, y, z);
        Vector actualVector = p.toVector();

        assertEquals(0.0, expectedVector.sub(actualVector).mag(), 1e-15);
    }

    @Test
    public void distance() {
        Point p1 = new Point(1, 2, 4);
        Point p2 = new Point(8, -5, 3);

        double expectedDistance = 9.949874371; // Calculated using hand-held calculator

        assertEquals(expectedDistance, p1.distance(p2), 1e-8);
        assertEquals(expectedDistance, p2.distance(p1), 1e-8);
    }

    @Test
    public void toStr() {
        Random rnd = new Random(124);
        double x = rnd.nextDouble() * rnd.nextInt();
        double y = rnd.nextDouble() * rnd.nextInt();
        double z = rnd.nextDouble() * rnd.nextInt();

        Point p = new Point(x, y, z);

        String expectedString = "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';

        assertEquals(expectedString, p.toString());
    }
}