package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.util.DoubleArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class VelocityInletBCTest {

    @Test
    public void setGhostCellValues() {
        ArtificialCompressibilityEquations govEqn = new ArtificialCompressibilityEquations(5.6, 4e-4,
                new Vector(-4, -9.2, 7));

        double bnd_u = -78;
        double bnd_v = 5;
        double bnd_w = 20;

        VelocityInletBC bc = new VelocityInletBC(govEqn, new Vector(bnd_u, bnd_v, bnd_w));

        Cell left = new Cell(null, null, null, govEqn.numVars());
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face face = new Face(null, null, null, left, right, govEqn.numVars());

        double pi = 5;
        double ui = 54;
        double vi = -80.2;
        double wi = 784;

        double beta = 1.0;

        double ghost_u = 2 * bnd_u - ui;
        double ghost_v = 2 * bnd_v - vi;
        double ghost_w = 2 * bnd_w - wi;

        double[] expectedGhostConservativeVars = {
                pi / beta, ghost_u, ghost_v, ghost_w
        };

        double[] insideConservativeVars = {
                pi / beta, ui, vi, wi
        };
        DoubleArray.copy(insideConservativeVars, face.left.U);
        bc.setGhostCellValues(face);

        assertArrayEquals(expectedGhostConservativeVars, face.right.U, 1e-15);
    }

    @Test
    public void convectiveFlux() {
        ArtificialCompressibilityEquations govEqn = new ArtificialCompressibilityEquations(5.6, 4e-4,
                new Vector(-4, -9.2, 7));

        double bnd_u = -78;
        double bnd_v = 5;
        double bnd_w = 20;

        VelocityInletBC bc = new VelocityInletBC(govEqn, new Vector(bnd_u, bnd_v, bnd_w));

        Surface surface = new Surface(654.0, null, new Vector(4, -1, .25).unit());

        Cell left = new Cell(null, null, null, govEqn.numVars());
        Cell right = new Cell(null, null, null, govEqn.numVars());
        Face face = new Face(null, null, surface, left, right, govEqn.numVars());

        double pi = 5;
        double ui = 54;
        double vi = -80.2;
        double wi = 784;

        double beta = 1.0;

        double[] expectedFlux = govEqn.convection()
                .flux(new double[]{pi / beta, bnd_u, bnd_v, bnd_w}, surface.unitNormal());

        double[] insideConservativeVars = {
                pi / beta, ui, vi, wi
        };
        DoubleArray.copy(insideConservativeVars, face.left.U);

        assertArrayEquals(expectedFlux, bc.convectiveFlux(face), 1e-15);
    }
}