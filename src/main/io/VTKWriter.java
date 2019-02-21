package main.io;

import main.mesh.Mesh;
import main.mesh.Node;
import main.physics.goveqn.GoverningEquations;
import vatika.data.*;
import vatika.writer.UnstructuredGridXmlVtKWriter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VTKWriter {
    private final Mesh mesh;
    private final Point[] points;
    private final GoverningEquations govEqn;
    private final Cell[] cells;

    public VTKWriter(Mesh mesh, GoverningEquations govEqn) {
        this.mesh = mesh;

        points = new Point[mesh.nodes().size()];
        this.govEqn = govEqn;
        for (int i = 0; i < points.length; i++) {
            Node n = mesh.nodes().get(i);
            points[i] = new Point(n.x, n.y, n.z);
        }

        Map<Node, Integer> nodeIndexing = new HashMap<>();
        for (int i = 0; i < mesh.nodes().size(); i++) {
            nodeIndexing.put(mesh.nodes().get(i), i);
        }

        cells = new Cell[mesh.cells().size()];
        for (int i = 0; i < cells.length; i++) {
            main.mesh.Cell cell = mesh.cells().get(i);
            int[] conn = new int[cell.nodes.length];
            for (int n = 0; n < conn.length; n++) {
                conn[n] = nodeIndexing.get(cell.nodes[n]);
            }
            cells[i] = new Cell(conn, VTKType.get(cell.vtkType.ID));
        }
    }

    public void write(File file) {

        double[][] primVarsCache = new double[cells.length][];
        for (int iCell = 0; iCell < cells.length; iCell++) {
            primVarsCache[iCell] = govEqn.primitiveVars(mesh.cells().get(iCell).U);
        }
        String[] primVarNames = govEqn.primitiveVarNames();

        ScalarData[] cellScalarData = new ScalarData[govEqn.numVars()];

        for (int var = 0; var < govEqn.numVars(); var++) {
            double[] scalars = new double[cells.length];
            for (int iCell = 0; iCell < cells.length; iCell++) {
                scalars[iCell] = primVarsCache[iCell][var];
            }
            cellScalarData[var] = new ScalarData(primVarNames[var], scalars);
        }

        UnstructuredGrid gridData = new UnstructuredGrid(points, cells,
                null, null,
                cellScalarData, null);
        UnstructuredGridXmlVtKWriter writer = new UnstructuredGridXmlVtKWriter(gridData);
        try {
            writer.write(file);
        } catch (Exception e) {
            System.out.println("Unable to write output file: " + file);
        }
    }
}
