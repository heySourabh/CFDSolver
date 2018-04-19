package main.mesh;

import main.physics.BoundaryCondition;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Mesh {

    private final ArrayList<Cell> cells;
    private final ArrayList<Face> internalFaces;
    private final ArrayList<Node> nodes;
    private final ArrayList<Boundary> boundaries;

    Mesh(MeshData meshData, BoundaryCondition[] bc) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * @return List of Cell objects inside the domain (does not include ghost cells).
     */
    public ArrayList<Cell> cells() {
        return cells;
    }

    public Stream<Cell> cellStream() {
        return cells().stream();
    }

    /**
     * @return List of internal faces (does not include boundary faces)
     */
    public ArrayList<Face> internalFaces() {
        return internalFaces;
    }

    public Stream<Face> internalFaceStream() {
        return internalFaces().stream();
    }

    /**
     * @return Node objects which are on the boundary and inside the domain
     * (does not include the nodes outside the domain - the outside nodes of the ghost cells)
     */
    public ArrayList<Node> nodes() {
        return nodes;
    }

    public Stream<Node> nodeStream() {
        return nodes().stream();
    }

    /**
     * @return Boundary objects, which contains the boundary condition and faces which make up the boundary.
     */
    public ArrayList<Boundary> boundaries() {
        return boundaries;
    }

    public Stream<Boundary> boundaryStream() {
        return boundaries().stream();
    }
}
