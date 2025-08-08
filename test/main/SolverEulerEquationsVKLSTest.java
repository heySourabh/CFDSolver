package main;

import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.bc.ExtrapolatedBC;
import main.physics.bc.InviscidWallBC;
import main.physics.bc.NormalInletBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.SolutionReconstructor;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.RusanovRiemannSolver;
import main.solver.problem.ProblemDefinition;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.LocalTimeStep;
import main.solver.time.TimeIntegrator;
import main.util.DoubleArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SolverEulerEquationsVKLSTest {

    private static ProblemDefinition testProblem;

    @BeforeAll
    public static void setupTestProblem() {
        testProblem = new ProblemDefinition() {
            private final EulerEquations govEqn = new EulerEquations(1.4);
            private Mesh mesh;

            {
                try {
                    mesh = new Unstructured2DMesh(
                            new File("test/test_data/mesh_diamond_airfoil_unstructured_2d.cfdu"),
                            govEqn.numVars(), Map.of(
                            "Top-Bottom", new ExtrapolatedBC(govEqn),
                            "Right", new ExtrapolatedBC(govEqn),
                            "Airfoil", new InviscidWallBC(govEqn),
                            "Inlet", new NormalInletBC(govEqn,
                                    new NormalInletBC.InletProperties(700.0, 1.0, 101325.0))
                    ));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            private final double u = 700.0;
            private final double rho = 1.0;
            private final double rhoE = 101325.0 / (1.4 - 1.0) / 1.0 + u * u / 2.0;
            private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                    p -> new double[]{rho, rho * u, 0.0, 0.0, rhoE});
            final CellNeighborCalculator neighborsCalculator = new FaceBasedCellNeighbors();
            final SolutionReconstructor reconstructor = new VKLimiterReconstructor(mesh, govEqn, neighborsCalculator);
            final ResidualCalculator convectiveCalculator = new ConvectionResidual(reconstructor,
                    new RusanovRiemannSolver(govEqn), mesh);
            final CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, neighborsCalculator);
            private final TimeIntegrator timeIntegrator = new ExplicitEulerTimeIntegrator(mesh,
                    new SpaceDiscretization(mesh, cellGradientCalculator, List.of(convectiveCalculator)),
                    new LocalTimeStep(mesh, govEqn), govEqn.numVars());
            private final Convergence convergence = new Convergence(DoubleArray.newFilledArray(govEqn.numVars(), 1e-3));
            private final Config config = new Config();

            @Override
            public String description() {
                return "Euler Equations - Diamond Airfoil.";
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
                config.setMaxIterations(1500);
                try {
                    config.setWorkingDirectory(new File("test/test_data/"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return config;
            }
        };
    }

    //@Ignore
    @Test
    public void solver() {
        ProblemDefinition problem = testProblem;
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());
        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        Config config = problem.config();
        boolean converged = false;
        int iter = 0;
        for (; iter < config.getMaxIterations(); iter++) {
            timeIntegrator.updateCellAverages();
            double[] totalResidual = timeIntegrator.currentTotalResidual(Norm.TWO_NORM);
            if (iter % 100 == 0) {
                System.out.println(iter + ": " + Arrays.toString(totalResidual));
            }
            if (problem.convergence().hasConverged(totalResidual)) {
                System.out.println(iter + ": " + Arrays.toString(totalResidual));
                converged = true;
                break;
            }
        }

        assertTrue(converged);
        assertEquals(1028, iter);
        new VTKWriter(mesh, problem.govEqn())
                .write(new File(config.getWorkingDirectory(), "output_airfoil_vk_ls_test.vtu"));
    }
}
