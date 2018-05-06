package main.mesh;

import main.geom.VTKType;

public class Face {
    public final Node[] nodes;
    public final VTKType vtkType;
    public final Surface surface;
    public final Cell left, right;

    public final double[] flux;

    public Face(Node[] nodes, VTKType vtkType, Surface surface, Cell left, Cell right, int numVars) {
        this.nodes = nodes;
        this.vtkType = vtkType;
        this.surface = surface;
        this.left = left;
        this.right = right;

        this.flux = new double[numVars];
    }
}
