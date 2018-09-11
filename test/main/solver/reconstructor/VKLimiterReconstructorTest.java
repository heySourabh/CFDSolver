package main.solver.reconstructor;

import main.geom.Point;
import main.geom.Vector;
import main.geom.factory.Line;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.goveqn.factory.EulerEquations;
import main.solver.CellGradientCalculator;
import main.solver.FaceBasedCellNeighbors;
import main.solver.LeastSquareCellGradient;
import main.solver.CellNeighborCalculator;
import main.util.DoubleArray;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static main.util.DoubleArray.copy;
import static main.util.DoubleArray.newFilledArray;
import static org.junit.Assert.assertArrayEquals;

public class VKLimiterReconstructorTest {

    @Test
    public void reconstruct_evaluation0() throws FileNotFoundException {
        EulerEquations govEqn = new EulerEquations(1.4, 287);
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d.cfdu"), govEqn.numVars(), Map.of());

        double[] U = {1.2, 45, 75, 18, 6546135};
        mesh.cellStream().forEach(cell -> copy(U, cell.U));

        CellNeighborCalculator neighborsCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalculator);
        SolutionReconstructor reconstructor = new VKLimiterReconstructor(mesh, cellGradientCalculator, neighborsCalculator);
        reconstructor.reconstruct();

        Point p1 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p2 = new Line(mesh.nodes().get(3).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p3 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(3).location())
                .centroid();
        Cell cell = mesh.cells().get(1);
        double[] consVars1 = reconstructor.conservativeVars(cell, p1);
        assertArrayEquals(U, consVars1, 1e-12);

        double[] consVars2 = reconstructor.conservativeVars(cell, p2);
        assertArrayEquals(U, consVars2, 1e-12);

