package main;

import main.geom.Point;
import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.InviscidWallVOFBC;
import main.physics.bc.WallVOFBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.HLLC_VOF_RiemannSolver;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.source.SourceResidual;
import main.solver.time.*;
import main.util.DoubleArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SolverDropSplash2DTest {
    private final double beta = 1;
    private final int numXCells = 10;
    private final int numYCells = 20;

    private final ProblemDefinition problem = new ProblemDefinition() {
        private final double Lx = 0.007;
        private final double Ly = 0.014;
        private final double rho1 = 1000.0;
        private final double rho2 = 1.125;
        private final double mu = 0.0;
        private final Vector gravity = new Vector(0, -9.81, 0);

        private final ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu, rho2, mu, gravity, beta);

        Mesh mesh = create2DMesh();

        private Mesh create2DMesh() {
            int numXNodes = numXCells + 1;
            int numYNodes = numYCells + 1;
            double minX = 0, minY = 0;
            double maxX = minX + Lx;
            double maxY = minY + Ly;
            File path = new File("test/test_data/");
            File tempMeshFile = new File(path, "tempFile.cfds");
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    System.out.println("Unable to create directories");
                }
            }

            try (PrintWriter writer = new PrintWriter(tempMeshFile)) {
                writer.println("dimension = 2");
                writer.println("mode = ASCII");
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

            BoundaryCondition inviscidWall = new InviscidWallVOFBC(govEqn);
            BoundaryCondition viscousWall = new WallVOFBC(govEqn, new Vector(0, 0, 0));
            Mesh mesh = null;
            try {
                mesh = new Structured2DMesh(tempMeshFile, govEqn.numVars(), inviscidWall, inviscidWall, viscousWall, viscousWall);
                if (!tempMeshFile.delete()) {
                    System.out.println("Unable to delete temporary file: " + tempMeshFile);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Mesh file is not found.");
            }
            return mesh;
        }

        private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                p -> new double[]{0, 0, 0, 0, C(p)});

        private double C(Point p) {
            if (p.y < 0.0088) return 1.0;
            Point dropCenter = new Point(Lx / 2.0, 0.0105, 0);
            if (dropCenter.distance(p) < 2.8e-3 / 2.0) return 1.0;

            return 0.0;
        }


        CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, cellNeighborCalculator);
        private final ConvectionResidual convectionResidual = new ConvectionResidual(
                new VKLimiterReconstructor(mesh, cellGradientCalculator, cellNeighborCalculator),
                new HLLC_VOF_RiemannSolver(govEqn), mesh);
        private final DiffusionResidual diffusionResidual = new DiffusionResidual(mesh, govEqn);
        private final SourceResidual sourceResidual = new SourceResidual(mesh, govEqn);
        private final SpaceDiscretization spaceDiscretization = new SpaceDiscretization(mesh,
                List.of(convectionResidual, diffusionResidual, sourceResidual));
        private final TimeStep timeStep = new LocalTimeStep(mesh, govEqn);

        private final TimeIntegrator timeIntegrator =
                new ExplicitSSPRK2TimeIntegrator(mesh, spaceDiscretization, timeStep, govEqn.numVars());

        private final Convergence convergence = new Convergence(DoubleArray.newFilledArray(govEqn.numVars(), 1e-3));

        private final Config config = createConfig();

        private Config createConfig() {
            Config config = new Config();
            config.setMaxIterations(10000);
            return config;
        }

        @Override
        public String description() {
            return "Drop Splash";
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
        new SolverDropSplash2DTest().solver();
    }

    @Test
    public void solver() throws IOException {
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);
        TimeDiscretization timeDiscretization = new TwoPointTimeDiscretization(mesh, problem.govEqn(), 0.01);
        timeIntegrator.setTimeDiscretization(timeDiscretization);
        Config config = problem.config();
        Convergence convergence = problem.convergence();

        int maxPseudoIter = config.getMaxIterations();
        int numRealIter = 5;

        int[] expectedPseudoIterations = {
                996, 543, 513, 609, 522
        };

        int[] actualPseudoIterations = new int[numRealIter];

        File outputFolder = new File("test/test_data/drop_splash/");
        if (!outputFolder.mkdirs() && !outputFolder.exists())
            throw new IOException("Unable to create required folders for writing output.");
        double time = 0;
        for (int real_time_iter = 0; real_time_iter < numRealIter; real_time_iter++) {
            new VTKWriter(new File(outputFolder,
                    String.format("sol_%05d.vtu", real_time_iter)),
                    mesh, problem.govEqn()).write();
            System.out.println("Time: " + time);
            int pseudoIter = 0;
            for (; pseudoIter < maxPseudoIter; pseudoIter++) {
                timeIntegrator.updateCellAverages();
                double[] residual = timeIntegrator.currentTotalResidual(config.getConvergenceNorm());
                System.out.println(pseudoIter + ": " + Arrays.toString(residual));
                if (convergence.hasConverged(residual)) {
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
            time += timeDiscretization.dt();
        }
        new VTKWriter(new File(outputFolder,
                String.format("sol_%05d.vtu", numRealIter)),
                mesh, problem.govEqn()).write();

        System.out.println(Arrays.toString(actualPseudoIterations));
        System.out.println("beta = " + beta);
        System.out.println("mesh: " + numXCells + " x " + numYCells);
        try (FileWriter numItersWriter = new FileWriter(new File(outputFolder, "01_num_iters.dat"))) {
            numItersWriter.write(Arrays.stream(actualPseudoIterations)
                    .mapToObj(iter -> "" + iter)
                    .collect(Collectors.joining(", ")));
        }
        Assert.assertArrayEquals(expectedPseudoIterations, actualPseudoIterations);
    }
}