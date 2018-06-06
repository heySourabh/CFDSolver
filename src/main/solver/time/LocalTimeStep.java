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
    public void updateCellTimeSteps(double courantNum) {
        // Go through all the internal faces and save maxAbsEigenvalue
        mesh.internalFaceStream()
                .forEach(this::updateEigenvalue);

        // Go through all the boundary faces and save maxAbsEigenvalue
        mesh.boundaryStream().flatMap(b -> b.faces.stream())
                .forEach(this::updateEigenvalue);

        // Go through all the cells and save the time step scaled by Courant number
        mesh.cellStream()
                .forEach(cell -> updateTimeStep(cell, courantNum));
    }

    private void updateTimeStep(Cell cell, double courantNum) {
        double volume = cell.shape.volume;
        double summation = cell.faces.stream()
                .mapToDouble(face -> face.maxAbsEigenvalue * face.surface.area)
                .sum();
        cell.dt = (volume / summation) * courantNum;
    }

    private void updateEigenvalue(Face face) {
        face.maxAbsEigenvalue = Math.max(
                govEqn.convection().maxAbsEigenvalues(face.left.U, face.surface.unitNormal),
                govEqn.convection().maxAbsEigenvalues(face.right.U, face.surface.unitNormal)
        );
    }
}
