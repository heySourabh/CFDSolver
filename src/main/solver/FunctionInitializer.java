package main.solver;

import main.geom.Point;
import main.geom.VTKType;
import main.geom.Vector;
import main.geom.factory.Hexahedron;
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
        } else if (cell.vtkType == VTKType.VTK_HEXAHEDRON) {
            return averageOverHexCell(cell, f);
        } else { // Value at cell centroid
            return f.apply(cell.shape.centroid);
        }
    }

    private double[] averageOverHexCell(Cell cell, Function<Point, double[]> f) {
        Point p0 = cell.nodes[0].location();
        Point p1 = cell.nodes[1].location();
        Point p2 = cell.nodes[2].location();
        Point p3 = cell.nodes[3].location();
        Point p4 = cell.nodes[4].location();
        Point p5 = cell.nodes[5].location();
        Point p6 = cell.nodes[6].location();
        Point p7 = cell.nodes[7].location();

        Hexahedron hex = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);

        return multiply(integrateOverHex(p0, p1, p2, p3, p4, p5, p6, p7, f, 2),
                1.0 / hex.volume());
    }

    private double[] integrateOverHex(
            Point p0, Point p1, Point p2, Point p3,
            Point p4, Point p5, Point p6, Point p7,
            Function<Point, double[]> f, int level) {
        if (level == 0) {
            Hexahedron hex = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);
            return multiply(f.apply(hex.centroid()), hex.volume());
        } else { // sub-divide
            Hexahedron hex = new Hexahedron(p0, p1, p2, p3, p4, p5, p6, p7);
            Point pP = hex.centroid();

            // edge points
            Point pa = pointInBetween(p0, p1);
            Point pb = pointInBetween(p1, p2);
            Point pc = pointInBetween(p2, p3);
            Point pd = pointInBetween(p3, p0);
            Point pe = pointInBetween(p4, p5);
            Point pf = pointInBetween(p5, p6);
            Point pg = pointInBetween(p6, p7);
            Point ph = pointInBetween(p7, p4);
            Point pi = pointInBetween(p1, p5);
            Point pj = pointInBetween(p2, p6);
            Point pk = pointInBetween(p3, p7);
            Point pl = pointInBetween(p0, p4);

            // face points
            Point pf0 = averagePointLocation(new Point[]{p0, p1, p2, p3});
            Point pf1 = averagePointLocation(new Point[]{p4, p5, p6, p7});
            Point pf2 = averagePointLocation(new Point[]{p0, p1, p5, p4});
            Point pf3 = averagePointLocation(new Point[]{p3, p2, p6, p7});
            Point pf4 = averagePointLocation(new Point[]{p1, p2, p6, p5});
            Point pf5 = averagePointLocation(new Point[]{p0, p3, p7, p4});

            double[] part1 = integrateOverHex(p0, pa, pf0, pd, pl, pf2, pP, pf5, f, level - 1);
            double[] part2 = integrateOverHex(pa, p1, pb, pf0, pf2, pi, pf4, pP, f, level - 1);
            double[] part3 = integrateOverHex(pd, pf0, pc, p3, pf5, pP, pf3, pk, f, level - 1);
            double[] part4 = integrateOverHex(pf0, pb, p2, pc, pP, pf4, pj, pf3, f, level - 1);
            double[] part5 = integrateOverHex(pl, pf2, pP, pf5, p4, pe, pf1, ph, f, level - 1);
            double[] part6 = integrateOverHex(pf2, pi, pf4, pP, pe, p5, pf, pf1, f, level - 1);
            double[] part7 = integrateOverHex(pf5, pP, pf3, pk, ph, pf1, pg, p7, f, level - 1);
            double[] part8 = integrateOverHex(pP, pf4, pj, pf3, pf1, pf, p6, pg, f, level - 1);

            return addArrays(new double[][]{
                    part1, part2, part3, part4,
                    part5, part6, part7, part8
            });
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

    private Point averagePointLocation(Point[] points) {
        double x = 0.0, y = 0.0, z = 0.0;
        for (Point point : points) {
            x += point.x;
            y += point.y;
            z += point.z;
        }

        return new Point(x / points.length, y / points.length, z / points.length);
    }

    private double[] addArrays(double[][] arrays) {
        int len = arrays[0].length;
        double[] sum = new double[len];

        for (double[] array : arrays) {
            for (int i = 0; i < len; i++) {
                sum[i] += array[i];
            }
        }

        return sum;
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
