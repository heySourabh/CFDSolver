package main.mesh;

import java.util.List;
import java.util.stream.Stream;

public interface Mesh {
    /**
     * @return List of Cell objects inside the domain (does not include ghost cells).
     */
    List<Cell> cells();

    default Stream<Cell> cellStream() {
        return cells().stream();
    }

    /**
     * @return List of internal faces (does not include boundary faces)
     */
    List<Face> internalFaces();

    default Stream<Face> internalFaceStream() {
        return internalFaces().stream();
    }

    /**
     * @return Node objects which are on the boundary and inside the domain
     * (does not include the nodes outside the domain - the outside nodes of the ghost cells)
     */
    List<Node> nodes();

    default Stream<Node> nodeStream() {
        return nodes().stream();
    }

    /**
     * @return Boundary objects, which contains the boundary condition and faces which make up the boundary.
     */
    List<Boundary> boundaries();

    default Stream<Boundary> boundaryStream() {
        return boundaries().stream();
    }
}
