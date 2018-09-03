package main.solver;

import main.mesh.Boundary;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.physics.bc.BoundaryCondition;
import main.solver.reconstructor.SolutionReconstructor;
import main.util.DoubleArray;

import static main.util.DoubleArray.*;

public class ConvectiveResidual implements ResidualCalculator {
    private final SolutionReconstructor reconstructor;
    private final RiemannSolver riemannSolver;
    private final Mesh mesh;

    public ConvectiveResidual(SolutionReconstructor reconstructor, RiemannSolver riemannSolver, Mesh mesh) {
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
        mesh.cellStream().forEach(this::updateCellResidual);
    }

    private void updateCellResidual(Cell cell) {
        int numVars = cell.residual.length;
        // Add flux from faces with normal pointing outward
        double[] totalResidual = cell.faces.stream()
                .filter(face -> face.left == cell)
                .map(face -> multiply(face.flux, face.surface.area))
                .reduce(zeros(numVars), DoubleArray::add);
        increment(cell.residual, totalResidual);

        // Subtract flux from faces with normal pointing inward
        totalResidual = cell.faces.stream()
                .filter(face -> face.right == cell)
                .map(face -> multiply(face.flux, face.surface.area))
                .reduce(zeros(numVars), DoubleArray::add);
        decrement(cell.residual, totalResidual);
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
        double[] flux = riemannSolver.flux(UL, UR, face.surface.unitNormal);
        copy(flux, face.flux);
    }
}
