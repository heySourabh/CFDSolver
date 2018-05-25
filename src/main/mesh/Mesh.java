package main.mesh;

import java.util.ArrayList;
import java.util.Arrays;
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

    default String stringify() {
        String str = "";
        str += "Number of nodes: " + nodes().size() + "\n";
        str += "Number of cells: " + cells().size() + "\n";
        str += "Number of internal faces: " + internalFaces().size() + "\n";
        str += "Number of Boundies: " + boundaries().size() + "\n";
        List<String> boundaryInfo = new ArrayList<>();
        for (Boundary boundary : boundaries()) {
            String boundaryString = Arrays
                    .asList("Name: " + boundary.name,
                            "Faces: " + boundary.faces.size(),
                            "Type: " + boundary.bc.getClass().getSimpleName())
                    .toString();
            boundaryInfo.add("    " + boundaryString);
        }
        str += String.join("\n", boundaryInfo);

        return str;
    }
}
