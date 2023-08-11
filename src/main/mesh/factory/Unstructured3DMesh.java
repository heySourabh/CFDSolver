package main.mesh.factory;

import main.geom.Geometry;
import main.geom.VTKType;
import main.geom.factory.*;
import main.io.DataFileReader;
import main.mesh.*;
import main.physics.bc.BoundaryCondition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Unstructured3DMesh implements Mesh {
    private final List<Cell> cells;
    private final List<Face> internalFaces;
    private final List<Node> nodes;
    private final List<Boundary> boundaries;

    public Unstructured3DMesh(File meshFile, int numVars, Map<String, BoundaryCondition> bcs) throws FileNotFoundException {
        try (DataFileReader meshFileReader = new DataFileReader(meshFile, "%")) {
            int dim = meshFileReader.readIntParameter("dimension");
            if (dim != 3) {
                throw new IllegalArgumentException("The mesh file dimension must be 3.");
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
                for (int ni = 0; ni < cellNodes.length; ni++) {
                    cellNodes[ni] = nodes.get(connectivity[ni + 1]);
                }

                if (vtkType.dim != 3) {
                    throw new IllegalArgumentException("The elements are not 3D.");
                }

                CellAndFaces cellAndFaces = createNewCellAndFaces(vtkType, cellNodes);
                cellList.add(cellAndFaces.cell);
                faceSet.addAll(cellAndFaces.faces);
            }
            this.cells = List.copyOf(cellList);
            setAllCellIndices();

            List<Face> allBoundaryFaces = faceSet.stream()
                    .filter(f -> f.right == null)
                    .toList();

            this.internalFaces = List.copyOf(faceSet.stream()
                    .filter(f -> f.right != null)
                    .toList());

            int numBoundaries = meshFileReader.readIntParameter("boundaries");
            Boundary[] boundaryArray = new Boundary[numBoundaries];
            for (int bi = 0; bi < numBoundaries; bi++) {
                String bndName = meshFileReader.readParameter("bname");
                int numBndFaces = meshFileReader.readIntParameter("bfaces");
                List<Face> bndFaces = new ArrayList<>();
                for (int fi = 0; fi < numBndFaces; fi++) {
                    int[] connectivity = meshFileReader.readIntArray();
                    VTKType vtkType = VTKType.get(connectivity[0]);
                    if (vtkType.dim != 2) {
                        throw new UnsupportedOperationException("Face dimension must be 2D. The geometry type " + vtkType + " is not supported.");
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

            setAllFaceIndices();
        }

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

    private CellAndFaces createNewCellAndFaces(VTKType vtkType, Node[] nodes) {
        return switch (vtkType) {
            case VTK_TETRA -> createNewTetraCellAndFaces(nodes);
            case VTK_HEXAHEDRON -> createNewHexahedronCellAndFaces(nodes);
            case VTK_WEDGE -> createNewWedgeCellAndFaces(nodes);
            case VTK_PYRAMID -> createNewPyramidCellAndFaces(nodes);
            default -> throw new UnsupportedOperationException("Cell: The geometry type "
                    + vtkType + " is not supported.");
        };
    }

    private CellAndFaces createNewTetraCellAndFaces(Node[] nodes) {
        Geometry geom = new Tetra(nodes[0].location(), nodes[1].location(), nodes[2].location(), nodes[3].location());
        Shape shape = new Shape(geom.volume(), geom.centroid());
        Cell cell = new Cell(nodes, geom.vtkType(), shape, nodes[0].U.length);

        List<Face> faces = List.of(
                createPlanarFace(cell, nodes[0], nodes[2], nodes[1]),
                createPlanarFace(cell, nodes[0], nodes[1], nodes[3]),
                createPlanarFace(cell, nodes[0], nodes[3], nodes[2]),
                createPlanarFace(cell, nodes[1], nodes[2], nodes[3])
        );
        return new CellAndFaces(cell, faces);
    }

    private CellAndFaces createNewHexahedronCellAndFaces(Node[] nodes) {
        Geometry geom = new Hexahedron(nodes[0].location(), nodes[1].location(), nodes[2].location(), nodes[3].location(),
                nodes[4].location(), nodes[5].location(), nodes[6].location(), nodes[7].location());
        Shape shape = new Shape(geom.volume(), geom.centroid());
        Cell cell = new Cell(nodes, geom.vtkType(), shape, nodes[0].U.length);

        List<Face> faces = List.of(
                createPlanarFace(cell, nodes[0], nodes[4], nodes[7], nodes[3]),
                createPlanarFace(cell, nodes[1], nodes[2], nodes[6], nodes[5]),
                createPlanarFace(cell, nodes[0], nodes[1], nodes[5], nodes[4]),
                createPlanarFace(cell, nodes[2], nodes[3], nodes[7], nodes[6]),
                createPlanarFace(cell, nodes[0], nodes[3], nodes[2], nodes[1]),
                createPlanarFace(cell, nodes[4], nodes[5], nodes[6], nodes[7])
        );
        return new CellAndFaces(cell, faces);
    }

    private CellAndFaces createNewWedgeCellAndFaces(Node[] nodes) {
        Geometry geom = new Wedge(nodes[0].location(), nodes[1].location(), nodes[2].location(),
                nodes[3].location(), nodes[4].location(), nodes[5].location());
        Shape shape = new Shape(geom.volume(), geom.centroid());
        Cell cell = new Cell(nodes, geom.vtkType(), shape, nodes[0].U.length);

        List<Face> faces = List.of(
                createPlanarFace(cell, nodes[0], nodes[1], nodes[2]),
                createPlanarFace(cell, nodes[3], nodes[5], nodes[4]),
                createPlanarFace(cell, nodes[0], nodes[2], nodes[5], nodes[3]),
                createPlanarFace(cell, nodes[1], nodes[4], nodes[5], nodes[2]),
                createPlanarFace(cell, nodes[0], nodes[3], nodes[4], nodes[1])
        );
        return new CellAndFaces(cell, faces);
    }

    private CellAndFaces createNewPyramidCellAndFaces(Node[] nodes) {
        Geometry geom = new Pyramid(nodes[0].location(), nodes[1].location(), nodes[2].location(),
                nodes[3].location(), nodes[4].location());
        Shape shape = new Shape(geom.volume(), geom.centroid());
        Cell cell = new Cell(nodes, geom.vtkType(), shape, nodes[0].U.length);

        List<Face> faces = List.of(
                createPlanarFace(cell, nodes[0], nodes[3], nodes[2], nodes[1]),
                createPlanarFace(cell, nodes[0], nodes[1], nodes[4]),
                createPlanarFace(cell, nodes[1], nodes[2], nodes[4]),
                createPlanarFace(cell, nodes[2], nodes[3], nodes[4]),
                createPlanarFace(cell, nodes[3], nodes[0], nodes[4])
        );
        return new CellAndFaces(cell, faces);
    }

    /**
     * Save the cell & its faces.
     *
     * @param cell  The cell
     * @param faces the faces are outward facing as per VTK convention (i.e. normal pointing outside the cell)
     */
    private record CellAndFaces(Cell cell, List<Face> faces) {
    }

    private Face createPlanarFace(Cell cell, Node... faceNodes) {
        Geometry faceGeom = switch (faceNodes.length) {
            case 3 -> new Triangle(faceNodes[0].location(), faceNodes[1].location(), faceNodes[2].location());
            case 4 -> new Quad(faceNodes[0].location(), faceNodes[1].location(),
                    faceNodes[2].location(), faceNodes[3].location());
            default -> throw new UnsupportedOperationException("Face: The geometry type with "
                    + faceNodes.length + " nodes is not supported.");
        };

        Surface surface = new Surface(faceGeom.area(), faceGeom.centroid(), faceGeom.unitNormal());
        return new Face(faceNodes, faceGeom.vtkType(), surface, cell, null, cell.U.length);
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
