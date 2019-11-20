package main;

import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Structured3DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.GrainGrowthFanChenEquations;
import main.solver.*;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.source.SourceResidual;
import main.solver.time.*;
import main.util.DoubleArray;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static main.util.DoubleArray.newFilledArray;

public class SolverGrainGrowth3DTest {

    private final ProblemDefinition problem = new ProblemDefinition() {
        private final int numOrientations = 6;
        private final double alpha = 1.0;
        private final double beta = 1.0;
        private final double gamma = 1.0;
        private final double[] kappa = newFilledArray(numOrientations, 0.0001);
        private final double[] L = newFilledArray(numOrientations, 1);

        private final GrainGrowthFanChenEquations govEqn
                = new GrainGrowthFanChenEquations(numOrientations, alpha, beta, gamma, kappa, L);

        private final Mesh mesh = create3DMesh(40, 40, 40);

        private Mesh create3DMesh(int numXCells, int numYCells, int numZCells) {
            int numXNodes = numXCells + 1;
            int numYNodes = numYCells + 1;
            int numZNodes = numZCells + 1;
            double minX = 0, minY = 0, minZ = 0, length = 1.0;
            double maxX = minX + length;
            double maxY = minY + length;
            double maxZ = minY + length;
            File tempMeshFile = new File("test/test_data/temp_mesh.cfds");

            try (FileWriter fileWriter = new FileWriter(tempMeshFile);
                 PrintWriter writer = new PrintWriter(fileWriter)) {
                writer.write("dimension = 3\n");
                writer.write("mode = ASCII\n");
                writer.printf("xi = %d\n", numXNodes);
                writer.printf("eta = %d\n", numYNodes);
                writer.printf("zeta = %d\n", numZNodes);
                for (int i = 0; i < numXNodes; i++) {
                    double x = minX + i / (numXNodes - 1.0) * (maxX - minX);
                    for (int j = 0; j < numYNodes; j++) {
                        double y = minY + j / (numYNodes - 1.0) * (maxY - minY);
                        for (int k = 0; k < numZNodes; k++) {
                            double z = minZ + k / (numZNodes - 1.0) * (maxZ - minZ);
                            writer.printf("%-20.15f %-20.15f %-20.15f\n", x, y, z);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Unable to create mesh.");
            }

            BoundaryCondition extrapolatedBC = new ExtrapolatedBC(govEqn);
            Mesh mesh = null;
            try {
                mesh = new Structured3DMesh(tempMeshFile, govEqn.numVars(),
                        extrapolatedBC, extrapolatedBC,
                        extrapolatedBC, extrapolatedBC,
                        extrapolatedBC, extrapolatedBC);
                if (!tempMeshFile.delete()) {
                    System.out.println("Unable to delete temporary file: " + tempMeshFile);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Mesh file is not found.");
            }
            return mesh;
        }

        Random random = new Random(1324);
        private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                p -> DoubleArray.random(numOrientations, random, -0.1, 0.1));


        CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        private final DiffusionResidual diffusionResidual = new DiffusionResidual(mesh, govEqn);
        private final SourceResidual sourceResidual = new SourceResidual(mesh, govEqn);
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, cellNeighborCalculator);
        private final SpaceDiscretization spaceDiscretization = new SpaceDiscretization(
                mesh, cellGradientCalculator, List.of(diffusionResidual, sourceResidual));
        private final TimeStep timeStep = new LocalTimeStep(mesh, govEqn);

        private final TimeIntegrator timeIntegrator =
                new ExplicitEulerTimeIntegrator(mesh, spaceDiscretization, timeStep, govEqn.numVars());

        private final Convergence convergence = new Convergence(newFilledArray(govEqn.numVars(), 1e-3));

        private final Config config = createConfig();

        private Config createConfig() {
            Config config = new Config();
            config.setMaxIterations(10_000);
            return config;
        }

        @Override
        public String description() {
            return "Grain Growth.";
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

    public static void main(String[] args) throws IOException {
        new SolverGrainGrowth3DTest().solver();
    }

    public void solver() throws IOException {
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);
        double real_dt = 0.1;
        TimeDiscretization timeDiscretization = new TwoPointTimeDiscretization(mesh, problem.govEqn(), real_dt);
        timeIntegrator.setTimeDiscretization(timeDiscretization);
        Config config = problem.config();
        Convergence convergence = problem.convergence();

        int maxPseudoIter = config.getMaxIterations();
        int numRealIter = 1000;

        File outputFolder = new File("test/test_data/grain_growth_3d/");
        if (!outputFolder.mkdirs() && !outputFolder.exists())
            throw new IOException("Unable to create required folders for writing output.");
        double time = 0;
        for (int real_time_iter = 0; real_time_iter < numRealIter; real_time_iter++) {
            System.out.println(String.format("Time = %1.5f", time));
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
            timeDiscretization.shiftSolution();
            if (real_time_iter == 0) {
                timeDiscretization = new ThreePointTimeDiscretization(mesh, problem.govEqn(), timeDiscretization.dt());
                timeIntegrator.setTimeDiscretization(timeDiscretization);
            }
            time += real_dt;
        }
        new VTKWriter(mesh, problem.govEqn()).write(new File(outputFolder,
                String.format("sol_%05d.vtu", numRealIter)));
    }
}