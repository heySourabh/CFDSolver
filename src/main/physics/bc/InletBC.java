package main.physics.bc;

import main.geom.Vector;
import main.mesh.Face;
import main.physics.goveqn.factory.EulerEquations;

import java.util.function.Function;

import static main.util.DoubleArray.copy;

public class InletBC implements BoundaryCondition {

    private final EulerEquations govEqn;
    private final Function<Double, InletProperties> unsteadyInletProperties;

    public InletBC(EulerEquations govEqn, Function<Double, InletProperties> unsteadyInletProperties) {
        this.govEqn = govEqn;
        this.unsteadyInletProperties = unsteadyInletProperties;
    }

    @Override
    public void setGhostCellValues(Face face, double time) {
        InletProperties prop = unsteadyInletProperties.apply(time);
        Vector velocity = face.surface.unitNormal
                .mult(-prop.normalVelocityMagnitude); // negative since the normal is pointing out
        double[] primVars = new double[]{
                prop.density, velocity.x, velocity.y, velocity.z, prop.pressure
        };
        double[] consVars = govEqn.conservativeVars(primVars);

        copy(consVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face, double time) {
        InletProperties prop = unsteadyInletProperties.apply(time);
        Vector velocity = face.surface.unitNormal
                .mult(prop.normalVelocityMagnitude);
        double[] primVars = new double[]{
                prop.density, velocity.x, velocity.y, velocity.z, prop.pressure
        };
        double[] consVars = govEqn.conservativeVars(primVars);

        return govEqn.convection().flux(consVars, face.surface.unitNormal);
    }

    public static class InletProperties {
        private final double normalVelocityMagnitude, density, pressure;

        public InletProperties(double normalVelocityMagnitude, double density, double pressure) {
            this.normalVelocityMagnitude = normalVelocityMagnitude;
            this.density = density;
            this.pressure = pressure;
        }
    }
}
