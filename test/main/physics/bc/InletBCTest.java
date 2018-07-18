package main.physics.bc;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Surface;
import main.physics.bc.InletBC.InletProperties;
import main.physics.goveqn.factory.EulerEquations;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;

import static main.util.DoubleArray.copy;
import static org.junit.Assert.*;

public class InletBCTest {

    @Test
    public void setGhostCellValues_subsonic1() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(-1, 0, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, prop.normalVelocityMagnitude, 0, 0, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void setGhostCellValues_supersonic1() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(-1, 0, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, prop.normalVelocityMagnitude, 0, 0, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void setGhostCellValues_subsonic2() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, -1, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, prop.normalVelocityMagnitude, 0, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void setGhostCellValues_supersonic2() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, -1, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, prop.normalVelocityMagnitude, 0, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void setGhostCellValues_subsonic3() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, 0, -1);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, 0, prop.normalVelocityMagnitude, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void setGhostCellValues_supersonic3() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, 0, -1);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        inletBC.setGhostCellValues(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, 0, prop.normalVelocityMagnitude, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        assertArrayEquals(consProp, right.U, 1e-8);
    }

    @Test
    public void convectiveFlux_subsonic1() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(-1, 0, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, prop.normalVelocityMagnitude, 0, 0, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux_supersonic1() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(-1, 0, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, prop.normalVelocityMagnitude, 0, 0, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux_subsonic2() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, -1, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, prop.normalVelocityMagnitude, 0, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux_supersonic2() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, -1, 0);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, prop.normalVelocityMagnitude, 0, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux_subsonic3() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 0.1;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, 0, -1);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, 0, prop.normalVelocityMagnitude, insidePrimVars[4]};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }

    @Test
    public void convectiveFlux_supersonic3() {
        double gamma = 1.4, R = 287;
        EulerEquations govEqn = new EulerEquations(gamma, R);
        double rho = 1;
        double p = 101325;
        double a = Math.sqrt(gamma * p / rho);
        double vel = a * 1.5;
        Function<Double, InletProperties> inletProps = time -> new InletProperties(vel, rho, p);
        InletBC inletBC = new InletBC(govEqn, inletProps);

        Cell left = new Cell(0, null, null, null, govEqn.numVars());
        Cell right = new Cell(-1, null, null, null, govEqn.numVars());
        Vector normal = new Vector(0, 0, -1);
        Surface surface = new Surface(1.0, null, normal);
        Face testFace = new Face(null, null, surface, left, right, govEqn.numVars());
        double[] insidePrimVars = {1.5, 20, 5, 21, 101328};
        double[] insideConsVars = govEqn.conservativeVars(insidePrimVars);

        copy(insideConsVars, left.U);
        double time = 0.0;
        double[] actualFlux = inletBC.convectiveFlux(testFace, time);
        InletProperties prop = inletProps.apply(time);
        double[] primProp = new double[]{prop.density, 0, 0, prop.normalVelocityMagnitude, prop.pressure};
        double[] consProp = govEqn.conservativeVars(primProp);
        double[] expectedFlux = govEqn.convection().flux(consProp, normal);
        assertArrayEquals(expectedFlux, actualFlux, 1e-8);
    }
}