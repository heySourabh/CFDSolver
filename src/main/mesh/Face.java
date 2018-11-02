package main.mesh;

import main.geom.VTKType;
import main.geom.Vector;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Face {
    private int index;

    public final Node[] nodes;
    public final VTKType vtkType;
    public final Surface surface;
    public final Cell left;
    public Cell right;

    public final double[] U;
    public final Vector[] gradientU;
    public final double[] flux;

    public double maxAbsEigenvalue;

    public Face(Node[] nodes, VTKType vtkType, Surface surface, Cell left, Cell right, int numVars) {
        this.index = -1;
        this.nodes = nodes;
        this.vtkType = vtkType;
        this.surface = surface;

        this.left = requireNonNull(left, "The left neighbor cannot be null");
        this.right = right;

        this.U = new double[numVars];
        this.gradientU = new Vector[numVars];
        this.flux = new double[numVars];
    }

    public void setIndex(int index) {
        if (this.index == -1) {
            this.index = index;
        } else {
            throw new IllegalStateException("The index can be set only once.");
        }
    }

    public int index() {
        return this.index;
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
            Vector avgNormal = thisFace.surface.unitNormal()
                    .sub(otherFace.surface.unitNormal())
                    .unit();
            thisFace.surface.setUnitNormal(avgNormal);
            otherFace.surface.setUnitNormal(avgNormal.mult(-1));
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

    @Override
    public String toString() {
        return "Face{" +
                "vtkType=" + vtkType +
                ", surface=" + surface +
                '}';
    }
}
