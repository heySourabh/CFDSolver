package main.solver.convection;

import main.mesh.Boundary;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.physics.bc.BoundaryCondition;
import main.solver.ResidualCalculator;
import main.solver.convection.riemann.RiemannSolver;
import main.solver.convection.reconstructor.SolutionReconstructor;

import static main.util.DoubleArray.*;

public class ConvectionResidual implements ResidualCalculator {
    private final SolutionReconstructor reconstructor;
    private final RiemannSolver riemannSolver;
    private final Mesh mesh;

    public ConvectionResidual(SolutionReconstructor reconstructor, RiemannSolver riemannSolver, Mesh mesh) {
        this.reconstructor = reconstructor;
        this.riemannSolver = riemannSolver;
        this.mesh = mesh;
    }

    @Override
    public void updateCellResiduals() {
        // solution reconstruction for all cells
        reconstructor.reconstruct();

        // Calculate the flux at the internal faces and save
        mesh.internalFaceStream().forEach(this::setFlux);

        // Calculate the flux at the boundary faces and save
        mesh.boundaryStream().forEach(this::setFlux);

        // add / subtract (flux * face.area) to the cells residual
        mesh.cellStream().forEach(this::updateResidual);
    }

    private void updateResidual(Cell cell) {
        int numVars = cell.residual.length;
        double[] totalResidual = new double[numVars];
        for (Face face : cell.faces) {
            double[] flux = multiply(face.flux, face.surface.area);
            if (face.left == cell) {
                increment(totalResidual, flux);
            } else {
                decrement(totalResidual, flux);
            }
        }

        increment(cell.residual, totalResidual);
    }

    private void setFlux(Boundary boundary) {
        BoundaryCondition bc = boundary.bc().orElseThrow(
                () -> new IllegalArgumentException("Boundary condition is not defined."));
        boundary.faces.forEach(bFace -> setFlux(bFace, bc));
    }

    private void setFlux(Face face, BoundaryCondition bc) {
        double[] flux = bc.convectiveFlux(face);
        copy(flux, face.flux);
    }

    private void setFlux(Face face) {
        double[] UL = reconstructor.conservativeVars(face.left, face.surface.centroid);
        double[] UR = reconstructor.conservativeVars(face.right, face.surface.centroid);
        double[] flux = riemannSolver.flux(UL, UR, face.surface);
        copy(flux, face.flux);
    }
}
