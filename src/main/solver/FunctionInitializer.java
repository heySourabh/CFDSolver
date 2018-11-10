package main.solver;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Quad;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.physics.goveqn.GoverningEquations;
import main.util.DoubleArray;

import java.util.function.Function;

import static main.util.DoubleArray.*;

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
        double[] conservativeVars = calculateCentroidValues(cell);
        DoubleArray.copy(conservativeVars, cell.U);
        DoubleArray.copy(govEqn.realVars(conservativeVars), cell.Wn);
    }

    private double[] calculateCentroidValues(Cell cell) {
        if (cell.vtkType == VTKType.VTK_QUAD) {
            return averageOverQuadCell(cell, f);
        } else if (cell.vtkType == VTKType.VTK_TRIANGLE) {
            return averageOverTriCell(cell, f);
        } else { // Value at cell centroid
            return f.apply(cell.shape.centroid);
        }
    }

    private double[] averageOverTriCell(Cell cell, Function<Point, double[]> f) {
        Point p1 = cell.nodes[0].location();
        Point p2 = cell.nodes[1].location();
        Point p3 = cell.nodes[2].location();

        return averageOverTri(p1, p2, p3, f, 5);
    }

    private double[] averageOverTri(Point p1, Point p2, Point p3, Function<Point, double[]> f, int level) {
        if (level == 0) {
            return f.apply(triCentroid(p1, p2, p3));
        } else { // sub-divide
            Point p12 = pointInBetween(p1, p2);
            Point p13 = pointInBetween(p1, p3);
            Point p23 = pointInBetween(p2, p3);

            double[] Ua = averageOverTri(p1, p13, p12, f, level - 1);
            double[] Ub = averageOverTri(p2, p12, p23, f, level - 1);
            double[] Uc = averageOverTri(p3, p13, p23, f, level - 1);
            double[] Ud = averageOverTri(p12, p13, p23, f, level - 1);

            return multiply(add(add(Ua, Ub), add(Uc, Ud)), 0.25);
        }
    }

    private Point pointInBetween(Point p1, Point p2) {
        double x = 0.5 * (p1.x + p2.x);
        double y = 0.5 * (p1.y + p2.y);
        double z = 0.5 * (p1.z + p2.z);

        return new Point(x, y, z);
    }

    private Point triCentroid(Point p1, Point p2, Point p3) {
        double xc = (p1.x + p2.x + p3.x) / 3.0;
        double yc = (p1.y + p2.y + p3.y) / 3.0;
        double zc = (p1.z + p2.z + p3.z) / 3.0;

        return new Point(xc, yc, zc);
    }

    private double[] averageOverQuadCell(Cell cell, Function<Point, double[]> f) {
        Point p00 = cell.nodes[0].location();
        Point p10 = cell.nodes[1].location();
        Point p11 = cell.nodes[2].location();
        Point p01 = cell.nodes[3].location();

        int numDivs = 20;
        Point[][] grid = grid(p00, p10, p11, p01, numDivs);

        double[] sumFdA = new double[cell.U.length];
        for (int i = 0; i < numDivs; i++) {
            for (int j = 0; j < numDivs; j++) {
                Point pa = grid[i][j];
                Point pb = grid[i + 1][j];
                Point pc = grid[i + 1][j + 1];
                Point pd = grid[i][j + 1];

                Quad quad = new Quad(pa, pb, pc, pd);

                Point centroid = quad.centroid();

                double[] U = f.apply(centroid);

                increment(sumFdA, multiply(U, quad.area()));
            }
        }
        return divide(sumFdA, new Quad(p00, p10, p11, p01).area());
    }

    private Point[][] grid(Point p00, Point p10, Point p11, Point p01, int numDivs) {
        double dXi = 1.0 / numDivs;
        double dEta = 1.0 / numDivs;
        Point[][] grid = new Point[numDivs + 1][numDivs + 1];

        for (int i = 0; i < numDivs + 1; i++) {
            double xi = dXi * i;
            for (int j = 0; j < numDivs + 1; j++) {
                double eta = dEta * j;
                grid[i][j] = eta0(p00, p10, xi).mult(1 - eta)
                        .add(eta1(p01, p11, xi).mult(eta))
                        .add(xi0(p00, p01, eta).mult(1 - xi))
                        .add(xi1(p10, p11, eta).mult(xi))
                        .sub(p00.toVector().mult((1 - eta) * (1 - xi)))
                        .sub(p01.toVector().mult(eta * (1 - xi)))
                        .sub(p10.toVector().mult((1 - eta) * xi))
                        .sub(p11.toVector().mult(xi * eta))
                        .toPoint();
            }
        }

        return grid;
    }

    private Vector xi0(Point p00, Point p01, double eta) {
        Vector v01 = new Vector(p00, p01);

        return p00.toVector()
                .add(v01.mult(eta));
    }

    private Vector xi1(Point p10, Point p11, double eta) {
        Vector v01 = new Vector(p10, p11);

        return p10.toVector()
                .add(v01.mult(eta));
    }

    private Vector eta0(Point p00, Point p10, double xi) {
        Vector v01 = new Vector(p00, p10);

        return p00.toVector()
                .add(v01.mult(xi));
    }

    private Vector eta1(Point p01, Point p11, double xi) {
        Vector v01 = new Vector(p01, p11);

        return p01.toVector()
                .add(v01.mult(xi));
    }
}
