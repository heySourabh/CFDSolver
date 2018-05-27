package main.solver.time;

import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;

public class GlobalTimeStep implements TimeStep {

    private final LocalTimeStep localTimeStep;
    private final Mesh mesh;

    GlobalTimeStep(Mesh mesh, GoverningEquations govEqn) {
        localTimeStep = new LocalTimeStep(mesh, govEqn);
        this.mesh = mesh;
    }

    @Override
    public void updateCellTimeSteps(double courantNum) {
        // Calculate the local dt
        localTimeStep.updateCellTimeSteps(courantNum);

        // Calculate minimum time step in the entire domain
        double minTimeStep = mesh.cellStream()
                .mapToDouble(cell -> cell.dt)
                .min().orElseThrow(() -> new ArithmeticException("Unable to calculate global time step."));

        // Set the same time step in the entire domain
        mesh.cellStream()
                .forEach(cell -> cell.dt = minTimeStep);
    }
}
