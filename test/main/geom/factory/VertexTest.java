package main.geom.factory;

import main.geom.Point;
import main.geom.VTKType;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

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
}