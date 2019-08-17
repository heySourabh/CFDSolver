package main.mesh;

import main.geom.VTKType;
import main.geom.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class Cell {
    /**
     * This value must be equal to the index in Mesh.cells() List.
     * This value must be equal to -1 for ghost cells.
     */
    private int index;
    public final Node[] nodes;
    public final ArrayList<Face> faces;
    public final VTKType vtkType;
    public final Shape shape;

    public final double[] U;
    public final Vector[] gradientU;

    public final double[] Wn;
    public final double[] Wnm1;
    public final double[] Wnm2;
    public final double[] residual;
    public final double[][] reconstructCoeffs;

    /**
     * Local time step or pseudo-time step
     */
    public double dt;

    public Cell(Node[] nodes, VTKType vtkType, Shape shape, int numVars) {
        this.index = -1;
        this.nodes = nodes;
        this.faces = new ArrayList<>();
        this.vtkType = vtkType;
        this.shape = shape;

        this.U = new double[numVars];
        this.gradientU = new Vector[numVars];
        for (int var = 0; var < numVars; var++) {
            this.gradientU[var] = new Vector(0, 0, 0);
        }

        this.Wn = new double[numVars];
        this.Wnm1 = new double[numVars];
        this.Wnm2 = new double[numVars];

        this.residual = new double[numVars];
        this.reconstructCoeffs = new double[numVars][]; // The reconstructor can decide the length
    }

    public int index() {
        return index;
    }

    public void setIndex(int index) {
        if (this.index != -1) {
            throw new IllegalStateException("The index of a cell can be set only once.");
        }
        this.index = index;
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
