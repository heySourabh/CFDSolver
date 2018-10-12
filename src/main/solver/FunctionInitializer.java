package main.solver;

import main.geom.Point;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.util.DoubleArray;

import java.util.function.Function;

public class FunctionInitializer implements SolutionInitializer {

    private final Function<Point, double[]> f;

    public FunctionInitializer(Function<Point, double[]> conservativeVarsFunction) {
        this.f = conservativeVarsFunction;
    }

    @Override
    public void initialize(Mesh mesh) {
        mesh.cellStream().forEach(this::initialize);
    }

    private void initialize(Cell cell) {
        double[] vars = f.apply(cell.shape.centroid);
        DoubleArray.copy(vars, cell.U);
        DoubleArray.copy(vars, cell.Wn);
    }
}
