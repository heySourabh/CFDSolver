package main.physics.bc;

import main.geom.Vector;
import main.mesh.Face;
import main.physics.goveqn.factory.EulerEquations;

import static main.util.DoubleArray.copy;

public class NormalInletBC implements BoundaryCondition {

    private final EulerEquations govEqn;
    private final InletProperties unsteadyInletProperties;

    public NormalInletBC(EulerEquations govEqn, InletProperties unsteadyInletProperties) {
        this.govEqn = govEqn;
        this.unsteadyInletProperties = unsteadyInletProperties;
    }

    @Override
    public void setGhostCellValues(Face face) {
        InletProperties prop = unsteadyInletProperties;
        Vector velocity = face.surface.unitNormal()
                .mult(-prop.normalVelocityMagnitude); // negative since the normal is pointing out
        double[] primVars = new double[]{
                prop.density, velocity.x, velocity.y, velocity.z, prop.pressure
        };
        double mach = govEqn.mach(primVars, face.surface.unitNormal());
        if (mach > -1 && mach < 1) {
            double[] insidePrimVars = govEqn.primitiveVars(face.left.U);
            double insidePressure = insidePrimVars[4];
            primVars[4] = insidePressure;
        }
        double[] consVars = govEqn.conservativeVars(primVars);

        copy(consVars, face.right.U);
    }

    @Override
    public double[] convectiveFlux(Face face) {
        InletProperties prop = unsteadyInletProperties;
        Vector velocity = face.surface.unitNormal()
                .mult(-prop.normalVelocityMagnitude); // negative since the normal is pointing out
        double[] primVars = new double[]{
                prop.density, velocity.x, velocity.y, velocity.z, prop.pressure
        };
        double mach = govEqn.mach(primVars, face.surface.unitNormal());
        if (mach > -1 && mach < 1) {
            double[] insidePrimVars = govEqn.primitiveVars(face.left.U);
            double insidePressure = insidePrimVars[4];
            primVars[4] = insidePressure;
        }
        double[] consVars = govEqn.conservativeVars(primVars);

        return govEqn.convection().flux(consVars, face.surface.unitNormal());
    }

    public static class InletProperties {
        public final double normalVelocityMagnitude, density, pressure;

        public InletProperties(double normalVelocityMagnitude, double density, double pressure) {
            this.normalVelocityMagnitude = normalVelocityMagnitude;
            this.density = density;
            this.pressure = pressure;
        }
    }
}
