package main.solver;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;

public class GreenGaussCellGradient implements CellGradientCalculator {
    @Override
    public Vector[] forCell(Cell cell) {
        // Assuming that face average U is calculated (face.U)
        int numVars = cell.U.length;

        Vector[] gradients = new Vector[numVars];
        for (int var = 0; var < numVars; var++) {
            gradients[var] = new Vector(0, 0, 0);
        }

        for (Face face : cell.faces) {
            double area = face.surface.area;
            Vector unitNormal = face.surface.unitNormal.mult(face.left == cell ? 1 : -1);
            Vector projectedArea = unitNormal.mult(area);
            for (int var = 0; var < numVars; var++) {
                gradients[var] = gradients[var].add(projectedArea.mult(face.U[var]));
            }
        }

        double volume = cell.shape.volume;
        for (int var = 0; var < numVars; var++) {
            gradients[var] = gradients[var].mult(1.0 / volume);
        }

        return gradients;
    }
}
