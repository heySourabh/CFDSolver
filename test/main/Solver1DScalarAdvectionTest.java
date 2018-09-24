package main;

import main.mesh.Mesh;
import main.mesh.factory.Structured1DMesh;
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static main.util.DoubleArray.copy;
import static org.junit.Assert.assertArrayEquals;

public class Solver1DScalarAdvectionTest {

    private static ProblemDefinition testProblem;

    @BeforeClass
    public static void setupTestProblem() {
        testProblem = new ProblemDefinition() {
            private final GoverningEquations govEqn = new ScalarAdvection(1.0, 0.0, 0.0);
            private final Mesh mesh = createMesh();
            private final TimeIntegrator timeIntegrator = createTimeIntegrator();

            private Mesh createMesh() {
                // <editor-fold desc="Create problem mesh and write to file"
                int xi = 21;
                double minX = -1;
                double maxX = 1;
                double dx = (maxX - minX) / (xi - 1);

                File tempMeshFile = new File("test/test_data/tempMeshFile.cfds");
                try (FileWriter fileWriter = new FileWriter(tempMeshFile)) {
                    fileWriter.write("dimension= 1\n");
                    fileWriter.write("mode = ASCII\n");
                    fileWriter.write(String.format("xi = %d\n", xi));

                    for (int i = 0; i < xi; i++) {
                        double x = minX + dx * i;
                        double y = 0;
                        double z = 0;

                        fileWriter.write(String.format("%-20.15f %-20.15f %-20.15f\n", x, y, z));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // </editor-fold>

                // <editor-fold desc="Read the mesh from file"
                BoundaryCondition bc = new ExtrapolatedBC(govEqn);
                Mesh mesh = null;
                try {
                    mesh = new Structured1DMesh(tempMeshFile, govEqn.numVars(), bc, bc);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // </editor-fold>

                //<editor-fold desc="Delete the mesh file"
                if (!tempMeshFile.delete()) {
                    System.out.println("Unable to delete " + tempMeshFile.toString());
                }
                //</editor-fold>

                return mesh;
            }

            private TimeIntegrator createTimeIntegrator() {
                ArrayList<ResidualCalculator> residuals = new ArrayList<>();

                SolutionReconstructor reconstructor = new PiecewiseConstantReconstructor();
                RiemannSolver riemannSolver = new RusanovRiemannSolver(govEqn);
                residuals.add(new ConvectionResidual(reconstructor, riemannSolver, mesh));
                GlobalTimeStep timeStep = new GlobalTimeStep(mesh, govEqn);
                return new ExplicitEulerTimeIntegrator(mesh, new SpaceDiscretization(mesh, residuals),
                        timeStep, govEqn.numVars());
            }

            @Override
            public String description() {
                return "Solver test problem - 1D Scalar Advection";
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
                return new FunctionInitializer(p -> new double[]{p.x > -0.25 && p.x < 0.25 ? 1.0 : 0.0});
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
                config.setConvergenceNorm(Norm.TWO_NORM);
                return config;
            }
        };
    }

    @Test
    public void solver() {
        ProblemDefinition problem = testProblem;
        Mesh mesh = problem.mesh();

        problem.solutionInitializer().initialize(mesh);
        double[] initU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        Config config = problem.config();
        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.6);
        Convergence convergence = problem.convergence();
        for (int i = 0; i < config.getMaxIterations(); i++) {
            timeIntegrator.updateCellAverages();
            double[] totalResidual = timeIntegrator.currentTotalResidual(config.getConvergenceNorm());
            if (convergence.hasConverged(totalResidual)) {
                break;
            }
        }

        double[] calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        // Actual solution after 1 iteration
        double[] expectedSolU = new double[mesh.cells().size()];
        for (int i = 1; i < mesh.cells().size(); i++) {
            expectedSolU[i] = actualSolution(initU[i - 1], initU[i], mesh.cells().get(i).dt, mesh.cells().get(i).shape.volume);
        }

        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);

        // Actual solution after 2 iterations
        copy(expectedSolU, initU);
        for (int i = 1; i < mesh.cells().size(); i++) {
            expectedSolU[i] = actualSolution(initU[i - 1], initU[i], mesh.cells().get(i).dt, mesh.cells().get(i).shape.volume);
        }

        // Calculated solution after another iteration
        for (int i = 0; i < config.getMaxIterations(); i++) {
            timeIntegrator.updateCellAverages();
        }
        calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);

        // Actual solution after 3 iterations
        copy(expectedSolU, initU);
        for (int i = 1; i < mesh.cells().size(); i++) {
            expectedSolU[i] = actualSolution(initU[i - 1], initU[i], mesh.cells().get(i).dt, mesh.cells().get(i).shape.volume);
        }

        // Calculated solution after another iteration
        for (int i = 0; i < config.getMaxIterations(); i++) {
            timeIntegrator.updateCellAverages();
        }
        calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);

        // after 6 iterations (3 more iterations)
        for (int n = 0; n < 3; n++) {
            copy(expectedSolU, initU);
            for (int i = 1; i < mesh.cells().size(); i++) {
                expectedSolU[i] = actualSolution(initU[i - 1], initU[i], mesh.cells().get(i).dt, mesh.cells().get(i).shape.volume);
            }
        }

        // Calculated solution after another 3 iterations
        for (int i = 0; i < 3; i++) {
            timeIntegrator.updateCellAverages();
        }
        calculatedSolU = mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();

        assertArrayEquals(expectedSolU, calculatedSolU, 1e-15);
    }

    private double actualSolution(double left, double center, double dt, double dx) {
        double fluxLeaving = center * dt;
        double fluxEntering = left * dt;

        return (center * dx + fluxEntering - fluxLeaving) / dx;
    }
}
