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
import main.solver.time.*;
import main.util.DoubleArray;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SolverTransientLidDrivenCavity2DTest {

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


        final CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        private final ConvectionResidual convectionResidual = new ConvectionResidual(
                new VKLimiterReconstructor(mesh, govEqn, cellNeighborCalculator),
                new RusanovRiemannSolver(govEqn), mesh);
        private final DiffusionResidual diffusionResidual = new DiffusionResidual(mesh, govEqn);
        final CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, cellNeighborCalculator);
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
    public void solver() throws IOException {
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);
        TimeDiscretization timeDiscretization = new TwoPointTimeDiscretization(mesh, problem.govEqn(), 0.1);
        timeIntegrator.setTimeDiscretization(timeDiscretization);
        Config config = problem.config();
        Convergence convergence = problem.convergence();

        int maxPseudoIter = config.getMaxIterations();
        int numRealIter = 100;

        int[] expectedPseudoIterations = {
                598, 507, 640, 386, 345, 290, 242, 211, 183, 164, 145, 129, 116, 106, 97, 88, 83, 77, 73, 68, 65,
                61, 58, 55, 51, 49, 46, 43, 41, 37, 36, 32, 30, 28, 25, 23, 22, 16, 20, 11, 15, 10, 13, 8, 7, 7,
                7, 6, 6, 6, 5, 5, 5, 4, 4, 4, 4, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };

        int[] actualPseudoIterations = new int[numRealIter];

        File outputFolder = new File("test/test_data/transient_lid_driven_cavity/");
        if (!outputFolder.mkdirs() && !outputFolder.exists())
            throw new IOException("Unable to create required folders for writing output.");
        for (int real_time_iter = 0; real_time_iter < numRealIter; real_time_iter++) {
            new VTKWriter(mesh, problem.govEqn()).write(new File(outputFolder,
                    String.format("sol_%05d.vtu", real_time_iter)));
            int pseudoIter = 0;
            for (; pseudoIter < maxPseudoIter; pseudoIter++) {
                timeIntegrator.updateCellAverages();
                double[] residual = timeIntegrator.currentTotalResidual(config.getConvergenceNorm());
                if (pseudoIter % 100 == 0) {
                    System.out.println(pseudoIter + ": " + Arrays.toString(residual));
                }
                if (convergence.hasConverged(residual)) {
                    System.out.println(pseudoIter + ": " + Arrays.toString(residual));
                    System.out.println("Converged.");
                    break;
                }
            }
            actualPseudoIterations[real_time_iter] = pseudoIter;
            timeDiscretization.shiftSolution();
            if (real_time_iter == 0) {
                timeDiscretization = new ThreePointTimeDiscretization(mesh, problem.govEqn(), timeDiscretization.dt());
                timeIntegrator.setTimeDiscretization(timeDiscretization);
            }
        }
        new VTKWriter(mesh, problem.govEqn()).write(new File(outputFolder,
                String.format("sol_%05d.vtu", numRealIter)));

        assertArrayEquals(expectedPseudoIterations, actualPseudoIterations);
    }
}