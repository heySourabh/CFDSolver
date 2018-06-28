package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Line;
import main.geom.factory.Quad;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import main.util.DataFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Structured2DMesh implements Mesh {
    private final List<Cell> cells;
    private final List<Face> internalFaces;
    private final List<Node> nodes;
    private final List<Boundary> boundaries;

    public Structured2DMesh(File meshFile, int numVars,
                            BoundaryCondition bc_xiMin, BoundaryCondition bc_xiMax,
                            BoundaryCondition bc_etaMin, BoundaryCondition bc_etaMax) throws FileNotFoundException {
        DataFileReader meshFileReader = new DataFileReader(meshFile, "%");
        int dim = meshFileReader.readIntParameter("dimension");
        if (dim != 2) {
            throw new IllegalArgumentException("The mesh file dimension must be 2.");
        }

        String mode = meshFileReader.readParameter("mode");
        if (!mode.equals("ASCII")) {
            throw new IllegalArgumentException("Only ASCII mode is supported.");
        }

        int num_xi = meshFileReader.readIntParameter("xi");
        int num_eta = meshFileReader.readIntParameter("eta");

        Node[][] nodeArray = new Node[num_xi][num_eta];
        this.nodes = new ArrayList<>();
        for (int i = 0; i < num_xi; i++) {
            for (int j = 0; j < num_eta; j++) {
                Node node = new Node(meshFileReader.readXYZ());
                nodeArray[i][j] = node;
                nodes.add(node);
            }
        }

        Cell[][] cellArray = new Cell[num_xi - 1][num_eta - 1];
        cells = new ArrayList<>();
        int cellIndex = 0;
        for (int i = 0; i < num_xi - 1; i++) {
            for (int j = 0; j < num_eta - 1; j++) {
                Node[] n = new Node[]{nodeArray[i][j], nodeArray[i + 1][j], nodeArray[i + 1][j + 1], nodeArray[i][j + 1]};
                Geometry cellGeom = new Quad(n[0].location(), n[1].location(),
                        n[2].location(), n[3].location());
                Cell cell = new Cell(cellIndex, n, VTKType.VTK_QUAD,
                        new Shape(cellGeom.area(), cellGeom.centroid()), numVars);
                cellArray[i][j] = cell;
                cells.add(cell);
                cellIndex++;
            }
        }

        this.internalFaces = new ArrayList<>();
        Set<Face> faceSet = new HashSet<>();
        Node na, nb;
        for (Cell cell : cells) {
            Node n0 = cell.nodes[0];
            Node n1 = cell.nodes[1];
            Node n2 = cell.nodes[2];
            Node n3 = cell.nodes[3];

            Geometry cellGeom = new Quad(n0.location(), n1.location(), n2.location(), n3.location());
            Vector cellNormal = cellGeom.unitNormal();

            // Face 0
            na = n0;
            nb = n1;
            Geometry faceGeom = new Line(na.location(), nb.location());
            double area = faceGeom.length() * 1.0;
            Point centroid = faceGeom.centroid();

            Vector faceTangent = new Vector(na.location(), nb.location());
            Vector unitNormal = faceTangent.cross(cellNormal).unit();

            Surface surface = new Surface(area, centroid, unitNormal);

            Face f0 = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, cell, null, numVars);

            // Face 1
            na = n1;
            nb = n2;
            faceGeom = new Line(na.location(), nb.location());
            area = faceGeom.length() * 1.0;
            centroid = faceGeom.centroid();

            faceTangent = new Vector(na.location(), nb.location());
            unitNormal = faceTangent.cross(cellNormal).unit();

            surface = new Surface(area, centroid, unitNormal);

            Face f1 = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, cell, null, numVars);

            // Face 2
            na = n2;
            nb = n3;
            faceGeom = new Line(na.location(), nb.location());
            area = faceGeom.length() * 1.0;
            centroid = faceGeom.centroid();

            faceTangent = new Vector(na.location(), nb.location());
            unitNormal = faceTangent.cross(cellNormal).unit();

            surface = new Surface(area, centroid, unitNormal);

            Face f2 = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, cell, null, numVars);

            // Face 3
            na = n3;
            nb = n0;
            faceGeom = new Line(na.location(), nb.location());
            area = faceGeom.length() * 1.0;
            centroid = faceGeom.centroid();

            faceTangent = new Vector(na.location(), nb.location());
            unitNormal = faceTangent.cross(cellNormal).unit();

            surface = new Surface(area, centroid, unitNormal);

            Face f3 = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, cell, null, numVars);

            faceSet.addAll(List.of(f0, f1, f2, f3));
        }
        internalFaces.addAll(faceSet);

        // Remove all boundary faces
        internalFaces.removeIf(face -> face.right == null);

        this.boundaries = new ArrayList<>();
        Face[] xiMinFaces = new Face[num_eta - 1];
        Face[] xiMaxFaces = new Face[num_eta - 1];
        Face[] etaMinFaces = new Face[num_xi - 1];
        Face[] etaMaxFaces = new Face[num_xi - 1];
        int i, j;
        i = 0;
        for (j = 0; j < num_eta - 1; j++) {
            Node n0 = nodeArray[i][j];
            Node n1 = nodeArray[i + 1][j];
            Node n2 = nodeArray[i + 1][j + 1];
            Node n3 = nodeArray[i][j + 1];

            na = n3;
            nb = n0;

            Cell left = cellArray[i][j];

            Geometry cellGeom = new Quad(n0.location(), n1.location(), n2.location(), n3.location());
            Vector cellNormal = cellGeom.unitNormal();
            Vector faceTangent = new Vector(na.location(), nb.location());
            Vector unitVector = faceTangent.cross(cellNormal);

            Geometry faceGeom = new Line(na.location(), nb.location());

            Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), unitVector);

            Face boundaryFace = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, left, null, numVars);
            boundaryFace.right = Mesh.ghostCell(left, boundaryFace);
            xiMinFaces[j] = boundaryFace;
        }

        i = num_xi - 2;
        for (j = 0; j < num_eta - 1; j++) {
            Node n0 = nodeArray[i][j];
            Node n1 = nodeArray[i + 1][j];
            Node n2 = nodeArray[i + 1][j + 1];
            Node n3 = nodeArray[i][j + 1];

            na = n1;
            nb = n2;

            Cell left = cellArray[i][j];

            Geometry cellGeom = new Quad(n0.location(), n1.location(), n2.location(), n3.location());
            Vector cellNormal = cellGeom.unitNormal();
            Vector faceTangent = new Vector(na.location(), nb.location());
            Vector unitVector = faceTangent.cross(cellNormal);

            Geometry faceGeom = new Line(na.location(), nb.location());

            Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), unitVector);

            Face boundaryFace = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, left, null, numVars);
            boundaryFace.right = Mesh.ghostCell(left, boundaryFace);
            xiMaxFaces[j] = boundaryFace;
        }

        j = 0;
        for (i = 0; i < num_xi - 1; i++) {
            Node n0 = nodeArray[i][j];
            Node n1 = nodeArray[i + 1][j];
            Node n2 = nodeArray[i + 1][j + 1];
            Node n3 = nodeArray[i][j + 1];

            na = n0;
            nb = n1;

            Cell left = cellArray[i][j];

            Geometry cellGeom = new Quad(n0.location(), n1.location(), n2.location(), n3.location());
            Vector cellNormal = cellGeom.unitNormal();
            Vector faceTangent = new Vector(na.location(), nb.location());
            Vector unitVector = faceTangent.cross(cellNormal);

            Geometry faceGeom = new Line(na.location(), nb.location());

            Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), unitVector);

            Face boundaryFace = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, left, null, numVars);
            boundaryFace.right = Mesh.ghostCell(left, boundaryFace);
            etaMinFaces[i] = boundaryFace;
        }

        j = num_eta - 2;
        for (i = 0; i < num_xi - 1; i++) {
            Node n0 = nodeArray[i][j];
            Node n1 = nodeArray[i + 1][j];
            Node n2 = nodeArray[i + 1][j + 1];
            Node n3 = nodeArray[i][j + 1];

            na = n2;
            nb = n3;

            Cell left = cellArray[i][j];

            Geometry cellGeom = new Quad(n0.location(), n1.location(), n2.location(), n3.location());
            Vector cellNormal = cellGeom.unitNormal();
            Vector faceTangent = new Vector(na.location(), nb.location());
            Vector unitVector = faceTangent.cross(cellNormal);

            Geometry faceGeom = new Line(na.location(), nb.location());

            Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), unitVector);

            Face boundaryFace = new Face(new Node[]{na, nb}, faceGeom.vtkType(), surface, left, null, numVars);
            boundaryFace.right = Mesh.ghostCell(left, boundaryFace);
            etaMaxFaces[i] = boundaryFace;
        }
        boundaries.add(new Boundary("xi min", List.of(xiMinFaces), bc_xiMin));
        boundaries.add(new Boundary("xi max", List.of(xiMaxFaces), bc_xiMax));
        boundaries.add(new Boundary("eta min", List.of(etaMinFaces), bc_etaMin));
        boundaries.add(new Boundary("eta max", List.of(etaMaxFaces), bc_etaMax));

        // Setup node neighbors
        for (Cell cell : cells) {
            for (Node node : cell.nodes) {
                node.neighbors.add(cell);
            }
        }
        for (Boundary bnd : boundaries) {
            for (Face face : bnd.faces) {
                for (Node node : face.right.nodes) {
                    node.neighbors.add(face.right);
                }
            }
        }

        // Setup faces of cells
        for (Face face : internalFaces) {
            face.left.faces.add(face);
            face.right.faces.add(face);
        }
        for (Boundary bnd : boundaries) {
            for (Face face : bnd.faces) {
                face.left.faces.add(face);
                face.right.faces.add(face);
            }
        }
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
