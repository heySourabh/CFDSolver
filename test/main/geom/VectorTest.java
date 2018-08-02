package main.geom;

import org.junit.Test;

import java.util.Random;

import static main.TestHelper.assertThrows;
import static org.junit.Assert.assertEquals;

public class VectorTest {

    @Test
    public void toPoint() {
        Random rnd = new Random(546);
        double x = rnd.nextDouble() * rnd.nextInt();
        double y = rnd.nextDouble() * rnd.nextInt();
        double z = rnd.nextDouble() * rnd.nextInt();

        Vector v = new Vector(x, y, z);
        Point expectedPoint = new Point(x, y, z);
        Point actualPoint = v.toPoint();

        assertEquals(0, expectedPoint.distance(actualPoint), 1e-15);
    }

    @Test
    public void add() {
        Random rnd = new Random(546);
        double x1 = rnd.nextDouble() * rnd.nextInt();
        double y1 = rnd.nextDouble() * rnd.nextInt();
        double z1 = rnd.nextDouble() * rnd.nextInt();
        double x2 = rnd.nextDouble() * rnd.nextInt();
        double y2 = rnd.nextDouble() * rnd.nextInt();
        double z2 = rnd.nextDouble() * rnd.nextInt();
        Vector v1 = new Vector(x1, y1, z1);
        Vector v2 = new Vector(x2, y2, z2);

        Vector actualVector = v1.add(v2);

        assertEquals(x1 + x2, actualVector.x, 1e-15);
        assertEquals(y1 + y2, actualVector.y, 1e-15);
        assertEquals(z1 + z2, actualVector.z, 1e-15);
    }

    @Test
    public void sub() {
        Random rnd = new Random(546);
        double x1 = rnd.nextDouble() * rnd.nextInt();
        double y1 = rnd.nextDouble() * rnd.nextInt();
        double z1 = rnd.nextDouble() * rnd.nextInt();
        double x2 = rnd.nextDouble() * rnd.nextInt();
        double y2 = rnd.nextDouble() * rnd.nextInt();
        double z2 = rnd.nextDouble() * rnd.nextInt();
        Vector v1 = new Vector(x1, y1, z1);
        Vector v2 = new Vector(x2, y2, z2);

        Vector actualVector = v1.sub(v2);

        assertEquals(x1 - x2, actualVector.x, 1e-15);
        assertEquals(y1 - y2, actualVector.y, 1e-15);
        assertEquals(z1 - z2, actualVector.z, 1e-15);
    }

    @Test
    public void mult() {
        Random rnd = new Random(546);
        double x = rnd.nextDouble() * rnd.nextInt();
        double y = rnd.nextDouble() * rnd.nextInt();
        double z = rnd.nextDouble() * rnd.nextInt();
        Vector v = new Vector(x, y, z);

        double scalar = rnd.nextDouble() * rnd.nextInt();

        Vector actualVector = v.mult(scalar);

        assertEquals(x * scalar, actualVector.x, 1e-15);
        assertEquals(y * scalar, actualVector.y, 1e-15);
        assertEquals(z * scalar, actualVector.z, 1e-15);
    }

    @Test
    public void magSqr() {
        double x = -124;
        double y = 24;
        double z = 45;
        Vector v = new Vector(x, y, z);
        double actualValue = v.magSqr();
        double expectedValue = 17977; // Calculated using hand-held calculator

        assertEquals(expectedValue, actualValue, 1e-15);
    }

    @Test
    public void mag() {
        double x = -124;
        double y = 24;
        double z = 45;
        Vector v = new Vector(x, y, z);
        double actualValue = v.mag();
        double expectedValue = 134.0783353; // Calculated using hand-held calculator

        assertEquals(expectedValue, actualValue, 1e-6);
    }

    @Test
    public void unit() {
        double x = -124;
        double y = 24;
        double z = 45;
        Vector v = new Vector(x, y, z);

        Vector actualUnitVector = v.unit();

        // Values calculated using hand-held calculator
        assertEquals(-0.924832484, actualUnitVector.x, 1e-8);
        assertEquals(0.178999835, actualUnitVector.y, 1e-8);
        assertEquals(0.335624692, actualUnitVector.z, 1e-8);

        assertThrows(ArithmeticException.class, () -> new Vector(0, 0, 0).unit());
    }

    @Test
    public void dot() {
        Random rnd = new Random(546);
        double x1 = rnd.nextDouble() * rnd.nextInt();
        double y1 = rnd.nextDouble() * rnd.nextInt();
        double z1 = rnd.nextDouble() * rnd.nextInt();
        double x2 = rnd.nextDouble() * rnd.nextInt();
        double y2 = rnd.nextDouble() * rnd.nextInt();
        double z2 = rnd.nextDouble() * rnd.nextInt();
        Vector v1 = new Vector(x1, y1, z1);
        Vector v2 = new Vector(x2, y2, z2);

        double dotProduct = v1.dot(v2);

        assertEquals(x1 * x2 + y1 * y2 + z1 * z2, dotProduct, 1e-15);
    }

    @Test
    public void cross() {
        Random rnd = new Random(546);
        double x1 = rnd.nextDouble() * rnd.nextInt();
        double y1 = rnd.nextDouble() * rnd.nextInt();
        double z1 = rnd.nextDouble() * rnd.nextInt();
        double x2 = rnd.nextDouble() * rnd.nextInt();
        double y2 = rnd.nextDouble() * rnd.nextInt();
        double z2 = rnd.nextDouble() * rnd.nextInt();
        Vector v1 = new Vector(x1, y1, z1);
        Vector v2 = new Vector(x2, y2, z2);

        assertEquals(0, (v1.cross(v2)).add(v2.cross(v1)).mag(), 1e-15);

        Vector actualVector = v1.cross(v2);

        assertEquals(y1 * z2 - y2 * z1, actualVector.x, 1e-15);
        assertEquals(x2 * z1 - x1 * z2, actualVector.y, 1e-15);
        assertEquals(x1 * y2 - x2 * y1, actualVector.z, 1e-15);
    }

    @Test
    public void toStr() {
        Random rnd = new Random(546);
        double x = rnd.nextDouble() * rnd.nextInt();
        double y = rnd.nextDouble() * rnd.nextInt();
        double z = rnd.nextDouble() * rnd.nextInt();
        Vector v = new Vector(x, y, z);
        String expectedString = "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';

        assertEquals(expectedString, v.toString());
    }
}