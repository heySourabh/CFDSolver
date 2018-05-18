package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Vertex;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import main.util.DataFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Structured1DMesh implements Mesh {

    final private List<Node> nodes;
    final private List<Cell> cells;
    final private List<Face> internalFaces;
    final private List<Boundary> boundaries;

    public Structured1DMesh(File meshFile, int numVars, BoundaryCondition bc_xiMin, BoundaryCondition bc_xiMax) throws FileNotFoundException {
        DataFileReader meshFileReader = new DataFileReader(meshFile, "%");
        int dim = meshFileReader.readIntParameter("dimension");
        if (dim != 1) {
            throw new IllegalArgumentException("The mesh file dimension must be 1.");
        }
        String mode = meshFileReader.readParameter("mode");
        if (!mode.equals("ASCII")) {
            throw new IllegalArgumentException("Only ASCII mode is supported.");
        }
        int xi = meshFileReader.readIntParameter("xi");

        this.nodes = IntStream.range(0, xi)
                .mapToObj(i -> new Node(meshFileReader.readXYZ()))
                .collect(toList());

        this.cells = new ArrayList<>();
        for (int i = 0; i < xi - 1; i++) {
            Node n0 = nodes.get(i);
            Node n1 = nodes.get(i + 1);
            Geometry cellGeometry = new Line(new Point(n0.x, n0.y, n0.z), new Point(n1.x, n1.y, n1.z));
            Shape cellShape = new Shape(cellGeometry.length() * 1.0 * 1.0, cellGeometry.centroid());
            Cell cell = new Cell(i, new Node[]{n0, n1}, VTKType.VTK_LINE, cellShape, numVars);
            this.cells.add(cell);
        }

        this.internalFaces = new ArrayList<>();
        for (int i = 1; i < xi - 1; i++) {
            Node ni = this.nodes.get(i);
            Node nim1 = this.nodes.get(i - 1);
            Node nip1 = this.nodes.get(i + 1);

            Point pi = new Point(ni.x, ni.y, ni.z);
            Point pim1 = new Point(nim1.x, nim1.y, nim1.z);
            Point pip1 = new Point(nip1.x, nip1.y, nip1.z);

            Geometry faceGeometry = new Vertex(new Point(ni.x, ni.y, ni.z));

            Vector faceNormal = new Vector(pim1, pi).add(new Vector(pi, pip1));
            Vector faceUnitNormal = faceNormal.unit();

            Surface surface = new Surface(1.0 * 1.0, faceGeometry.centroid(), faceUnitNormal);

            Cell left = this.cells.get(i - 1);
            Cell right = this.cells.get(i);

            Face face = new Face(new Node[]{ni}, VTKType.VTK_VERTEX, surface, left, right, numVars);
            this.internalFaces.add(face);
        }


        // Boundaries
        this.boundaries = new ArrayList<>();
        // xi min boundary
        Node boundaryNode = nodes.get(0);
        Node interNode = nodes.get(1);
        Point boundaryPoint = new Point(boundaryNode.x, boundaryNode.y, boundaryNode.z);
        Point interPoint = new Point(interNode.x, interNode.y, interNode.z);
        Vector faceNormal = new Vector(interPoint, boundaryPoint);
        Point ghostPoint = new Point(
                boundaryNode.x + faceNormal.x,
                boundaryNode.y + faceNormal.y,
                boundaryNode.z + faceNormal.z);
        Node ghostNode = new Node(ghostPoint);

        Geometry ghostCellGeom = new Line(ghostPoint, boundaryPoint);
        Cell ghostCell = new Cell(-1,
                new Node[]{ghostNode, boundaryNode},
                ghostCellGeom.vtkType(),
                new Shape(ghostCellGeom.length() * 1.0 * 1.0, ghostCellGeom.centroid()),
                numVars);

        Geometry boundaryFaceGeom = new Vertex(boundaryPoint);
        Surface surface = new Surface(1.0 * 1.0, boundaryFaceGeom.centroid(), faceNormal.unit());
        Face boundaryFace = new Face(new Node[]{boundaryNode},
                VTKType.VTK_VERTEX, surface,
                cells.get(0),
                ghostCell,
                numVars);

        Boundary boundary = new Boundary("xi min", List.of(boundaryFace), bc_xiMin);
        boundaries.add(boundary);

        // xi max boundary
        boundaryNode = nodes.get(xi - 1);
        interNode = nodes.get(xi - 2);
        boundaryPoint = new Point(boundaryNode.x, boundaryNode.y, boundaryNode.z);
        interPoint = new Point(interNode.x, interNode.y, interNode.z);
        faceNormal = new Vector(interPoint, boundaryPoint);
        ghostPoint = new Point(
                boundaryNode.x + faceNormal.x,
                boundaryNode.y + faceNormal.y,
                boundaryNode.z + faceNormal.z);
        ghostNode = new Node(ghostPoint);

        ghostCellGeom = new Line(boundaryPoint, ghostPoint);
        ghostCell = new Cell(-1,
                new Node[]{boundaryNode, ghostNode},
                ghostCellGeom.vtkType(),
                new Shape(ghostCellGeom.length() * 1.0 * 1.0, ghostCellGeom.centroid()),
                numVars);

        boundaryFaceGeom = new Vertex(boundaryPoint);
        surface = new Surface(1.0 * 1.0, boundaryFaceGeom.centroid(), faceNormal.unit());
        boundaryFace = new Face(new Node[]{boundaryNode},
                VTKType.VTK_VERTEX, surface,
                cells.get(xi - 2),
                ghostCell,
                numVars);

        boundary = new Boundary("xi max", List.of(boundaryFace), bc_xiMax);
        boundaries.add(boundary);

        // TODO: Setup the faces of cells (caution with the test case infinite loop!)
        // TODO: Setup node neighbors
    }

    @Override
    public List<Cell> cells() {
        return cells;
    }

    @Override
    public List<Face> internalFaces() {
        return internalFaces;
    }

    @Override
    public List<Node> nodes() {
        return nodes;
    }

    @Override
    public List<Boundary> boundaries() {
        return boundaries;
    }
}
