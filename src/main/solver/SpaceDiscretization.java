package main.solver;

import main.mesh.Boundary;
import main.mesh.Mesh;
import main.physics.bc.BoundaryCondition;

import java.util.Arrays;
import java.util.List;

public class SpaceDiscretization {

    private final Mesh mesh;
    private final List<ResidualCalculator> residuals;
    private final LeastSquareFaceInterpolation faceInterpolation;
    private final CellGradientCalculator cellGradientCalculator;

    public SpaceDiscretization(Mesh mesh, CellGradientCalculator cellGradientCalculator, List<ResidualCalculator> residuals) {
        this.mesh = mesh;
        this.cellGradientCalculator = cellGradientCalculator;
        this.residuals = residuals;
        this.faceInterpolation = new LeastSquareFaceInterpolation(mesh);
    }

    public void setResiduals() {
        setGhostCellValues();
        faceInterpolation.setupAllFaces();
        cellGradientCalculator.setupAllCells();

        mesh.cellStream().forEach(cell -> Arrays.fill(cell.residual, 0.0));
        residuals.forEach(ResidualCalculator::updateCellResiduals);
    }

    private void setGhostCellValues() {
        mesh.boundaryStream().forEach(this::setGhostCellValues);
    }

    private void setGhostCellValues(Boundary boundary) {
        BoundaryCondition bc = boundary.bc().orElseThrow(
                () -> new IllegalArgumentException("Boundary condition is not defined for \"" + boundary.name + "\"."));
        boundary.faces.forEach(bc::setGhostCellValues);
    }
}
