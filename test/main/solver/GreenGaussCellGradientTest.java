package main.solver;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.*;
import main.mesh.*;
import main.mesh.factory.Structured1DMesh;
import main.mesh.factory.Structured2DMesh;
import main.mesh.factory.Structured3DMesh;
import main.util.DoubleArray;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static main.util.TestHelper.assertPointEquals;
import static main.util.TestHelper.assertVectorEquals;
import static org.junit.Assert.assertEquals;

public class GreenGaussCellGradientTest {

    private Vector unitNormal(Line line, Vector up) {
        Vector lineVec = new Vector(line.points()[0], line.points()[1]);
        return lineVec.cross(up).unit();
    }

    @Test
    public void oneDim_x_single_cell() {
        int numVars = 2;
        Node n1 = new Node(0.5, 0.0, 0.0, numVars);
        Node n2 = new Node(2.8, 0.0, 0.0, numVars);

        Line line = new Line(n1.location(), n2.location());
        Shape shape = new Shape(line.length(), line.centroid());
        Cell cell = new Cell(new Node[]{n1, n2}, VTKType.VTK_LINE, shape, numVars);
        cell.setIndex(0);

        Surface surface = new Surface(1.0, n1.location(), new Vector(-1, 0, 0));
        Face f1 = new Face(new Node[]{n1}, VTKType.VTK_VERTEX, surface, cell, null, numVars);
        surface = new Surface(1.0, n2.location(), new Vector(1, 0, 0));
        Face f2 = new Face(new Node[]{n2}, VTKType.VTK_VERTEX, surface, cell, null, numVars);

        cell.faces.add(f1);
        cell.faces.add(f2);

        DoubleArray.copy(new double[]{0, 0}, f1.U);
        DoubleArray.copy(new double[]{1, 2}, f2.U);

        double dx = 2.8 - 0.5;
        Vector[] expectedGradients = new Vector[]{
                new Vector(1, 0, 0).mult(1.0 / dx),
                new Vector(2, 0, 0).mult(1.0 / dx)
        };

        Mesh mesh = createMesh(cell);
        CellGradientCalculator gradientCalculator = new GreenGaussCellGradient(mesh);
        gradientCalculator.setupAllCells();

        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
    }

