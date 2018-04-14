package main.mesh;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;

public class Face {
    public final Node[] nodes;
    public final VTKType vtkType;
    public final double area;
    public final Point[] integrationPoints;
    public final Vector[] unitNormals;
    public final double[][] flux;

    public Face(Node[] nodes, VTKType vtkType, double area,
                Point[] integrationPoints, Vector[] unitNormals, int numVars) {
        this.nodes = nodes;
        this.vtkType = vtkType;
        this.area = area;
        this.integrationPoints = integrationPoints;
        this.unitNormals = unitNormals;
        this.flux = new double[integrationPoints.length][numVars];
    }
}