        double[] consVars3 = reconstructor.conservativeVars(cell, p3);
        assertArrayEquals(U, consVars3, 1e-12);
    }

    @Test
    public void reconstruct_evaluation_smooth_xy() throws FileNotFoundException {
        EulerEquations govEqn = new EulerEquations(1.4, 287);
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d.cfdu"), govEqn.numVars(), Map.of());

        Cell cell_i = mesh.cells().get(1);
        int numVars = cell_i.U.length;
        Vector[] gradientsNotLimited = gradientsNotLimited();
        mesh.cellStream().forEach(c -> copy(linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, c.shape.centroid), c.U));

        double[] negInf = newFilledArray(numVars, Double.NEGATIVE_INFINITY);
        double[] posInf = newFilledArray(numVars, Double.POSITIVE_INFINITY);

        Cell[] neighs = {mesh.cells().get(0), mesh.cells().get(2), mesh.cells().get(5)};
        double[] minU = DoubleArray.min(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(posInf, DoubleArray::min), cell_i.U);

        double[] maxU = DoubleArray.max(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(negInf, DoubleArray::max), cell_i.U);

        double[] Phi = Arrays.stream(cell_i.nodes)
                .map(n -> linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, n.location()))
                .map(Uj -> Phi(minU, maxU, cell_i.U, Uj))
                .reduce(posInf, DoubleArray::min);

        Vector[] gradientsLimited = IntStream.range(0, numVars)
                .mapToObj(var -> gradientsNotLimited[var].mult(Phi[var]))
                .toArray(Vector[]::new);

        CellNeighborCalculator neighborsCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalculator);
        SolutionReconstructor reconstructor = new VKLimiterReconstructor(mesh, cellGradientCalculator, neighborsCalculator);
        reconstructor.reconstruct();

        Point p1 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p2 = new Line(mesh.nodes().get(3).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p3 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(3).location())
                .centroid();
        double[] consVars1 = reconstructor.conservativeVars(cell_i, p1);
        double[] expVars1 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p1);
        assertArrayEquals(expVars1, consVars1, 1e-8);

        double[] consVars2 = reconstructor.conservativeVars(cell_i, p2);
        double[] expVars2 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p2);
        assertArrayEquals(expVars2, consVars2, 1e-8);

        double[] consVars3 = reconstructor.conservativeVars(cell_i, p3);
        double[] expVars3 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p3);
        assertArrayEquals(expVars3, consVars3, 1e-8);
    }

    @Test
    public void reconstruct_evaluation_smooth_yz() throws FileNotFoundException {
        EulerEquations govEqn = new EulerEquations(1.4, 287);
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d_yz.cfdu"), govEqn.numVars(), Map.of());

        Cell cell_i = mesh.cells().get(1);
        int numVars = cell_i.U.length;
        Vector[] gradientsNotLimited = gradientsNotLimited();
        mesh.cellStream().forEach(c -> copy(linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, c.shape.centroid), c.U));

        double[] negInf = newFilledArray(numVars, Double.NEGATIVE_INFINITY);
        double[] posInf = newFilledArray(numVars, Double.POSITIVE_INFINITY);

        Cell[] neighs = {mesh.cells().get(0), mesh.cells().get(2), mesh.cells().get(5)};
        double[] minU = DoubleArray.min(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(posInf, DoubleArray::min), cell_i.U);

        double[] maxU = DoubleArray.max(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(negInf, DoubleArray::max), cell_i.U);

        double[] Phi = Arrays.stream(cell_i.nodes)
                .map(n -> linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, n.location()))
                .map(Uj -> Phi(minU, maxU, cell_i.U, Uj))
                .reduce(posInf, DoubleArray::min);

        Vector[] gradientsLimited = IntStream.range(0, numVars)
                .mapToObj(var -> gradientsNotLimited[var].mult(Phi[var]))
                .toArray(Vector[]::new);

        CellNeighborCalculator neighborsCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalculator);
        SolutionReconstructor reconstructor = new VKLimiterReconstructor(mesh, cellGradientCalculator, neighborsCalculator);
        reconstructor.reconstruct();

        Point p1 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p2 = new Line(mesh.nodes().get(3).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p3 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(3).location())
                .centroid();
        double[] consVars1 = reconstructor.conservativeVars(cell_i, p1);
        double[] expVars1 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p1);
        assertArrayEquals(expVars1, consVars1, 1e-8);

        double[] consVars2 = reconstructor.conservativeVars(cell_i, p2);
        double[] expVars2 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p2);
        assertArrayEquals(expVars2, consVars2, 1e-8);

        double[] consVars3 = reconstructor.conservativeVars(cell_i, p3);
        double[] expVars3 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p3);
        assertArrayEquals(expVars3, consVars3, 1e-8);
    }

    @Test
    public void reconstruct_evaluation_smooth_xz() throws FileNotFoundException {
        EulerEquations govEqn = new EulerEquations(1.4, 287);
        Mesh mesh = new Unstructured2DMesh(
                new File("test/test_data/mesh_unstructured_2d_xz.cfdu"), govEqn.numVars(), Map.of());

        Cell cell_i = mesh.cells().get(1);
        int numVars = cell_i.U.length;
        Vector[] gradientsNotLimited = gradientsNotLimited();
        mesh.cellStream().forEach(c -> copy(linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, c.shape.centroid), c.U));

        double[] negInf = newFilledArray(numVars, Double.NEGATIVE_INFINITY);
        double[] posInf = newFilledArray(numVars, Double.POSITIVE_INFINITY);

        Cell[] neighs = {mesh.cells().get(0), mesh.cells().get(2), mesh.cells().get(5)};
        double[] minU = DoubleArray.min(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(posInf, DoubleArray::min), cell_i.U);

        double[] maxU = DoubleArray.max(Arrays.stream(neighs)
                .map(cell -> cell.U)
                .reduce(negInf, DoubleArray::max), cell_i.U);

        double[] Phi = Arrays.stream(cell_i.nodes)
                .map(n -> linearReconstruct(gradientsNotLimited, cell_i.shape.centroid, n.location()))
                .map(Uj -> Phi(minU, maxU, cell_i.U, Uj))
                .reduce(posInf, DoubleArray::min);

        Vector[] gradientsLimited = IntStream.range(0, numVars)
                .mapToObj(var -> gradientsNotLimited[var].mult(Phi[var]))
                .toArray(Vector[]::new);

        CellNeighborCalculator neighborsCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalculator);
        SolutionReconstructor reconstructor = new VKLimiterReconstructor(mesh, cellGradientCalculator, neighborsCalculator);
        reconstructor.reconstruct();

        Point p1 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p2 = new Line(mesh.nodes().get(3).location(), mesh.nodes().get(4).location())
                .centroid();
        Point p3 = new Line(mesh.nodes().get(2).location(), mesh.nodes().get(3).location())
                .centroid();
        double[] consVars1 = reconstructor.conservativeVars(cell_i, p1);
        double[] expVars1 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p1);
        assertArrayEquals(expVars1, consVars1, 1e-8);

        double[] consVars2 = reconstructor.conservativeVars(cell_i, p2);
        double[] expVars2 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p2);
        assertArrayEquals(expVars2, consVars2, 1e-8);

        double[] consVars3 = reconstructor.conservativeVars(cell_i, p3);
        double[] expVars3 = linearReconstruct(gradientsLimited, cell_i.shape.centroid, p3);
        assertArrayEquals(expVars3, consVars3, 1e-8);
    }

    private static double[] Phi(double[] minU, double[] maxU, double[] Ui, double[] Uj) {
        return IntStream.range(0, Ui.length)
                .mapToDouble(var
                        -> Uj[var] - Ui[var] > 0 ? phi(maxU[var], Ui[var], Uj[var])
                        : Uj[var] - Ui[var] < 0 ? phi(minU[var], Ui[var], Uj[var])
                        : 1.0).toArray();
    }

    private Vector[] gradientsNotLimited() {
        Random rnd = new Random(984);

        return new Vector[]{
                new Vector(next(rnd), next(rnd), next(rnd)),
                new Vector(next(rnd), next(rnd), next(rnd)),
                new Vector(next(rnd), next(rnd), next(rnd)),
                new Vector(next(rnd), next(rnd), next(rnd)),
                new Vector(next(rnd), next(rnd), next(rnd))
        };
    }

    private double[] linearReconstruct(Vector[] grad_i, Point pi, Point pj) {
        Vector vr = new Vector(pi, pj);

        int numVars = 5;
        double[] Ui = {1.2, 45, 75, 18, 6546135};
        double[] recon = new double[numVars];
        for (int var = 0; var < numVars; var++) {
            recon[var] = Ui[var] + grad_i[var].dot(vr);
        }

        return recon;
    }

    private static double phi(double uiMinOrMax, double ui, double uj) {
        double y = (uiMinOrMax - ui) / (uj - ui);
        return (y * y + 2.0 * y) / (y * y + y + 2.0);
    }

    private double next(Random rnd) {
        return rnd.nextDouble() * rnd.nextInt(100) - 50;
    }
}