    @Test
    public void oneDim_x_same_normal_dir() throws IOException {
        int numVars = 3;
        File tempFile = new File("test/test_data/temp_1d.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "-2.0      0        0\n" +
                    "-0.75     0        0\n" +
                    "0.5       0        0\n" +
                    "1.2       0        0\n"
            );
        }

        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete())
            System.out.println("Unable to delete temp file created while testing: " + tempFile.toString());

        Cell cell = mesh.cells().get(1);

        Face fl = cell.faces.get(0);
        Face fr = cell.faces.get(1);

        assertEquals(fl.nodes[0].x, -0.75, 1e-15);
        assertEquals(fr.nodes[0].x, 0.5, 1e-15);

        assertVectorEquals(new Vector(1, 0, 0), fl.surface.unitNormal(), 1e-12);
        assertVectorEquals(new Vector(1, 0, 0), fr.surface.unitNormal(), 1e-12);

        DoubleArray.copy(new double[]{3.0, 6.0, -9.0}, fl.U);
        DoubleArray.copy(new double[]{-4.0, 8.0, -8.0}, fr.U);

        double dx = 0.5 + 0.75;

        Vector[] expectedGradients = new Vector[]{
                new Vector((-4 - 3) / dx, 0, 0),
                new Vector((8 - 6) / dx, 0, 0),
                new Vector((-8 + 9) / dx, 0, 0)
        };

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void oneDim_y() throws IOException {
        int numVars = 3;
        File tempFile = new File("test/test_data/temp_1d.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "0     -2.0       0\n" +
                    "0     -0.75      0\n" +
                    "0     0.5        0\n" +
                    "0     1.2        0\n"
            );
        }

        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete())
            System.out.println("Unable to delete temp file created while testing: " + tempFile.toString());

        Cell cell = mesh.cells().get(1);

        Face fl = cell.faces.get(0);
        Face fr = cell.faces.get(1);

        assertEquals(fl.nodes[0].y, -0.75, 1e-15);
        assertEquals(fr.nodes[0].y, 0.5, 1e-15);

        assertVectorEquals(new Vector(0, 1, 0), fl.surface.unitNormal(), 1e-12);
        assertVectorEquals(new Vector(0, 1, 0), fr.surface.unitNormal(), 1e-12);

        DoubleArray.copy(new double[]{3.0, 6.0, -9.0}, fl.U);
        DoubleArray.copy(new double[]{-4.0, 8.0, -8.0}, fr.U);

        double dy = 0.5 + 0.75;

        Vector[] expectedGradients = new Vector[]{
                new Vector(0, (-4 - 3) / dy, 0),
                new Vector(0, (8 - 6) / dy, 0),
                new Vector(0, (-8 + 9) / dy, 0)
        };

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void oneDim_z() throws IOException {
        int numVars = 3;
        File tempFile = new File("test/test_data/temp_1d.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "0      0      -2.0 \n" +
                    "0      0      -0.75\n" +
                    "0      0      0.5  \n" +
                    "0      0      1.2  \n"
            );
        }

        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete())
            System.out.println("Unable to delete temp file created while testing: " + tempFile.toString());

        Cell cell = mesh.cells().get(1);

        Face fl = cell.faces.get(0);
        Face fr = cell.faces.get(1);

        assertEquals(fl.nodes[0].z, -0.75, 1e-15);
        assertEquals(fr.nodes[0].z, 0.5, 1e-15);

        assertVectorEquals(new Vector(0, 0, 1), fl.surface.unitNormal(), 1e-12);
        assertVectorEquals(new Vector(0, 0, 1), fr.surface.unitNormal(), 1e-12);

        DoubleArray.copy(new double[]{3.0, 6.0, -9.0}, fl.U);
        DoubleArray.copy(new double[]{-4.0, 8.0, -8.0}, fr.U);

        double dz = 0.5 + 0.75;

        Vector[] expectedGradients = new Vector[]{
                new Vector(0, 0, (-4 - 3) / dz),
                new Vector(0, 0, (8 - 6) / dz),
                new Vector(0, 0, (-8 + 9) / dz)
        };

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void oneDim_xyz() throws IOException {
        int numVars = 3;
        File tempFile = new File("test/test_data/temp_1d.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 1\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "-5      3      -2\n" +
                    "-4      1      1 \n" +
                    "-3      -1     4 \n" +
                    "-2      -3     7 \n"
            );
        }

        Mesh mesh = new Structured1DMesh(tempFile, numVars, null, null);
        if (!tempFile.delete())
            System.out.println("Unable to delete temp file created while testing: " + tempFile.toString());

        Cell cell = mesh.cells().get(1);

        Face fl = cell.faces.get(0);
        Face fr = cell.faces.get(1);

        assertEquals(fl.nodes[0].x, -4, 1e-15);
        assertEquals(fr.nodes[0].x, -3, 1e-15);
        assertEquals(fl.nodes[0].y, 1, 1e-15);
        assertEquals(fr.nodes[0].y, -1, 1e-15);
        assertEquals(fl.nodes[0].z, 1, 1e-15);
        assertEquals(fr.nodes[0].z, 4, 1e-15);

        Vector v1 = new Vector(new Point(-5, 3, -2), new Point(-4, 1, 1));
        Vector v2 = new Vector(new Point(-4, 1, 1), new Point(-3, -1, 4));
        Vector v3 = new Vector(new Point(-3, -1, 4), new Point(-2, -3, 7));

        assertVectorEquals(v1.add(v2).unit(), fl.surface.unitNormal(), 1e-12);
        assertVectorEquals(v2.add(v3).unit(), fr.surface.unitNormal(), 1e-12);

        DoubleArray.copy(new double[]{3.0, 6.0, -9.0}, fl.U);
        DoubleArray.copy(new double[]{-4.0, 8.0, -8.0}, fr.U);

        double dx = -3 + 4;
        double dy = -1 - 1;
        double dz = 4 - 1;

        double dr = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double wx = (dx * dx) / (dr * dr); // Look at my lab notebook for derivation of weights
        double wy = (dy * dy) / (dr * dr);
        double wz = (dz * dz) / (dr * dr);

        Vector[] expectedGradients = new Vector[]{
                new Vector((-4 - 3) / dx * wx, (-4 - 3) / dy * wy, (-4 - 3) / dz * wz),
                new Vector((8 - 6) / dx * wx, (8 - 6) / dy * wy, (8 - 6) / dz * wz),
                new Vector((-8 + 9) / dx * wx, (-8 + 9) / dy * wy, (-8 + 9) / dz * wz)
        };

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_xy_single_quad_cell() {
        int numVars = 3;
        Point p0 = new Point(0, 0, 0);
        Point p1 = new Point(1, 0, 0);
        Point p2 = new Point(1, 1, 0);
        Point p3 = new Point(0, 1, 0);
        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);
        Node n3 = new Node(p3, numVars);

        Quad quad = new Quad(p0, p1, p2, p3);
        Shape shape = new Shape(quad.area() * 1.0, quad.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2, n3}, quad.vtkType(), shape, numVars);
        cell.setIndex(0);

        Line line = new Line(p0, p1);
        Surface surface = new Surface(line.length() * 1.0, line.centroid(), new Vector(0, -1, 0));
        Face fs = new Face(new Node[]{n0, n1}, line.vtkType(), surface, cell, null, numVars);
        line = new Line(p1, p2);
        surface = new Surface(line.length() * 1.0, line.centroid(), new Vector(1, 0, 0));
        Face fe = new Face(new Node[]{n1, n2}, line.vtkType(), surface, cell, null, numVars);
        line = new Line(p2, p3);
        surface = new Surface(line.length() * 1.0, line.centroid(), new Vector(0, 1, 0));
        Face fn = new Face(new Node[]{n2, n3}, line.vtkType(), surface, cell, null, numVars);
        line = new Line(p3, p0);
        surface = new Surface(line.length() * 1.0, line.centroid(), new Vector(-1, 0, 0));
        Face fw = new Face(new Node[]{n3, n0}, line.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(fs, fe, fn, fw));

        DoubleArray.copy(new double[]{5.5, 6.4, 4}, fe.U);
        DoubleArray.copy(new double[]{-5, 54.5, 4}, fw.U);
        DoubleArray.copy(new double[]{-56, 0, 6}, fn.U);
        DoubleArray.copy(new double[]{8, 0, -6}, fs.U);

        double dx = 1;
        double dy = 1;
        Vector[] expectedGradients = {
                new Vector((5.5 + 5) / dx, (-56 - 8) / dy, 0),
                new Vector((6.4 - 54.5) / dx, (0.0 - 0.0) / dy, 0),
                new Vector((4.0 - 4.0) / dx, (6 + 6) / dy, 0)
        };

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_xy_single_tri_cell() {
        int numVars = 3;
        Point p0 = new Point(0, 2, 0);
        Point p1 = new Point(1, 1, 0);
        Point p2 = new Point(-1, 0, 0);
        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);

        Triangle tri = new Triangle(p0, p1, p2);
        Shape shape = new Shape(tri.area() * 1.0, tri.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2}, tri.vtkType(), shape, numVars);
        cell.setIndex(0);

        Vector up = new Vector(0, 0, 1);
        Line line = new Line(p2, p1);
        Surface surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f0 = new Face(new Node[]{n2, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p1, p0);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f1 = new Face(new Node[]{n0, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p0, p2);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f2 = new Face(new Node[]{n2, n0}, line.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2));

        Vector[] expectedGradients = {
                new Vector(3.4, 6.2, 0.0),
                new Vector(8.4, 172, 0.0),
                new Vector(21, 45, 0.0)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_xy_structured_mesh() throws IOException {
        File tempFile = new File("test/test_data/tempMesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n");
            writer.write("" +
                    "-2.000000            1.000000             0.000000\n" +
                    "-2.000000            2.500000             0.000000\n" +
                    "-2.000000            4.000000             0.000000\n" +
                    "-2.000000            5.500000             0.000000\n" +
                    "-1.500000            1.000000             0.000000\n" +
                    "-1.500000            2.500000             0.000000\n" +
                    "-1.500000            4.000000             0.000000\n" +
                    "-1.500000            5.500000             0.000000\n" +
                    "-1.000000            1.000000             0.000000\n" +
                    "-1.000000            2.500000             0.000000\n" +
                    "-1.000000            4.000000             0.000000\n" +
                    "-1.000000            5.500000             0.000000\n" +
                    "-0.500000            1.000000             0.000000\n" +
                    "-0.500000            2.500000             0.000000\n" +
                    "-0.500000            4.000000             0.000000\n" +
                    "-0.500000            5.500000             0.000000\n");
        }

        int numVars = 3;
        Mesh mesh = new Structured2DMesh(tempFile, numVars, null, null, null, null);
        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file created while testing: " + tempFile);

        Cell cell = mesh.cells().get(4);
        Point expectedCentroid = new Point(-1.25, 3.25, 0);
        assertPointEquals(expectedCentroid, cell.shape.centroid, 1e-12);

        Vector[] expectedGradients = {
                new Vector(3.4, 6.2, 0.0),
                new Vector(8.4, 172, 0.0),
                new Vector(21, 45, 0.0)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_yz_single_tri_cell() {
        int numVars = 3;
        Point p0 = new Point(0, 0, 2);
        Point p1 = new Point(0, 1, 1);
        Point p2 = new Point(0, -1, 0);
        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);

        Triangle tri = new Triangle(p0, p1, p2);
        Shape shape = new Shape(tri.area() * 1.0, tri.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2}, tri.vtkType(), shape, numVars);
        cell.setIndex(0);

        Vector up = new Vector(1, 0, 0);
        Line line = new Line(p2, p1);
        Surface surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f0 = new Face(new Node[]{n2, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p1, p0);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f1 = new Face(new Node[]{n0, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p0, p2);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f2 = new Face(new Node[]{n2, n0}, line.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2));

        Vector[] expectedGradients = {
                new Vector(0.0, 3.4, 6.2),
                new Vector(0.0, 8.4, 172),
                new Vector(0.0, 21, 45)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_xz_single_tri_cell() {
        int numVars = 3;
        Point p0 = new Point(0, 0, 2);
        Point p1 = new Point(1, 0, 1);
        Point p2 = new Point(-1, 0, 0);
        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);

        Triangle tri = new Triangle(p0, p1, p2);
        Shape shape = new Shape(tri.area() * 1.0, tri.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2}, tri.vtkType(), shape, numVars);
        cell.setIndex(0);

        Vector up = new Vector(0, -1, 0);
        Line line = new Line(p2, p1);
        Surface surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f0 = new Face(new Node[]{n2, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p1, p0);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f1 = new Face(new Node[]{n0, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p0, p2);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f2 = new Face(new Node[]{n2, n0}, line.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2));

        Vector[] expectedGradients = {
                new Vector(3.4, 0.0, 6.2),
                new Vector(8.4, 0.0, 172),
                new Vector(21, 0.0, 45)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void twoDim_xyz_single_tri_cell() {
        int numVars = 3;
        Point p0 = new Point(0, 2, 0);
        Point p1 = new Point(1, 1, 0);
        Point p2 = new Point(-1, 0, 0.5);
        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);

        Triangle tri = new Triangle(p0, p1, p2);
        Shape shape = new Shape(tri.area() * 1.0, tri.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2}, tri.vtkType(), shape, numVars);
        cell.setIndex(0);

        Vector up = new Vector(p2, p1).cross(new Vector(p2, p0)).unit();
        Line line = new Line(p2, p1);
        Surface surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f0 = new Face(new Node[]{n2, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p1, p0);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f1 = new Face(new Node[]{n0, n1}, line.vtkType(), surface, cell, null, numVars);

        line = new Line(p0, p2);
        surface = new Surface(line.length() * 1.0, line.centroid(), unitNormal(line, up));
        Face f2 = new Face(new Node[]{n2, n0}, line.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2));

        Vector[] expectedGradients = {
                new Vector(3.4, 8.7, 6.2),
                new Vector(-8.4, 2.7, 10),
                new Vector(1, -9.0, 12)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        Vector[] normalGradientComponents = new Vector[numVars];
        for (int var = 0; var < numVars; var++) {
            normalGradientComponents[var] = up.mult(expectedGradients[var].dot(up));
            expectedGradients[var] = expectedGradients[var].sub(normalGradientComponents[var]);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void threeDim_hexahedron() {
        int numVars = 3;
        Point p0 = new Point(-1, -1, -1);
        Point p1 = new Point(1, -1, -1);
        Point p2 = new Point(1, 1, -1);
        Point p3 = new Point(-1, 1, -1);
        Point p4 = new Point(-1, -1, 1);
        Point p5 = new Point(1, -1, 1);
        Point p6 = new Point(1, 1, 1);
        Point p7 = new Point(-1, 1, 1);

        Hexahedron hexahedron = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);

        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);
        Node n3 = new Node(p3, numVars);
        Node n4 = new Node(p4, numVars);
        Node n5 = new Node(p5, numVars);
        Node n6 = new Node(p6, numVars);
        Node n7 = new Node(p7, numVars);

        Shape shape = new Shape(hexahedron.volume(), hexahedron.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2, n3, n4, n5, n6, n7}, hexahedron.vtkType(), shape, numVars);
        cell.setIndex(0);

        Quad quad = new Quad(p3, p2, p1, p0);
        Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f0 = new Face(new Node[]{n0, n1, n2, n3}, quad.vtkType(), surface, cell, null, numVars);

        quad = new Quad(p4, p5, p6, p7);
        surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f1 = new Face(new Node[]{n4, n5, n6, n7}, quad.vtkType(), surface, cell, null, numVars);

        quad = new Quad(p0, p4, p7, p3);
        surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f2 = new Face(new Node[]{n0, n4, n7, n3}, quad.vtkType(), surface, cell, null, numVars);

        quad = new Quad(p1, p2, p6, p5);
        surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f3 = new Face(new Node[]{n1, n2, n6, n5}, quad.vtkType(), surface, cell, null, numVars);

        quad = new Quad(p2, p3, p7, p6);
        surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f4 = new Face(new Node[]{n2, n3, n7, n6}, quad.vtkType(), surface, cell, null, numVars);

        quad = new Quad(p0, p1, p5, p4);
        surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        Face f5 = new Face(new Node[]{n0, n1, n5, n4}, quad.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2, f3, f4, f5));

        Vector[] expectedGradients = {
                new Vector(3.4, 8.7, 6.2),
                new Vector(-8.4, 2.7, 10),
                new Vector(1, -9.0, 12)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void threeDim_tetrahedron() {
        int numVars = 3;
        Point p0 = new Point(2, 3, -3);
        Point p1 = new Point(0, 0, 0);
        Point p2 = new Point(5, 1, -0.5);
        Point p3 = new Point(2.5, -0.5, -2.5);

        Tetra tetra = new Tetra(p0, p1, p2, p3);

        Node n0 = new Node(p0, numVars);
        Node n1 = new Node(p1, numVars);
        Node n2 = new Node(p2, numVars);
        Node n3 = new Node(p3, numVars);

        Shape shape = new Shape(tetra.volume(), tetra.centroid());

        Cell cell = new Cell(new Node[]{n0, n1, n2, n3}, tetra.vtkType(), shape, numVars);
        cell.setIndex(0);

        Triangle tri = new Triangle(p0, p1, p2);
        Surface surface = new Surface(tri.area(), tri.centroid(), tri.unitNormal());
        Face f0 = new Face(new Node[]{n0, n1, n2}, tri.vtkType(), surface, cell, null, numVars);

        tri = new Triangle(p0, p3, p1);
        surface = new Surface(tri.area(), tri.centroid(), tri.unitNormal());
        Face f1 = new Face(new Node[]{n0, n3, n1}, tri.vtkType(), surface, cell, null, numVars);

        tri = new Triangle(p3, p2, p1);
        surface = new Surface(tri.area(), tri.centroid(), tri.unitNormal());
        Face f2 = new Face(new Node[]{n3, n2, n1}, tri.vtkType(), surface, cell, null, numVars);

        tri = new Triangle(p0, p2, p3);
        surface = new Surface(tri.area(), tri.centroid(), tri.unitNormal());
        Face f3 = new Face(new Node[]{n0, n2, n3}, tri.vtkType(), surface, cell, null, numVars);

        cell.faces.addAll(List.of(f0, f1, f2, f3));

        Vector[] expectedGradients = {
                new Vector(3.4, 8.7, 6.2),
                new Vector(-8.4, 2.7, 10),
                new Vector(1, -9.0, 12)
        };

        double[] Ui = {2.8, -78, -54};

        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] faceU = new double[numVars];
            for (int var = 0; var < numVars; var++) {
                // set face values using linear interpolation
                faceU[var] = Ui[var] + expectedGradients[var].dot(dr);
            }
            DoubleArray.copy(faceU, face.U);
        }

        new GreenGaussCellGradient(createMesh(cell)).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        assertVectorEquals(expectedGradients[0], actualGradients[0], 1e-12);
        assertVectorEquals(expectedGradients[1], actualGradients[1], 1e-12);
        assertVectorEquals(expectedGradients[2], actualGradients[2], 1e-12);
    }

    @Test
    public void threeDim_structured_mesh() throws IOException {
        File tempFile = new File("test/test_data/mesh.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 3\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n" +
                    "zeta = 4\n" +
                    "2.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "2.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "2.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "2.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "2.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "2.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "2.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "2.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "2.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "2.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "3.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "3.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "3.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "3.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "3.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "3.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "3.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "3.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "4.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "4.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "4.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "4.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "4.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "4.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "4.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "4.000000000000000    3.000000000000000    6.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    -3.000000000000000  \n" +
                    "5.000000000000000    1.000000000000000    0.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    3.000000000000000   \n" +
                    "5.000000000000000    1.000000000000000    6.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    -3.000000000000000  \n" +
                    "5.000000000000000    1.666666666666667    0.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    3.000000000000000   \n" +
                    "5.000000000000000    1.666666666666667    6.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    -3.000000000000000  \n" +
                    "5.000000000000000    2.333333333333333    0.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    3.000000000000000   \n" +
                    "5.000000000000000    2.333333333333333    6.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    -3.000000000000000  \n" +
                    "5.000000000000000    3.000000000000000    0.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    3.000000000000000   \n" +
                    "5.000000000000000    3.000000000000000    6.000000000000000   \n");
        }

        int numVars = 2;
        Structured3DMesh mesh = new Structured3DMesh(tempFile, numVars,
                null, null, null, null, null, null);

        if (!tempFile.delete())
            System.out.println("Unable to delete temporary file: " + tempFile);

        Cell cell = mesh.cells().get(13);

        Vector[] expectedGradients = {
                new Vector(-1.50, -0.86, 1.92),
                new Vector(9.80, -3.61, -5.48)
        };

        double[] U0 = {4.31, -7.18};
        for (Face face : cell.faces) {
            Vector dr = new Vector(cell.shape.centroid, face.surface.centroid);
            double[] U = new double[expectedGradients.length];
            for (int i = 0; i < expectedGradients.length; i++) {
                U[i] = U0[i] + expectedGradients[i].dot(dr);
            }
            DoubleArray.copy(U, face.U);
        }

        new GreenGaussCellGradient(mesh).setupAllCells();
        Vector[] actualGradients = cell.gradientU;

        assertEquals(numVars, actualGradients.length);
        for (int i = 0; i < numVars; i++) {
            assertVectorEquals(expectedGradients[i], actualGradients[i], 1e-12);
        }
    }

    private Mesh createMesh(Cell... cellArray) {
        return new Mesh() {
            private List<Cell> cells = List.of(cellArray);

            @Override
            public List<Cell> cells() {
                return cells;
            }

            @Override
            public List<Face> internalFaces() {
                return null;
            }

            @Override
            public List<Node> nodes() {
                return null;
            }

            @Override
            public List<Boundary> boundaries() {
                return null;
            }
        };
    }
}
