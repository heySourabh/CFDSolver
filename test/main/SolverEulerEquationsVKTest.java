package main;

import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.bc.ExtrapolatedBC;
import main.physics.bc.InletBC;
import main.physics.bc.InviscidWallBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.EulerEquations;
import main.solver.*;
import main.solver.problem.ProblemDefinition;
import main.solver.reconstructor.VKLimiterReconstructor;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.LocalTimeStep;
import main.solver.time.TimeIntegrator;
import main.util.DoubleArray;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SolverEulerEquationsVKTest {

    private static ProblemDefinition testProblem;

    @BeforeClass
    public static void setupTestProblem() {
        testProblem = new ProblemDefinition() {
            private final String description = "Euler Equations - Diamond Airfoil.";
            private final EulerEquations govEqn = new EulerEquations(1.4, 287);
            private Mesh mesh;

            {
                try {
                    mesh = new Unstructured2DMesh(
                            new File("test/test_data/mesh_diamond_airfoil_unstructured_2d.cfdu"),
                            govEqn.numVars(), Map.of(
                            "Top-Bottom", new ExtrapolatedBC(govEqn),
                            "Right", new ExtrapolatedBC(govEqn),
                            "Airfoil", new InviscidWallBC(govEqn),
                            "Inlet", new InletBC(govEqn,
                                    time -> new InletBC.InletProperties(700.0, 1.0, 101325.0))
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
            ResidualCalculator convectiveCalculator = new ConvectiveResidual(new VKLimiterReconstructor(mesh),
                    new RusanovRiemannSolver(govEqn), mesh);
            private final TimeIntegrator timeIntegrator = new ExplicitEulerTimeIntegrator(mesh,
                    List.of(convectiveCalculator), new LocalTimeStep(mesh, govEqn), govEqn.numVars());
            private final Convergence convergence = new Convergence(DoubleArray.newFilledArray(govEqn.numVars(), 1e-3));
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

    @Ignore
    @Test
    public void solver() {
        ProblemDefinition problem = testProblem;
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh);
        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        Config config = problem.config();
        boolean converged = false;
        for (int iter = 0; iter < config.getMaxIterations(); iter++) {
            timeIntegrator.updateCellAverages(0);
            double[] totalResidual = timeIntegrator.currentTotalResidual(Norm.TWO_NORM);
            System.out.println(iter + ": " + Arrays.toString(totalResidual));
            if (problem.convergence().hasConverged(totalResidual)) {
                converged = true;
                break;
            }
        }

        assertTrue(converged);
        new VTKWriter(new File(config.getWorkingDirectory(), "output_airfoil_vk_test.vtu"), mesh, problem.govEqn()).write();
    }
}
