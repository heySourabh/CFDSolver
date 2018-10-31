package main;

import main.geom.Point;
import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Boundary;
import main.mesh.Face;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.InviscidWallVOFBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ArtificialCompressibilityVOFEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.HLLRiemannSolver;
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
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class SolverSloshing2DHLLUnstructuredTest {
    private final double beta = 8;

    private final ProblemDefinition problem = new ProblemDefinition() {
        private final double rho1 = 1000.0;
        private final double rho2 = 1.125;
        private final double mu = 0.0;
        private final Vector gravity = new Vector(0, -9.81, 0);

        private final ArtificialCompressibilityVOFEquations govEqn
                = new ArtificialCompressibilityVOFEquations(rho1, mu, rho2, mu, gravity, beta);

        Mesh mesh = readUnstructuredMesh();

        private Mesh readUnstructuredMesh() {
            BoundaryCondition inviscidWall = new InviscidWallVOFBC(govEqn);
            Mesh mesh = null;
            File meshFile = new File("test/test_data/mesh_square_0.1x0.1.cfdu");
            try {
                mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(),
                        Map.of("Left", inviscidWall,
                                "Right", inviscidWall,
                                "Top", inviscidWall,
                                "Bottom", inviscidWall));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to read mesh from file: " + meshFile);
            }

            return mesh;
        }

        private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                p -> new double[]{0, 0, 0, 0,
                        p.y <= (0.05 + 0.005 * cos(2 * PI * p.x / 0.2)) ? 1.0 : 0});

        CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(mesh, cellNeighborCalculator);
        private final ConvectionResidual convectionResidual = new ConvectionResidual(
                new VKLimiterReconstructor(mesh, cellGradientCalculator, cellNeighborCalculator),
                new HLLRiemannSolver(govEqn), mesh);
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
            return "Sloshing";
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
        new SolverSloshing2DHLLUnstructuredTest().solver();
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
                1489, 633, 585, 585, 584
        };

        int[] actualPseudoIterations = new int[numRealIter];

        File outputFolder = new File("test/test_data/sloshing_hll_unstructured/");
        if (!outputFolder.mkdirs() && !outputFolder.exists())
            throw new IOException("Unable to create required folders for writing output.");
        double time = 0;
        for (int real_time_iter = 0; real_time_iter < numRealIter; real_time_iter++) {
            new VTKWriter(new File(outputFolder,
                    String.format("sol_%05d.vtu", real_time_iter)),
                    mesh, problem.govEqn()).write();
            saveBoundaryC(mesh, new File(outputFolder, String.format("C_%05d.dat", real_time_iter))
                    , time, "Left");
            System.out.println("Time: " + time);
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
            time += timeDiscretization.dt();
        }
        new VTKWriter(new File(outputFolder,
                String.format("sol_%05d.vtu", numRealIter)),
                mesh, problem.govEqn()).write();
        saveBoundaryC(mesh, new File(outputFolder, String.format("C_%05d.dat", numRealIter)), time, "Left");

        System.out.println(Arrays.toString(actualPseudoIterations));
        System.out.println("beta = " + beta);
        System.out.println("mesh: " + mesh.cells().size() + " cells");
        try (FileWriter numItersWriter = new FileWriter(new File(outputFolder, "01_num_iters.dat"))) {
            numItersWriter.write(Arrays.stream(actualPseudoIterations)
                    .mapToObj(iter -> "" + iter)
                    .collect(Collectors.joining(", ")));
        }

        Assert.assertArrayEquals(expectedPseudoIterations, actualPseudoIterations);
    }

    private void saveBoundaryC(Mesh mesh, File file, double time, String boundaryName) {
        Boundary boundary = getBoundary(mesh, boundaryName);

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.printf("time = %f\n", time);
            for (Face face : boundary.faces) {
                Point centroid = face.surface.centroid;
                double x = centroid.x;
                double y = centroid.y;
                double z = centroid.z;
                double C = face.left.U[4];
                writer.printf("%f %f %f %f\n", x, y, z, C);
            }
        } catch (IOException e) {
            System.out.println("Unable to write boundary data.");
        }
    }

    private Boundary getBoundary(Mesh mesh, String boundaryName) {
        return mesh.boundaryStream()
                .filter(b -> b.name.equals(boundaryName))
                .findFirst().orElseThrow();
    }
}