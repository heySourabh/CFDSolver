package main.mesh.factory;

import main.geom.GeometryHelper;
import main.geom.GeometryHelper.TriGeom;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.factory.Hexahedron;
import main.geom.factory.Quad;
import main.io.DataFileReader;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Structured3DMesh implements Mesh {
    private enum FaceOrientation {
        CLOCKWISE, COUNTER_CLOCKWISE
    }

    private final List<Cell> cells;
    private final List<Face> internalFaces;
    private final List<Node> nodes;
    private final List<Boundary> boundaries;

    public Structured3DMesh(File meshFile, int numVars,
                            BoundaryCondition bc_xiMin, BoundaryCondition bc_xiMax,
                            BoundaryCondition bc_etaMin, BoundaryCondition bc_etaMax,
                            BoundaryCondition bc_zetaMin, BoundaryCondition bc_zetaMax) throws FileNotFoundException {
        int num_xi, num_eta, num_zeta;
        Node[][][] nodeArray;
        try (DataFileReader meshFileReader = new DataFileReader(meshFile, "%")) {
            int dim = meshFileReader.readIntParameter("dimension");
            if (dim != 3) {
                throw new IllegalArgumentException("The mesh file dimension must be 3.");
            }

            String mode = meshFileReader.readParameter("mode");
            if (!mode.equals("ASCII")) {
                throw new IllegalArgumentException("Only ASCII mode is supported.");
            }

            num_xi = meshFileReader.readIntParameter("xi");
            num_eta = meshFileReader.readIntParameter("eta");
            num_zeta = meshFileReader.readIntParameter("zeta");

            nodeArray = new Node[num_xi][num_eta][num_zeta];
            this.nodes = new ArrayList<>();
            for (int i = 0; i < num_xi; i++) {
                for (int j = 0; j < num_eta; j++) {
                    for (int k = 0; k < num_zeta; k++) {
                        Node node = new Node(meshFileReader.readXYZ(), numVars);
                        nodeArray[i][j][k] = node;
                        this.nodes.add(node);
                    }
                }
            }
        }

        this.cells = new ArrayList<>();
        Cell[][][] cellArray = new Cell[num_xi - 1][num_eta - 1][num_zeta - 1];
        for (int i = 0; i < num_xi - 1; i++) {
            for (int j = 0; j < num_eta - 1; j++) {
                for (int k = 0; k < num_zeta - 1; k++) {
                    Cell cell = hexCell(nodeArray, i, j, k, numVars);
                    cellArray[i][j][k] = cell;
                    this.cells.add(cell);
                }
            }
        }
        setAllCellIndices();

        this.internalFaces = this.cells.stream()
                .flatMap(cell -> hexCellFaces(cell).stream())
                .distinct()
                .collect(Collectors.toList());

        // Remove all boundary faces
        this.internalFaces.removeIf(face -> face.right == null);

        int i, j, k;
        List<Face> xiMinFaces = new ArrayList<>();
        i = 0;
        for (j = 0; j < num_eta - 1; j++) {
            for (k = 0; k < num_zeta - 1; k++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i][j][k];
                Node nb = nodeArray[i][j][k + 1];
                Node nc = nodeArray[i][j + 1][k + 1];
                Node nd = nodeArray[i][j + 1][k];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                xiMinFaces.add(face);
            }
        }

        List<Face> xiMaxFaces = new ArrayList<>();
        i = num_xi - 2;
        for (j = 0; j < num_eta - 1; j++) {
            for (k = 0; k < num_zeta - 1; k++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i + 1][j][k];
                Node nb = nodeArray[i + 1][j + 1][k];
                Node nc = nodeArray[i + 1][j + 1][k + 1];
                Node nd = nodeArray[i + 1][j][k + 1];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                xiMaxFaces.add(face);
            }
        }

        List<Face> etaMinFaces = new ArrayList<>();
        j = 0;
        for (i = 0; i < num_xi - 1; i++) {
            for (k = 0; k < num_zeta - 1; k++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i][j][k];
                Node nb = nodeArray[i + 1][j][k];
                Node nc = nodeArray[i + 1][j][k + 1];
                Node nd = nodeArray[i][j][k + 1];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                etaMinFaces.add(face);
            }
        }

        List<Face> etaMaxFaces = new ArrayList<>();
        j = num_eta - 2;
        for (i = 0; i < num_xi - 1; i++) {
            for (k = 0; k < num_zeta - 1; k++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i][j + 1][k];
                Node nb = nodeArray[i][j + 1][k + 1];
                Node nc = nodeArray[i + 1][j + 1][k + 1];
                Node nd = nodeArray[i + 1][j + 1][k];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                etaMaxFaces.add(face);
            }
        }

        List<Face> zetaMinFaces = new ArrayList<>();
        k = 0;
        for (i = 0; i < num_xi - 1; i++) {
            for (j = 0; j < num_eta - 1; j++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i][j][k];
                Node nb = nodeArray[i][j + 1][k];
                Node nc = nodeArray[i + 1][j + 1][k];
                Node nd = nodeArray[i + 1][j][k];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                zetaMinFaces.add(face);
            }
        }

        List<Face> zetaMaxFaces = new ArrayList<>();
        k = num_zeta - 2;
        for (i = 0; i < num_xi - 1; i++) {
            for (j = 0; j < num_eta - 1; j++) {
                // Quad a-b-c-d will have normal pointing outward from the cell,
                // if the orientation of the cell is counter-clockwise (in standard VTK_HEXAHEDRON numbering)
                Node na = nodeArray[i][j][k + 1];
                Node nb = nodeArray[i + 1][j][k + 1];
                Node nc = nodeArray[i + 1][j + 1][k + 1];
                Node nd = nodeArray[i][j + 1][k + 1];

                Point pa = na.location();
                Point pb = nb.location();
                Point pc = nc.location();
                Point pd = nd.location();

                Cell innerCell = cellArray[i][j][k];
                Quad quad = new Quad(pa, pb, pc, pd);
                Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
                FaceOrientation faceOrientation = faceOrientation(
                        innerCell.nodes[0], innerCell.nodes[1], innerCell.nodes[2], innerCell.nodes[3],
                        innerCell.nodes[4], innerCell.nodes[5], innerCell.nodes[6], innerCell.nodes[7]
                );
                surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.CLOCKWISE ? -1 : 1);

                Face face = new Face(new Node[]{na, nb, nc, nd}, VTKType.VTK_QUAD, surface, innerCell, null, numVars);
                face.right = Mesh.ghostCell(innerCell, face);
                zetaMaxFaces.add(face);
            }
        }

        this.boundaries = List.of(
                new Boundary("xi min", xiMinFaces, bc_xiMin),
                new Boundary("xi max", xiMaxFaces, bc_xiMax),
                new Boundary("eta min", etaMinFaces, bc_etaMin),
                new Boundary("eta max", etaMaxFaces, bc_etaMax),
                new Boundary("zeta min", zetaMinFaces, bc_zetaMin),
                new Boundary("zeta max", zetaMaxFaces, bc_zetaMax));

        // set cell faces
        for (Face face : this.internalFaces) {
            face.left.faces.add(face);
            face.right.faces.add(face);
        }
        for (Boundary boundary : this.boundaries) {
            for (Face face : boundary.faces) {
                face.left.faces.add(face);
                face.right.faces.add(face);
            }
        }

        // set node neighbors
        for (Cell cell : this.cells) {
            for (Node node : cell.nodes) {
                node.neighbors.add(cell);
            }
        }
        for (Boundary boundary : this.boundaries) {
            for (Face face : boundary.faces) {
                for (Node node : face.right.nodes) {
                    node.neighbors.add(face.right);
                }
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

    private Cell hexCell(Node[][][] nodeArray, int i, int j, int k, int numVars) {
        Node n0 = nodeArray[i][j][k];
        Node n1 = nodeArray[i + 1][j][k];
        Node n2 = nodeArray[i + 1][j + 1][k];
        Node n3 = nodeArray[i][j + 1][k];
        Node n4 = nodeArray[i][j][k + 1];
        Node n5 = nodeArray[i + 1][j][k + 1];
        Node n6 = nodeArray[i + 1][j + 1][k + 1];
        Node n7 = nodeArray[i][j + 1][k + 1];

        Point p0 = n0.location();
        Point p1 = n1.location();
        Point p2 = n2.location();
        Point p3 = n3.location();
        Point p4 = n4.location();
        Point p5 = n5.location();
        Point p6 = n6.location();
        Point p7 = n7.location();

        Hexahedron hexahedron = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);
        Shape shape = new Shape(hexahedron.volume(), hexahedron.centroid());

        return new Cell(new Node[]{n0, n1, n2, n3, n4, n5, n6, n7}, hexahedron.vtkType(), shape, numVars);
    }

    private List<Face> hexCellFaces(Cell cell) {
        Node n0 = cell.nodes[0];
        Node n1 = cell.nodes[1];
        Node n2 = cell.nodes[2];
        Node n3 = cell.nodes[3];
        Node n4 = cell.nodes[4];
        Node n5 = cell.nodes[5];
        Node n6 = cell.nodes[6];
        Node n7 = cell.nodes[7];

        FaceOrientation faceOrientation = faceOrientation(n0, n1, n2, n3, n4, n5, n6, n7);

        // The nodes are based on VTK_HEXAHEDRON numbering such that the faces are
        // oriented counter-clockwise, looking from outside of the volume
        Face f0 = createFace(n3, n2, n1, n0, cell, faceOrientation);
        Face f1 = createFace(n4, n5, n6, n7, cell, faceOrientation);
        Face f2 = createFace(n0, n1, n5, n4, cell, faceOrientation);
        Face f3 = createFace(n2, n3, n7, n6, cell, faceOrientation);
        Face f4 = createFace(n1, n2, n6, n5, cell, faceOrientation);
        Face f5 = createFace(n0, n4, n7, n3, cell, faceOrientation);

        return List.of(f0, f1, f2, f3, f4, f5);
    }

    private FaceOrientation faceOrientation(Node n0, Node n1, Node n2, Node n3, Node n4, Node n5, Node n6, Node n7) {
        // Assuming that the nodes of the VTK_HEXAHEDRON numbering setup the geometry with counter-clockwise triangles
        Point p0 = n0.location();
        Point p1 = n1.location();
        Point p2 = n2.location();
        Point p3 = n3.location();
        Point p4 = n4.location();
        Point p5 = n5.location();
        Point p6 = n6.location();
        Point p7 = n7.location();

        TriGeom[] surfaceTriangles = {
                new TriGeom(p0, p3, p2),
                new TriGeom(p0, p2, p1),
                new TriGeom(p4, p5, p6),
                new TriGeom(p4, p6, p7),
                new TriGeom(p1, p2, p6),
                new TriGeom(p1, p6, p5),
                new TriGeom(p0, p4, p7),
                new TriGeom(p0, p7, p3),
                new TriGeom(p0, p1, p5),
                new TriGeom(p0, p5, p4),
                new TriGeom(p3, p7, p6),
                new TriGeom(p3, p6, p2)
        };

        // if signed-volume is negative then the face orientation is clockwise
        return GeometryHelper.signedVolume(surfaceTriangles) < 0
                ? FaceOrientation.CLOCKWISE
                : FaceOrientation.COUNTER_CLOCKWISE;
    }

    private Face createFace(Node n0, Node n1, Node n2, Node n3, Cell left, FaceOrientation faceOrientation) {
        Point p0 = n0.location();
        Point p1 = n1.location();
        Point p2 = n2.location();
        Point p3 = n3.location();

        Quad quad = new Quad(p0, p1, p2, p3);
        Surface surface = new Surface(quad.area(), quad.centroid(), quad.unitNormal());
        surface.unitNormal = surface.unitNormal.mult(faceOrientation == FaceOrientation.COUNTER_CLOCKWISE ? 1 : -1);

        return new Face(new Node[]{n0, n1, n2, n3}, quad.vtkType(), surface, left, null, left.U.length);
    }
}