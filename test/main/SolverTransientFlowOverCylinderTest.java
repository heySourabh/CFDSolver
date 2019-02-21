package main;

import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Mesh;
import main.mesh.factory.Unstructured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.PressureOutletBC;
import main.physics.bc.VelocityInletBC;
import main.physics.bc.WallBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ArtificialCompressibilityEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.HLLRiemannSolver;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.time.*;
import main.util.DoubleArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SolverTransientFlowOverCylinderTest {

    private final ProblemDefinition problem = new ProblemDefinition() {
        private final double Re = 120;
        private final double D = 1.0;
        private final double rho = 1.0;
        private final double inletVelocity = 1.0;
        private final double mu = rho * inletVelocity * D / Re;
        private final Vector gravity = new Vector(0, 0, 0);

        private final ArtificialCompressibilityEquations govEqn
                = new ArtificialCompressibilityEquations(1.0, mu, gravity);

        private final Mesh mesh = readUnstructuredMesh();

        private Mesh readUnstructuredMesh() {
            BoundaryCondition cylinderWall = new WallBC(govEqn, new Vector(0, 0, 0));
            BoundaryCondition velocityInlet = new VelocityInletBC(govEqn, new Vector(1, 0, 0));
            BoundaryCondition pressureOutlet = new PressureOutletBC(govEqn, 101325.0);

            Mesh mesh = null;
            File meshFile = new File("test/test_data/transient_flow_over_cylinder/mesh.cfdu");
            try {
                mesh = new Unstructured2DMesh(meshFile, govEqn.numVars(),
                        Map.of("cylinder", cylinderWall,
                                "velocity inlet", velocityInlet,
                                "pressure outlet", pressureOutlet));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to read mesh from file: " + meshFile);
            }

            return mesh;
        }

        private final SolutionInitializer solutionInitializer = new FunctionInitializer(
                p -> p.x > 0.5 && p.y > -5.0 && p.y < 5.0
                        ? new double[]{101325.0, 1, 0.2, 0.0}
                        : new double[]{101325.0, 1, 0.0, 0.0});


        CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
        private final ConvectionResidual convectionResidual = new ConvectionResidual(
                new VKLimiterReconstructor(mesh, cellNeighborCalculator),
                new HLLRiemannSolver(govEqn), mesh);
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
            return "Transient flow over a cylinder.";
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
        new SolverTransientFlowOverCylinderTest().solver();
    }

    @Test
    public void solver() throws IOException {
        Mesh mesh = problem.mesh();
        problem.solutionInitializer().initialize(mesh, problem.govEqn());

        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);
        TimeDiscretization timeDiscretization = new TwoPointTimeDiscretization(mesh, problem.govEqn(), 1);
        timeIntegrator.setTimeDiscretization(timeDiscretization);
        Config config = problem.config();
        Convergence convergence = problem.convergence();

        int maxPseudoIter = config.getMaxIterations();
        int numRealIter = 50;

        int[] expectedPseudoIterations = {
                479, 464, 334, 121, 119, 79, 57, 42, 25, 22, 20, 16, 11, 8, 5, 4, 3, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };

        int[] actualPseudoIterations = new int[numRealIter];

        File outputFolder = new File("test/test_data/transient_flow_over_cylinder/");
        if (!outputFolder.mkdirs() && !outputFolder.exists())
            throw new IOException("Unable to create required folders for writing output.");
        VTKWriter vtkWriter = new VTKWriter(mesh, problem.govEqn());
        double time = 0.0;
        for (int real_time_iter = 0; real_time_iter < numRealIter; real_time_iter++) {
            System.out.println("time = " + time);
            vtkWriter.write(new File(outputFolder, String.format("sol_%05d.vtu", real_time_iter)));
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
        vtkWriter.write(new File(outputFolder, String.format("sol_%05d.vtu", numRealIter)));

        System.out.println(Arrays.toString(actualPseudoIterations));

        Assert.assertArrayEquals(expectedPseudoIterations, actualPseudoIterations);
    }
}