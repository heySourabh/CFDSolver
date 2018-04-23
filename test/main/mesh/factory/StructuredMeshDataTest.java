package main.mesh.factory;

import static org.junit.jupiter.api.Assertions.*;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.factory.Line;
import main.geom.factory.Quad;
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
    private static StructuredMeshData meshData2d;
    //private static StructuredMeshData meshData3d;

    private final static Point[][] points2d = new Point[5][7];

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        System.out.println("Reading files.");
        meshData1d = new StructuredMeshData(new File("test/test_data/mesh_structured_1d.dat"));

        double x_o = 1;                     // origin x
        double y_o = -2;                    // origin y
        double r_i = 5;                     // inside radius
        double r_o = 8;                     // outside radius
        double a_s = 30 * Math.PI / 180.0;  // start angle
        double a_e = 115 * Math.PI / 180.0; // end angle

        int xi2d = points2d.length;
        int eta2d = points2d[0].length;
        double da = (a_e - a_s) / (xi2d - 1.0);
        double dr = (r_o - r_i) / (eta2d - 1.0);
        for (int i = 0; i < xi2d; i++) {
            double a = a_s + i * da;
            for (int j = 0; j < eta2d; j++) {
                double r = r_i + j * dr;
                double x = r * Math.cos(a) + x_o;
                double y = r * Math.sin(a) + y_o;
                double z = 1.0;

                points2d[i][j] = new Point(x, y, z);
                // System.out.printf("%-25.20f %-25.20f %-25.20f\n", x, y, z);
            }
        }
        meshData2d = new StructuredMeshData(new File("test/test_data/mesh_structured_2d.dat"));
        //meshData3d = new StructuredMeshData(new File("test/test_data/mesh_structured_3d.dat"));
    }

    @Test
    void dims1d() {
        assertEquals(Dimension.ONE_DIM, meshData1d.dims());
    }

    @Test
    void dims2d() {
        assertEquals(Dimension.TWO_DIM, meshData2d.dims());
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
    void points2d() {
        List<Point> expectedPoints = new ArrayList<>();
        for (int i = 0; i < points2d.length; i++) {
            for (int j = 0; j < points2d[0].length; j++) {
                expectedPoints.add(points2d[i][j]);
            }
        }

        assertEquals(expectedPoints, meshData2d.points());
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
    void cellGeom2d() {
        List<Geometry> cellGeom = new ArrayList<>();
        for (int i = 0; i < points2d.length - 1; i++) {
            for (int j = 0; j < points2d[0].length - 1; j++) {
                cellGeom.add(new Quad(points2d[i][j], points2d[i + 1][j], points2d[i + 1][j + 1], points2d[i][j + 1]));
            }
        }

        assertEquals(cellGeom, meshData2d.cellGeom());
    }

    @Test
    void boundaryNames1d() {
        String[] boundaryNames = {"xi min", "xi max"};

        assertArrayEquals(boundaryNames, meshData1d.boundaryNames());
    }

    @Test
    void boundaryNames2d() {
        String[] boundaryNames = {"xi min", "xi max", "eta min", "eta max"};

        assertArrayEquals(boundaryNames, meshData2d.boundaryNames());
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

    @Test
    void boundaryFaces2d() {
        List<List<Geometry>> boundaryFaces = new ArrayList<>();
        int i, j;

        List<Geometry> xi_min_faces = new ArrayList<>();
        i = 0;
        for (j = 0; j < points2d[0].length - 1; j++) {
            xi_min_faces.add(new Line(points2d[i][j], points2d[i][j + 1]));
        }

        List<Geometry> xi_max_faces = new ArrayList<>();
        i = points2d.length - 1;
        for (j = 0; j < points2d[0].length - 1; j++) {
            xi_max_faces.add(new Line(points2d[i][j], points2d[i][j + 1]));
        }

        List<Geometry> eta_min_faces = new ArrayList<>();
        j = 0;
        for (i = 0; i < points2d.length - 1; i++) {
            eta_min_faces.add(new Line(points2d[i][j], points2d[i + 1][j]));
        }

        List<Geometry> eta_max_faces = new ArrayList<>();
        j = points2d[0].length - 1;
        for (i = 0; i < points2d.length - 1; i++) {
            eta_max_faces.add(new Line(points2d[i][j], points2d[i + 1][j]));
        }

        boundaryFaces.add(xi_min_faces);
        boundaryFaces.add(xi_max_faces);
        boundaryFaces.add(eta_min_faces);
        boundaryFaces.add(eta_max_faces);

        assertEquals(boundaryFaces, meshData2d.boundaryFaces());
    }
}
