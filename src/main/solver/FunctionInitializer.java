package main.solver;

import main.geom.Point;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.util.DoubleArray;

import java.util.function.Function;

public class FunctionInitializer implements SolutionInitializer {

    private final Function<Point, double[]> f;

    public FunctionInitializer(Function<Point, double[]> conservativeVarsFunction) {
        this.f = conservativeVarsFunction;
    }

    @Override
    public void initialize(Mesh mesh, GoverningEquations govEqn) {
        mesh.cellStream().forEach(cell -> initialize(cell, govEqn));
    }

    private void initialize(Cell cell, GoverningEquations govEqn) {
        double[] conservativeVars = f.apply(cell.shape.centroid);
        DoubleArray.copy(conservativeVars, cell.U);
        DoubleArray.copy(govEqn.realVars(conservativeVars), cell.Wn);
    }
}
