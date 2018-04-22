package main.mesh.factory;

import static org.junit.jupiter.api.Assertions.*;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.factory.Line;
import main.geom.factory.Vertex;
import main.mesh.Dimension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class StructuredMeshDataTest {

    private static StructuredMeshData meshData1d;
    static StructuredMeshData meshData2d;
    static StructuredMeshData meshData3d;

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        System.out.println("Reading files.");
        meshData1d = new StructuredMeshData(new File("test/test_data/mesh_structured_1d.dat"));
        //meshData2d = new StructuredMeshData(new File("test/test_data/mesh_structured_2d.dat"));
        //meshData3d = new StructuredMeshData(new File("test/test_data/mesh_structured_3d.dat"));
    }

    @Test
    void dims1d() {
        assertEquals(Dimension.ONE_DIM, meshData1d.dims());
    }

    @Test
    void points1d() {
        List<Point> expectedPoints = Arrays.asList(
                new Point(-2.0, 0.0, 0),
                new Point(-0.75, 0.25, 0),
                new Point(0.5, 0.5, 0),
                new Point(1.75, 0.75, 0),
                new Point(3.0, 1.0, 0));

        assertEquals(expectedPoints, meshData1d.points());
    }

    @Test
    void cellGeom1d() {
        ArrayList<Point> points = meshData1d.points();
        Line l0 = new Line(points.get(0), points.get(1));
        Line l1 = new Line(points.get(1), points.get(2));
        Line l2 = new Line(points.get(2), points.get(3));
        Line l3 = new Line(points.get(3), points.get(4));
        List<Geometry> cellGeom = List.of(l0, l1, l2, l3);

        assertEquals(cellGeom, meshData1d.cellGeom());
    }

    @Test
    void boundaryNames1d() {
        String[] boundaryNames = {"xi min", "xi max"};

        assertArrayEquals(boundaryNames, meshData1d.boundaryNames());
    }

    @Test
    void boundaryFaces1d() {
        ArrayList<Point> points = meshData1d.points();
        Geometry xi_min = new Vertex(points.get(0));
        Geometry xi_max = new Vertex(points.get(points.size() - 1));
        List<List<Geometry>> boundaryFaces = new ArrayList<>();
        List<Geometry> xi_min_faces = List.of(xi_min);
        List<Geometry> xi_max_faces = List.of(xi_max);
        boundaryFaces.add(xi_min_faces);
        boundaryFaces.add(xi_max_faces);

        assertEquals(boundaryFaces, meshData1d.boundaryFaces());
    }
}
