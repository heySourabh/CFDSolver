package main.mesh;

import main.geom.VTKType;

import java.util.ArrayList;
import java.util.Arrays;

public class Cell {
    /**
     * This value must be equal to the index in Mesh.cells() List.
     * This value must be equal to -1 for ghost cells.
     */
    public final int index;
    public final Node[] nodes;
    public final ArrayList<Face> faces;
    public final VTKType vtkType;
    public final Shape shape;

    public final double[] U;
    public final double[] residual;
    public final double[][] reconstructCoeffs;

    /**
     * Local time step or pseudo-time step
     */
    public double dt;

    public Cell(int index, Node[] nodes, VTKType vtkType, Shape shape, int numVars) {
        this.index = index;
        this.nodes = nodes;
        this.faces = new ArrayList<>();
        this.vtkType = vtkType;
        this.shape = shape;

        this.U = new double[numVars];
        this.residual = new double[numVars];
        this.reconstructCoeffs = new double[numVars][]; // The reconstructor can decide the length
    }

    @Override
    public String toString() {
        return "Cell{" +
                "\nindex=" + index +
                "\nnodes=" + (nodes == null ? null : Arrays.asList(nodes)) +
                "\nfaces=" + faces +
                "\nvtkType=" + vtkType +
                "\nshape=" + shape +
                "\n}";
    }
}
