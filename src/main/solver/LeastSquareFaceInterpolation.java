package main.solver;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.*;
import main.util.DoubleMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.util.DoubleArray.divide;
import static main.util.DoubleArray.sum;

public class LeastSquareFaceInterpolation {
    private final Cell[][] neighbours;
    private final double[][][] inverseMatrix;
    private final Mesh mesh;
    private final Set<Node> boundaryIntersectionNodes;

    public LeastSquareFaceInterpolation(Mesh mesh) {
        this.mesh = mesh;
        this.boundaryIntersectionNodes = calculateCornerNodes(mesh);
        int totalNumFaces = mesh.internalFaces().size();
        totalNumFaces += mesh.boundaryStream()
                .mapToInt(b -> b.faces.size())
                .sum();

        this.neighbours = new Cell[totalNumFaces][];
        this.inverseMatrix = new double[totalNumFaces][][];
        mesh.internalFaceStream()
                .forEach(this::setup);
        mesh.boundaryStream()
                .flatMap(b -> b.faces.stream())
                .forEach(this::setup);
    }

    private Set<Node> calculateCornerNodes(Mesh mesh) {
        // For each boundary create a Set of nodes (including the corner nodes)
        List<Set<Node>> boundaryNodes = new ArrayList<>(mesh.boundaries().size());
        for (Boundary boundary : mesh.boundaries()) {
            Set<Node> bNodes = boundary.faces.stream()
                    .flatMap(f -> Arrays.stream(f.nodes))
                    .collect(Collectors.toSet());
            boundaryNodes.add(bNodes);
        }

        // For each set of boundary nodes, create a new set by subtracting all the other boundary sets
        // this will remove the corner nodes
        List<Set<Node>> nonCornerBoundaryNodes = new ArrayList<>(mesh.boundaries().size());
        for (Set<Node> nodesOnABoundary : boundaryNodes) {
            Set<Node> copyOfBoundaryNodes = new HashSet<>(nodesOnABoundary);
            for (Set<Node> nodesOnAnotherBoundary : boundaryNodes) {
                if (nodesOnAnotherBoundary == nodesOnABoundary) continue;
                copyOfBoundaryNodes.removeAll(nodesOnAnotherBoundary);
            }
            nonCornerBoundaryNodes.add(copyOfBoundaryNodes);
        }

        // Put together all the original sets
        Set<Node> allBoundaryNodes = boundaryNodes.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        // Subtract sets not-containing-the-corner-nodes from the complete collection
        for (Set<Node> bndNodes : nonCornerBoundaryNodes) {
            allBoundaryNodes.removeAll(bndNodes);
        }

        return allBoundaryNodes;
    }

    private void setup(Face face) {
        int index = face.index();
        Cell[] neighs = getNeighbours(face);
        this.neighbours[index] = neighs;

        this.inverseMatrix[index] = leastSquareMatrix(neighs, face.surface.centroid);
    }

    private double[][] leastSquareMatrix(Cell[] neighs, Point faceCentroid) {
        Vector[] distances = Arrays.stream(neighs)
                .map(neigh -> new Vector(faceCentroid, neigh.shape.centroid))
                .toArray(Vector[]::new);

        double[] weights = normalize(Arrays.stream(distances)
                .mapToDouble(this::weight)
                .toArray());

        RealMatrix A = new Array2DRowRealMatrix(neighs.length, 4);
        for (int i = 0; i < neighs.length; i++) {
            Vector r = distances[i].mult(weights[i]);
            A.setRow(i, new double[]{weights[i], r.x, r.y, r.z});
        }

        return invert(A).multiply(new DiagonalMatrix(weights))
                .getData();
    }

    private RealMatrix invert(RealMatrix matrix) {
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        return svd.getSolver().getInverse();
    }


    private double weight(Vector dr) {
        return 1.0 / dr.mag();
    }

    private double[] normalize(double[] positiveWeights) {
        return divide(positiveWeights, sum(positiveWeights));
    }

    Cell[] getNeighbours(Face face) {
        // In case of a boundary having only one face (highly unlikely), at least left and right cell will be added.
        return Stream.concat(Stream.of(face.left, face.right),
                Arrays.stream(face.nodes)
                        .filter(n -> !boundaryIntersectionNodes.contains(n))
                        .flatMap(n -> n.neighbors.stream()))
                .distinct()
                .toArray(Cell[]::new);
    }

    void setupAllFaces() {
        mesh.internalFaceStream().forEach(this::setFace);
        mesh.boundaryStream().flatMap(b -> b.faces.stream()).forEach(this::setFace);
    }

    private void setFace(Face face) {
        int numVars = face.U.length;
        for (int var = 0; var < numVars; var++) {
            setVar(face, var);
        }
    }

    private void setVar(Face face, int var) {
        int index = face.index();
        Cell[] neighs = neighbours[index];
        int numNeighs = neighs.length;
        double[] U = new double[numNeighs];

        for (int j = 0; j < numNeighs; j++) {
            U[j] = neighs[j].U[var];
        }

        double[] solution = DoubleMatrix.multiply(inverseMatrix[index], U);

        face.U[var] = solution[0];
        face.gradientU[var] = new Vector(solution[1], solution[2], solution[3]);
    }
}
