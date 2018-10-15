package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.util.DoubleArray;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class WallBCTest {

    @Test
    public void setGhostCellValues_stationary_wall() {
        ArtificialCompressibilityEquations govEqn =
                new ArtificialCompressibilityEquations(2.6, 1e-4, new Vector(-1, -8, 7));
        Cell insideCell = new Cell(null, null, null, govEqn.numVars());
        Cell ghostCell = new Cell(null, null, null, govEqn.numVars());
        Vector faceNormal = new Vector(1, 5, 5).unit();
        Face face = new Face(null, null, new Surface(1.5, null, faceNormal),
                insideCell, ghostCell, govEqn.numVars());

        double p = 4654;
        double u = 15;
        double v = 45;
        double w = 87;

        double beta = 1.0;

        DoubleArray.copy(new double[]{p / beta, u, v, w}, insideCell.U);

        double[] expectedU = {p / beta, -u, -v, -w};

        WallBC bc = new WallBC(govEqn, new Vector(0, 0, 0));
        bc.setGhostCellValues(face);

        assertArrayEquals(expectedU, ghostCell.U, 1e-15);
    }

    @Test
    public void setGhostCellValues_moving_wall() {
        ArtificialCompressibilityEquations govEqn =
                new ArtificialCompressibilityEquations(2.6, 1e-4, new Vector(-1, -8, 7));
        Cell insideCell = new Cell(null, null, null, govEqn.numVars());
        Cell ghostCell = new Cell(null, null, null, govEqn.numVars());
        Vector faceNormal = new Vector(1, 5, 5).unit();
        Face face = new Face(null, null, new Surface(1.5, null, faceNormal),
                insideCell, ghostCell, govEqn.numVars());

        double p = 4654;
        double u = 15;
        double v = 45;
        double w = 20;

        double beta = 1.0;

        DoubleArray.copy(new double[]{p / beta, u, v, w}, insideCell.U);

        Vector wallVelocity = new Vector(-1, 4, 0);

        double[] expectedU = {p / beta, -17, -37, -20};

        WallBC bc = new WallBC(govEqn, wallVelocity);
        bc.setGhostCellValues(face);

        assertArrayEquals(expectedU, ghostCell.U, 1e-15);
    }

    @Test
    public void convectiveFlux() {
        ArtificialCompressibilityEquations govEqn =
                new ArtificialCompressibilityEquations(2.6, 1e-4, new Vector(-1, -8, 7));
        Cell insideCell = new Cell(null, null, null, govEqn.numVars());
        Cell ghostCell = new Cell(null, null, null, govEqn.numVars());
        Vector faceNormal = new Vector(1, 5, 5).unit();
        Face face = new Face(null, null, new Surface(1.5, null, faceNormal),
                insideCell, ghostCell, govEqn.numVars());

        double p = 4654;
        double u = 15;
        double v = 45;
        double w = 20;

        double beta = 1.0;

        DoubleArray.copy(new double[]{p / beta, u, v, w}, insideCell.U);

        Vector wallVelocity = new Vector(-1, 4, 0);
        WallBC bc = new WallBC(govEqn, wallVelocity);

        double[] expectedFlux = {
                -1 * faceNormal.x + 4 * faceNormal.y,
                (1 + 4654 / 2.6) * faceNormal.x + (-4) * faceNormal.y,
                (-4) * faceNormal.x + (16 + 4654 / 2.6) * faceNormal.y,
                4654 / 2.6 * faceNormal.z
        };

        assertArrayEquals(expectedFlux, bc.convectiveFlux(face), 1e-12);
    }
}
