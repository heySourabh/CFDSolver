package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.*;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;
import main.io.DataFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Unstructured2DMesh implements Mesh {
    private final List<Cell> cells;
    private final List<Face> internalFaces;
    private final List<Node> nodes;
    private final List<Boundary> boundaries;

    public Unstructured2DMesh(File meshFile, int numVars, Map<String, BoundaryCondition> bcs) throws FileNotFoundException {
        DataFileReader meshFileReader = new DataFileReader(meshFile, "%");
        int dim = meshFileReader.readIntParameter("dimension");
        if (dim != 2) {
            throw new IllegalArgumentException("The mesh file dimension must be 2.");
        }
        String mode = meshFileReader.readParameter("mode");
        if (!mode.equals("ASCII")) {
            throw new IllegalArgumentException("Only ASCII mode is supported.");
        }

        int numPoints = meshFileReader.readIntParameter("points");
        this.nodes = List.copyOf(IntStream.range(0, numPoints)
                .mapToObj(i -> new Node(meshFileReader.readXYZ(), numVars))
                .collect(toList()));

        int numElements = meshFileReader.readIntParameter("elements");
        List<Cell> cellList = new ArrayList<>();
        Set<Face> faceSet = new HashSet<>();
        for (int i = 0; i < numElements; i++) {
            int[] connectivity = meshFileReader.readIntArray();
            VTKType vtkType = VTKType.get(connectivity[0]);
            Node[] cellNodes = new Node[connectivity.length - 1];
            Point[] cellPoints = new Point[cellNodes.length];
            for (int ni = 0; ni < cellNodes.length; ni++) {
                cellNodes[ni] = nodes.get(connectivity[ni + 1]);
                cellPoints[ni] = cellNodes[ni].location();
            }
            if (vtkType.dim != 2) {
                throw new IllegalArgumentException("The elements are not 2D.");
            }
            Geometry cellGeom;
            switch (vtkType) {
                case VTK_TRIANGLE:
                    cellGeom = new Triangle(cellPoints[0], cellPoints[1], cellPoints[2]);
                    break;
                case VTK_QUAD:
                    cellGeom = new Quad(cellPoints[0], cellPoints[1], cellPoints[2], cellPoints[3]);
                    break;
                case VTK_POLYGON:
                    cellGeom = new Polygon(cellPoints);
                    break;
                case VTK_TRIANGLE_STRIP:
                    cellGeom = new TriangleStrip(cellPoints);
                    break;
                default:
                    throw new UnsupportedOperationException("Cell: The geometry type " + vtkType + " is not supported.");
            }
            Shape cellShape = new Shape(cellGeom.area() * 1.0, cellGeom.centroid());
            Cell cell = new Cell(cellNodes, vtkType, cellShape, numVars);
            cellList.add(cell);

            // Create and add faces of the cell to Set
            faceSet.addAll(createCellFaces(cell, cellGeom));
        }
        this.cells = List.copyOf(cellList);
        setAllCellIndices();

        List<Face> allBoundaryFaces = faceSet.stream()
                .filter(f -> f.right == null)
                .collect(toList());

        this.internalFaces = List.copyOf(faceSet.stream()
                .filter(f -> f.right != null)
                .collect(toList()));

        int numBoundaries = meshFileReader.readIntParameter("boundaries");
        Boundary[] boundaryArray = new Boundary[numBoundaries];
        for (int bi = 0; bi < numBoundaries; bi++) {
            String bndName = meshFileReader.readParameter("bname");
            int numBndFaces = meshFileReader.readIntParameter("bfaces");
            List<Face> bndFaces = new ArrayList<>();
            for (int fi = 0; fi < numBndFaces; fi++) {
                int[] connectivity = meshFileReader.readIntArray();
                VTKType vtkType = VTKType.get(connectivity[0]);
                if (vtkType != VTKType.VTK_LINE) {
                    throw new UnsupportedOperationException("Face: The geometry type " + vtkType + " is not supported.");
                }
                Node[] faceNodes = new Node[connectivity.length - 1];
                for (int ni = 0; ni < faceNodes.length; ni++) {
                    faceNodes[ni] = nodes.get(connectivity[ni + 1]);
                }
                Face bndFace = search(faceNodes, allBoundaryFaces)
                        .orElseThrow(() -> new IllegalStateException("Couldn't locate boundary face in mesh."));
                bndFace.right = Mesh.ghostCell(bndFace.left, bndFace);
                bndFaces.add(bndFace);
            }
            boundaryArray[bi] = new Boundary(bndName, bndFaces, bcs.get(bndName));
        }
        this.boundaries = List.of(boundaryArray);
        meshFileReader.close();

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

    private List<Face> createCellFaces(Cell cell, Geometry cellGeom) {
        // Assumes 2D cell geometry with nodes ordered clockwise or counter-clockwise
        List<Face> cellFaces = new ArrayList<>();
        for (int i = 0; i < cell.nodes.length - 1; i++) {
            cellFaces.add(createFace(cell.nodes[i], cell.nodes[i + 1], cell, cellGeom));
        }
        cellFaces.add(createFace(cell.nodes[cell.nodes.length - 1], cell.nodes[0], cell, cellGeom));

        return cellFaces;
    }

    private Face createFace(Node n0, Node n1, Cell cell, Geometry cellGeom) {
        Vector cellNormal = cellGeom.unitNormal();
        Geometry faceGeom = new Line(n0.location(), n1.location());
        Vector faceNormal = edgeUnitNormal(n0, n1, cellNormal);
        Surface surface = new Surface(faceGeom.length() * 1.0, faceGeom.centroid(), faceNormal);

        return new Face(new Node[]{n0, n1}, cell.vtkType, surface, cell, null, cell.U.length);
    }

    private Vector edgeUnitNormal(Node n0, Node n1, Vector cellNormal) {
        Vector edgeTangent = new Vector(n0.location(), n1.location()).unit();
        return edgeTangent.cross(cellNormal).unit();
    }

    private Optional<Face> search(Node[] faceNodes, List<Face> faces) {
        for (Face face : faces) {
            if (sameNodes(faceNodes, face.nodes))
                return Optional.of(face);
        }

        return Optional.empty();
    }

    private boolean sameNodes(Node[] na1, Node[] na2) {
        if (na1.length != na2.length) return false;

        List<Node> nl1 = List.of(na1);
        List<Node> nl2 = List.of(na2);

        return nl1.containsAll(nl2);
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
