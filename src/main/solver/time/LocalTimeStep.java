package main.solver.time;

import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;

public class LocalTimeStep implements TimeStep {

    private final Mesh mesh;
    private final GoverningEquations govEqn;

    public LocalTimeStep(Mesh mesh, GoverningEquations govEqn) {
        this.mesh = mesh;
        this.govEqn = govEqn;
    }

    @Override
    public void updateCellTimeSteps(double courantNum, double timeStepLimit) {
        // Go through all the internal faces and save maxAbsEigenvalue
        mesh.internalFaceStream()
                .forEach(this::updateEigenvalue);

        // Go through all the boundary faces and save maxAbsEigenvalue
        mesh.boundaryStream().flatMap(b -> b.faces.stream())
                .forEach(this::updateEigenvalue);

        // Go through all the cells, calculate spectral radius due to convection and diffusion
        // and save the time step scaled by Courant number
        mesh.cellStream()
                .forEach(cell -> updateTimeStep(cell, courantNum, timeStepLimit));
    }

    private void updateTimeStep(Cell cell, double courantNum, double timeStepLimit) {
        // Convection spectral radius
        double spectralRadiusConvection = 0.0;
        for (Face face : cell.faces) {
            spectralRadiusConvection += face.maxAbsEigenvalue * face.surface.area;
        }

        // Diffusion spectral radius
        double spectralRadiusDiffusion = 0.0;
        for (Face face : cell.faces) {
            // Assuming that face U is calculated before time step calculation
            double diffusivity = govEqn.diffusion().maxAbsDiffusivity(face.U);
            double area = face.surface.area;

            spectralRadiusDiffusion += diffusivity * area * area;
        }
        double volume = cell.shape.volume;
        spectralRadiusDiffusion /= volume;

        double C = 4.0; // Constant multiplying diffusion spectral radius, 4 for central discretization
        double dt = courantNum * (volume / (spectralRadiusConvection + C * spectralRadiusDiffusion));

        cell.dt = Math.min(dt, timeStepLimit);
    }

    private void updateEigenvalue(Face face) {
        face.maxAbsEigenvalue = Math.max(
                govEqn.convection().maxAbsEigenvalues(face.left.U, face.surface.unitNormal()),
                govEqn.convection().maxAbsEigenvalues(face.right.U, face.surface.unitNormal())
        );
    }
}
