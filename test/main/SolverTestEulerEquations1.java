package main;

import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.solver.*;
import main.solver.problem.ProblemDefinition;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.GlobalTimeStep;
import main.solver.time.TimeIntegrator;
import main.util.DoubleArray;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class SolverTestEulerEquations1 {
    private static ProblemDefinition testProblem;

    @BeforeClass
    public static void setupTestProblem() {
        testProblem = new ProblemDefinition() {
            private final String description = "Single Cell Euler Equations.";
            private final EulerEquations govEqn = new EulerEquations(1.4, 287);
            private Mesh mesh;

            {
                try {
                    mesh = new Structured2DMesh(
                            new File("test/test_data/mesh_1cell_structured_2d.cfds"),
                            govEqn.numVars(), null, null, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            private final double rho = 1.2;
            private final double u = 0.5;
            private final double v = 0.25;
            private final double w = -6.9;
            private final double pr = 101325.0;
            private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                    p -> govEqn.conservativeVars(new double[]{rho, u, v, w, pr}));
            ResidualCalculator convectiveCalculator = new ConvectiveResidual(new PiecewiseConstantSolutionReconstructor(),
                    new RusanovRiemannSolver(govEqn), mesh);
            private final TimeIntegrator timeIntegrator = new ExplicitEulerTimeIntegrator(mesh,
                    List.of(convectiveCalculator), new GlobalTimeStep(mesh, govEqn), govEqn.numVars());
            private final Convergence convergence = new Convergence(DoubleArray.newFilledArray(govEqn.numVars(), 1e-6));
            private final Config config = new Config();

            @Override
            public String description() {
                return description;
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
                return solutionInitializer;
            }

            @Override
            public TimeIntegrator timeIntegrator() {
                return timeIntegrator;
            }

            @Override
            public Convergence convergence() {
                return convergence;
            }

            @Override
            public Config config() {
                config.setMaxIterations(1);
                return config;
            }
        };
    }

    @Test
    public void solver_extrapolated() {
        ProblemDefinition problem = testProblem;
        GoverningEquations govEqn = problem.govEqn();
        BoundaryCondition bc = new ExtrapolatedBC(govEqn);
        Mesh mesh = problem.mesh();
        mesh.boundaryStream().forEach(bnd -> bnd.setBC(bc));
        problem.solutionInitializer().initialize(mesh);
        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        Config config = problem.config();

        for (int iter = 0; iter < config.getMaxIterations(); iter++) {
            timeIntegrator.updateCellAverages(0);
            double[] totalResidual = timeIntegrator.currentTotalResidual(Norm.ONE_NORM);
            if (problem.convergence().hasConverged(totalResidual)) break;
        }

        double[] expectedU = {1.2, 0.6, 0.3, -8.28, 253341.2535}; // solved manually: on IITB lab notebook

        assertArrayEquals(expectedU, mesh.cells().get(0).U, 1e-8);
    }
}
