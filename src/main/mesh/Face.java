package main.mesh;

import main.geom.VTKType;
import main.geom.Vector;

import java.util.Arrays;
import java.util.List;

public class Face {
    public final Node[] nodes;
    public final VTKType vtkType;
    public final Surface surface;
    public final Cell left;
    public Cell right;

    public final double[] flux;

    public double maxAbsEigenvalue;

    public Face(Node[] nodes, VTKType vtkType, Surface surface, Cell left, Cell right, int numVars) {
        this.nodes = nodes;
        this.vtkType = vtkType;
        this.surface = surface;

        if (left == null) throw new IllegalArgumentException("The left neighbor cannot be null");
        this.left = left;
        this.right = right;

        this.flux = new double[numVars];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Face otherFace = (Face) o;
        Face thisFace = this;

        boolean same = hasSameNodes(thisFace.nodes, otherFace.nodes);
        if (same) {
            // Set the right nodes from the other face
            thisFace.right = otherFace.left;
            otherFace.right = thisFace.left;

            // Average of the normal from the other face (subtract since it is pointing in opposite direction)
            thisFace.surface.unitNormal = thisFace.surface.unitNormal
                    .sub(otherFace.surface.unitNormal)
                    .mult(0.5).unit();
            otherFace.surface.unitNormal = otherFace.surface.unitNormal
                    .sub(thisFace.surface.unitNormal)
                    .mult(0.5).unit();
        }

        return same;
    }

    @Override
    public int hashCode() {
        return Arrays.stream(this.nodes)
                .mapToInt(Object::hashCode)
                .sum();
    }

    private boolean hasSameNodes(Node[] a1, Node[] a2) {
        List<Node> l1 = Arrays.asList(a1);
        List<Node> l2 = Arrays.asList(a2);

        if (l1.size() != l2.size()) return false;
        return l1.containsAll(l2);
    }
}
