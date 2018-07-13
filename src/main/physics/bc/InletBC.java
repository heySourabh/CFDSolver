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
                .mult(-prop.inVelocity); // negative since the normal is pointing out
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
                .mult(prop.inVelocity);
        double[] primVars = new double[]{
                prop.density, velocity.x, velocity.y, velocity.z, prop.pressure
        };
        double[] consVars = govEqn.conservativeVars(primVars);

        return govEqn.convection().flux(consVars, face.surface.unitNormal);
    }

    public static class InletProperties {
        final double inVelocity, density, pressure;

        public InletProperties(double inVelocity, double density, double pressure) {
            this.inVelocity = inVelocity;
            this.density = density;
            this.pressure = pressure;
        }
    }
}
