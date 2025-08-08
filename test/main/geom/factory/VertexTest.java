package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static main.util.TestHelper.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class VertexTest {

    private final int SEED = 1;
    private final int BOUND = 200;

    @Test
    public void points() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);

        assertSame(p, vertex.points()[0]);
    }

    @Test
    public void vtkType() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);

        assertEquals(VTKType.VTK_VERTEX, vertex.vtkType());
    }

    @Test
    public void length() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);
        assertThrows(ArithmeticException.class, vertex::length);
    }

    @Test
    public void area() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);
        assertThrows(ArithmeticException.class, vertex::area);
    }

    @Test
    public void volume() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);
        assertThrows(ArithmeticException.class, vertex::volume);
    }

    @Test
    public void centroid() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);

        assertEquals(0, p.distance(vertex.centroid()), 1e-8);
    }

    @Test
    public void unitNormal() {
        Random r = new Random(SEED);
        int range = r.nextInt(BOUND) - BOUND / 2;
        Point p = new Point(
                r.nextDouble() * range,
                r.nextDouble() * range,
                r.nextDouble() * range);
        Vertex vertex = new Vertex(p);
        assertThrows(ArithmeticException.class, vertex::unitNormal);
    }
}