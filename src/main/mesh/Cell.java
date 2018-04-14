package main.mesh;

import main.geom.VTKType;

import java.util.ArrayList;

public class Cell {
    public final int index;
    public final Node[] nodes;
    public final ArrayList<Face> faces;
    public final VTKType vtkType;
    public final double[] U;
    public final double[] residual;
    public final double volume;

    public Cell(int index, Node[] nodes, VTKType vtkType, double volume, int numVars) {
        this.index = index;
        this.nodes = nodes;
        this.faces = new ArrayList<>();
        this.vtkType = vtkType;
        this.volume = volume;

        this.U = new double[numVars];
        this.residual = new double[numVars];
    }
}
