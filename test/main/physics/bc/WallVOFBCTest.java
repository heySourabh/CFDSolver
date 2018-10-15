package main.physics.bc;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;
import main.util.DoubleArray;
import org.junit.Test;

import static org.junit.Assert.*;

public class WallVOFBCTest {

    @Test
    public void setGhostCellValues() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                100, 200, 1e-5, 5e-4, new Vector(1, 2, 3));

        Vector wallVelocity = new Vector(1, -4, 5);
        double[] insidePrimitiveVars = {
                101325, 1.0, 3.0, 2.0, 0.75
        };
        double[] insideConservativeVars = govEqn.conservativeVars(insidePrimitiveVars);
        Cell insideCell = new Cell(null, null, null, govEqn.numVars());
        DoubleArray.copy(insideConservativeVars, insideCell.U);
        Cell ghostCell = new Cell(null, null, null, govEqn.numVars());
        Face face = new Face(null, null, null, insideCell, ghostCell, govEqn.numVars());
        WallVOFBC bc = new WallVOFBC(govEqn, wallVelocity);

        bc.setGhostCellValues(face);

        double[] expectedGhostCellU = govEqn.conservativeVars(
                new double[]{101325, 1.0, -11, 8.0, 0.75});

        assertArrayEquals(expectedGhostCellU, face.right.U, 1e-15);
    }

    @Test
    public void convectiveFlux() {
        ArtificialCompressibilityVOFEquations govEqn = new ArtificialCompressibilityVOFEquations(
                100, 200, 1e-5, 5e-4, new Vector(1, 2, 3));

        Vector wallVelocity = new Vector(1, -4, 5);
        double pi = 1254;
        double ui = 1.0;
        double vi = 3.0;
        double wi = 2.0;
        double Ci = 0.75;
        double[] insidePrimitiveVars = {
                pi, ui, vi, wi, Ci
        };
        double[] insideConservativeVars = govEqn.conservativeVars(insidePrimitiveVars);
        Cell insideCell = new Cell(null, null, null, govEqn.numVars());
        DoubleArray.copy(insideConservativeVars, insideCell.U);
        Cell ghostCell = new Cell(null, null, null, govEqn.numVars());
        Vector n = new Vector(7, 9, 2).unit();
        Surface surface = new Surface(2.5, new Point(0, 9, 0), n);
        Face face = new Face(null, null, surface, insideCell, ghostCell, govEqn.numVars());
        WallVOFBC bc = new WallVOFBC(govEqn, wallVelocity);

        double rhoi = govEqn.rho(Ci);
        double u = wallVelocity.x;
        double v = wallVelocity.y;
        double w = wallVelocity.z;
        double[] F = {
                u, rhoi * u * u + pi, rhoi * u * v, rhoi * u * w, u * Ci
        };
        double[] G = {
                v, rhoi * u * v, rhoi * v * v + pi, rhoi * v * w, v * Ci
        };
        double[] H = {
                w, rhoi * u * w, rhoi * v * w, rhoi * w * w + pi, w * Ci
        };

        double[] expectedFlux = new double[govEqn.numVars()];
        for (int var = 0; var < expectedFlux.length; var++) {
            expectedFlux[var] = F[var] * n.x + G[var] * n.y + H[var] * n.z;
        }

        assertArrayEquals(expectedFlux, bc.convectiveFlux(face), 1e-12);
    }
}