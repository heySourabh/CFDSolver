package main.solver;

import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;

import static main.util.DoubleArray.*;

public class DiffusionResidual implements ResidualCalculator {
    private final Mesh mesh;
    private final GoverningEquations govEqn;

    public DiffusionResidual(Mesh mesh, GoverningEquations govEqn) {
        this.mesh = mesh;
        this.govEqn = govEqn;
    }

    @Override
    public void updateCellResiduals() {
        // Assuming that the conservative variable gradients at the faces are already calculated

        // For all the internal faces set flux
        mesh.internalFaceStream()
                .forEach(this::setFlux);

        // For all the boundary faces set flux
        mesh.boundaryStream()
                .flatMap(b -> b.faces.stream())
                .forEach(this::setFlux);

        // For all cells add / subtract face flux
        mesh.cellStream().forEach(this::updateResidual);
    }

    private void setFlux(Face face) {
        double[] flux = govEqn.diffusion().flux(face.U, face.gradient, face.surface.unitNormal);
        copy(flux, face.flux);
    }

    private void updateResidual(Cell cell) {
        double[] totalResidual = new double[cell.residual.length];
        for (Face face : cell.faces) {
            double[] flux = multiply(face.flux, face.surface.area);
            if (face.left == cell) {
                decrement(totalResidual, flux);
            } else {
                increment(totalResidual, flux);
            }
        }

        increment(cell.residual, totalResidual);
    }
}
