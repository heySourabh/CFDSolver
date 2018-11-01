package main;

import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.WallBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.RusanovRiemannSolver;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.LocalTimeStep;
import main.solver.time.TimeIntegrator;
import main.solver.time.TimeStep;
import main.util.DoubleArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class SolverLidDrivenCavity2DTest {

    private final ProblemDefinition problem = new ProblemDefinition() {
        private final double Re = 100;
        private final double L = 1.0;
        private final double rho = 1.0;
        private final double lidVelocity = 1.0;
        private final double mu = rho * lidVelocity * L / Re;
        private final Vector gravity = new Vector(0, 0, 0);

        private final ArtificialCompressibilityEquations govEqn
                = new ArtificialCompressibilityEquations(1.0, mu, gravity);

        private final Mesh mesh = create2DMesh(20, 20);

        private Mesh create2DMesh(int numXCells, int numYCells) {
            int numXNodes = numXCells + 1;
            int numYNodes = numYCells + 1;
            double minX = 0, minY = 0;
            double maxX = minX + L;
            double maxY = minY + L;
            File tempMeshFile = new File("test/test_data/lid_driven_cavity_mesh");

            try (FileWriter fileWriter = new FileWriter(tempMeshFile);
                 PrintWriter writer = new PrintWriter(fileWriter)) {
                writer.write("dimension = 2\n");
                writer.write("mode = ASCII\n");
                writer.printf("xi = %d\n", numXNodes);
                writer.printf("eta = %d\n", numYNodes);
                for (int i = 0; i < numXNodes; i++) {
                    double x = minX + i / (numXNodes - 1.0) * (maxX - minX);
                    for (int j = 0; j < numYNodes; j++) {
                        double y = minY + j / (numYNodes - 1.0) * (maxY - minY);
                        writer.printf("%-20.15f %-20.15f %-20.15f\n", x, y, 0.0);
                    }
                }
            } catch (IOException e) {
                System.out.println("Unable to create mesh.");
            }

            BoundaryCondition stationaryWall = new WallBC(govEqn, new Vector(0, 0, 0));
            BoundaryCondition movingLid = new WallBC(govEqn, new Vector(lidVelocity, 0, 0));
            Mesh mesh = null;
            try {
                mesh = new Structured2DMesh(tempMeshFile, govEqn.numVars(), stationaryWall, stationaryWall, stationaryWall, movingLid);
                if (!tempMeshFile.delete()) {
                    System.out.println("Unable to delete temporary file: " + tempMeshFile);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Mesh file is not found.");
            }
            return mesh;
        }

        private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                p -> new double[]{0, 0, 0, 0});


        CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        private final ConvectionResidual convectionResidual = new ConvectionResidual(
                new VKLimiterReconstructor(mesh, cellNeighborCalculator),
                new RusanovRiemannSolver(govEqn), mesh);
        private final DiffusionResidual diffusionResidual = new DiffusionResidual(mesh, govEqn);
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, cellNeighborCalculator);
        private final SpaceDiscretization spaceDiscretization = new SpaceDiscretization(mesh,
                cellGradientCalculator,
                List.of(convectionResidual, diffusionResidual));
        private final TimeStep timeStep = new LocalTimeStep(mesh, govEqn);

        private final TimeIntegrator timeIntegrator =
                new ExplicitEulerTimeIntegrator(mesh, spaceDiscretization, timeStep, govEqn.numVars());

        private final Convergence convergence = new Convergence(DoubleArray.newFilledArray(govEqn.numVars(), 1e-3));

        private final Config config = createConfig();

        private Config createConfig() {
            Config config = new Config();
            config.setMaxIterations(10000);
            return config;
        }

        @Override
        public String description() {
            return "Lid Driven Cavity.";
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
            return config;
        }
    };

    @Test
    public void solver() {
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);
        Config config = problem.config();
        Convergence convergence = problem.convergence();

        int maxIter = config.getMaxIterations();
        int iter = 0;
        for (; iter < maxIter; iter++) {
            timeIntegrator.updateCellAverages();
            double[] residual = timeIntegrator.currentTotalResidual(config.getConvergenceNorm());
            if (iter % 100 == 0) {
                System.out.println(iter + ": " + Arrays.toString(residual));
            }
            if (convergence.hasConverged(residual)) {
                System.out.println(iter + ": " + Arrays.toString(residual));
                System.out.println("Converged.");
                break;
            }
        }

        new VTKWriter(new File("test/test_data/lid_driven_cavity2d.vtu"), mesh, problem.govEqn()).write();
        Assert.assertEquals(3020, iter);
    }
}