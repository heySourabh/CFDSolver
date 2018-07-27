package main;

import main.geom.Point;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ScalarAdvection;
import main.solver.*;
import main.solver.problem.ProblemDefinition;
import main.solver.reconstructor.PiecewiseConstantReconstructor;
import main.solver.reconstructor.SolutionReconstructor;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.GlobalTimeStep;
import main.solver.time.TimeIntegrator;
import main.solver.time.TimeStep;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;

public class Solver2DScalarAdvectionTest {
    private static int xi = 41;
    private static int eta = 41;
    private static double minX = -1;
    private static double maxX = 1;
    private static double minY = -1;
    private static double maxY = 1;
    private static Function<Point, double[]> initFunction = p
            -> new double[]{(p.x > -0.25 && p.x < 0.25 && p.y > -0.25 && p.y < 0.25) ? 1.0 : 0.0};

    private static ProblemDefinition testProblem;

    @BeforeClass
    public static void setupTestProblem() {
        testProblem = new ProblemDefinition() {
            GoverningEquations govEqn = new ScalarAdvection(1.0, 1.0, 0.0);
            Mesh mesh = createMesh();

            private Mesh createMesh() {
                double dx = (maxX - minX) / (xi - 1);
                double dy = (maxY - minY) / (eta - 1);

                File tempMeshFile = new File("test/test_data/tempMeshFile.cfds");
                // <editor-fold desc="Create mesh file">
                try (FileWriter meshFileWriter = new FileWriter(tempMeshFile)) {
                    meshFileWriter.write("dimension = 2\n");
                    meshFileWriter.write("mode = ASCII\n");
                    meshFileWriter.write(String.format("xi = %d\n", xi));
                    meshFileWriter.write(String.format("eta = %d\n", eta));
                    for (int i = 0; i < xi; i++) {
                        for (int j = 0; j < eta; j++) {
                            double x = minX + dx * i;
                            double y = minY + dy * j;
                            double z = 0;
                            meshFileWriter.write(String.format("%-20.15f %-20.15f %-20.15f\n", x, y, z));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //</editor-fold>

                // <editor-fold desc="Read mesh file">
                BoundaryCondition bc = new ExtrapolatedBC(govEqn);
                Mesh mesh2D = null;
                try {
                    mesh2D = new Structured2DMesh(tempMeshFile,
                            govEqn.numVars(), bc, bc, bc, bc);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //</editor-fold>

                // <editor-fold desc="Delete mesh file">
                if (!tempMeshFile.delete()) {
                    System.out.println("Unable to delete temporary mesh file: " + tempMeshFile.toString());
                }
                // </editor-fold>

                return mesh2D;
            }

            TimeIntegrator timeIntegrator = createTimeIntegrator();

            private TimeIntegrator createTimeIntegrator() {
                List<ResidualCalculator> residuals = new ArrayList<>();

                SolutionReconstructor reconstructor = new PiecewiseConstantReconstructor();
                RiemannSolver rusanovSolver = new RusanovRiemannSolver(govEqn);
                residuals.add(new ConvectiveResidual(reconstructor, rusanovSolver, mesh));
                TimeStep timeStep = new GlobalTimeStep(mesh, govEqn);

                return new ExplicitEulerTimeIntegrator(mesh, residuals, timeStep, govEqn.numVars());
            }

            @Override
            public String description() {
                return "Solver test problem - 2D Scalar Advection";
            }

            @Override
            public GoverningEquations govEqn() {
                return govEqn;
            }

            @Override
            public Mesh mesh() {
                return mesh;
            }

            @Override
            public SolutionInitializer solutionInitializer() {
                return new FunctionInitializer(initFunction);
            }

            @Override
            public TimeIntegrator timeIntegrator() {
                return timeIntegrator;
            }

            @Override
            public Convergence convergence() {
                return new Convergence(new double[]{1e-6});
            }

            @Override
            public Config config() {
                Config config = new Config();
                config.setMaxIterations(1);

                return config;
            }
        };
    }

    @Test
    public void solver() {
        ProblemDefinition problem = testProblem;
        Mesh mesh = testProblem.mesh();

        problem.solutionInitializer().initialize(mesh);

        double[][] initU = new double[xi - 1][eta - 1];
        double dx = (maxX - minX) / (xi - 1);
        double dy = (maxY - minY) / (eta - 1);
        for (int i = 0; i < xi - 1; i++) {
            for (int j = 0; j < eta - 1; j++) {
                double x = minX + dx * i + dx / 2.0;
                double y = minY + dy * j + dy / 2.0;
                double z = 0.0;
                initU[i][j] = initFunction.apply(new Point(x, y, z))[0];
            }
        }

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.6);

        double time = 0;
        for (int iter = 0; iter < 1; iter++) {
            timeIntegrator.updateCellAverages(time);
            double[] totalResidual = timeIntegrator.currentTotalResidual(Norm.INFINITY_NORM);
            if (problem.convergence().hasConverged(totalResidual)) break;
        }
        double[] calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        double[] expectedSolU = new double[(xi - 1) * (eta - 1)];
        for (int i = 1; i < xi - 1; i++) {
            for (int j = 1; j < eta - 1; j++) {
                int index = cellIndex(i, j);
                double dt = mesh.cells().get(index).dt;
                expectedSolU[index] = actualSolution(initU[i - 1][j], initU[i][j - 1], initU[i][j], dt, dx, dy);
            }
        }
        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);

        // After 1 more step
        // copy to initU
        for (int i = 0; i < xi - 1; i++) {
            for (int j = 0; j < eta - 1; j++) {
                initU[i][j] = expectedSolU[cellIndex(i, j)];
            }
        }
        timeIntegrator.updateCellAverages(time);
        calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();
        for (int i = 1; i < xi - 1; i++) {
            for (int j = 1; j < eta - 1; j++) {
                int index = cellIndex(i, j);
                double dt = mesh.cells().get(index).dt;
                expectedSolU[index] = actualSolution(initU[i - 1][j], initU[i][j - 1], initU[i][j], dt, dx, dy);
            }
        }
        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);

        // After 3 more iterations

        for (int iter = 0; iter < 3; iter++) {
            timeIntegrator.updateCellAverages(time);
        }
        calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        for (int iter = 0; iter < 3; iter++) {
            // copy to initU
            for (int i = 0; i < xi - 1; i++) {
                for (int j = 0; j < eta - 1; j++) {
                    initU[i][j] = expectedSolU[cellIndex(i, j)];
                }
            }
            for (int i = 1; i < xi - 1; i++) {
                for (int j = 1; j < eta - 1; j++) {
                    int index = cellIndex(i, j);
                    double dt = mesh.cells().get(index).dt;
                    expectedSolU[index] = actualSolution(initU[i - 1][j], initU[i][j - 1], initU[i][j], dt, dx, dy);
                }
            }
        }
        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);
    }

    private double actualSolution(double left, double bottom, double center, double dt, double dx, double dy) {
        double fluxLeaving = center * dy * dt + center * dx * dt;
        double fluxEntering = left * dy * dt + bottom * dx * dt;

        return (center * dx * dy + fluxEntering - fluxLeaving) / (dx * dy);
    }

    private int cellIndex(int i, int j) {
        return i * (eta - 1) + j;
    }
}