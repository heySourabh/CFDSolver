package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;
import main.util.DoubleArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InviscidWallVOFBCTest {

    @Test
    public void setGhostCellValues() {
        double p = 542;
        double u = 6545;
        double v = 81;
        double w = 86;
        double C = 0.8;

        double rho1 = 841;
        double mu1 = 0.001;
        double rho2 = 20;
        double mu2 = 14;

        double beta = 654;

        Vector gravity = new Vector(54, 7, -78);

        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                rho1, mu1, rho2, mu2, gravity, beta);

        double[] conservativeVars = govEqn.conservativeVars(new double[]{p, u, v, w, C});

        BoundaryCondition bc = new InviscidWallVOFBC(govEqn);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        Cell ghost = new Cell(null, null, null, govEqn.numVars());
        Surface surface1 = new Surface(1.0, null, new Vector(1, 0, 0));
        Face face = new Face(null, null, surface1, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        bc.setGhostCellValues(face);

        double[] expectedGhostCellValues = govEqn.conservativeVars(new double[]{
                p, -u, v, w, C
        });

        assertArrayEquals(expectedGhostCellValues, face.right.U, 1e-15);


        Surface surface2 = new Surface(1.0, null, new Vector(0, 1, 0));
        face = new Face(null, null, surface2, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        bc.setGhostCellValues(face);

        expectedGhostCellValues = govEqn.conservativeVars(new double[]{
                p, u, -v, w, C
        });

        assertArrayEquals(expectedGhostCellValues, face.right.U, 1e-15);


        Surface surface3 = new Surface(1.0, null, new Vector(0, 0, 1));
        face = new Face(null, null, surface3, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        bc.setGhostCellValues(face);

        expectedGhostCellValues = govEqn.conservativeVars(new double[]{
                p, u, v, -w, C
        });

        assertArrayEquals(expectedGhostCellValues, face.right.U, 1e-15);
    }

    @Test
    public void convectiveFlux() {
        double p = 542;
        double u = 6545;
        double v = 81;
        double w = 86;
        double C = 0.8;

        double rho1 = 841;
        double mu1 = 0.001;
        double rho2 = 20;
        double mu2 = 14;

        double beta = 654;

        Vector gravity = new Vector(54, 7, -78);

        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                rho1, mu1, rho2, mu2, gravity, beta);

        double[] conservativeVars = govEqn.conservativeVars(new double[]{p, u, v, w, C});

        BoundaryCondition bc = new InviscidWallVOFBC(govEqn);
        Cell left = new Cell(null, null, null, govEqn.numVars());
        Cell ghost = new Cell(null, null, null, govEqn.numVars());
        Surface surface1 = new Surface(1.0, null, new Vector(1, 0, 0));
        Face face = new Face(null, null, surface1, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        double[] expectedFlux = {
                0, p, 0, 0, 0
        };

        double[] actualFlux = bc.convectiveFlux(face);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);


        Surface surface2 = new Surface(1.0, null, new Vector(0, 1, 0));
        face = new Face(null, null, surface2, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        expectedFlux = new double[]{
                0, 0, p, 0, 0
        };

        actualFlux = bc.convectiveFlux(face);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);


        Surface surface3 = new Surface(1.0, null, new Vector(0, 0, 1));
        face = new Face(null, null, surface3, left, ghost, govEqn.numVars());
        DoubleArray.copy(conservativeVars, face.left.U);

        expectedFlux = new double[]{
                0, 0, 0, p, 0
        };

        actualFlux = bc.convectiveFlux(face);
        assertArrayEquals(expectedFlux, actualFlux, 1e-15);
    }
}