package main;

import main.geom.Point;
import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.VolumeFractionAdvectionEquations;
import main.solver.*;
import main.solver.convection.ConvectionResidual;
import main.solver.convection.reconstructor.SolutionReconstructor;
import main.solver.convection.reconstructor.VKLimiterReconstructor;
import main.solver.convection.riemann.HLLRiemannSolver;
import main.solver.convection.riemann.RiemannSolver;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.time.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SolverTransient2DVolumeFractionAdvectionTest {

    private ProblemDefinition createProblemDef() {
        return new ProblemDefinition() {
            private final double minX = 0;
            private final double minY = 0;
            private final double Lx = 4;
            private final double Ly = 4;
            private final int numXCells = 30;
            private final int numYCells = 30;

            private final String description = "2D Volume fraction advection.";
            private final GoverningEquations govEqn = createGovEqn();

            private GoverningEquations createGovEqn() {
                return new VolumeFractionAdvectionEquations();
            }

            private final Mesh mesh = create2DMesh();

            private Mesh create2DMesh() {
                int numXNodes = numXCells + 1;
                int numYNodes = numYCells + 1;
                File tempMeshFile = new File("test/test_data/temp.cfdu");

                try (FileWriter fileWriter = new FileWriter(tempMeshFile);
                     PrintWriter writer = new PrintWriter(fileWriter)) {
                    writer.write("dimension = 2\n");
                    writer.write("mode = ASCII\n");
                    writer.printf("xi = %d\n", numXNodes);
                    writer.printf("eta = %d\n", numYNodes);
                    for (int i = 0; i < numXNodes; i++) {
                        double x = minX + i / (numXNodes - 1.0) * Lx;
                        for (int j = 0; j < numYNodes; j++) {
                            double y = minY + j / (numYNodes - 1.0) * Ly;
                            writer.printf("%-20.15f %-20.15f %-20.15f\n", x, y, 0.0);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Unable to create mesh.");
                }

                BoundaryCondition extrapolatedBC = new ExtrapolatedBC(govEqn);
                Mesh mesh = null;
                try {
                    mesh = new Structured2DMesh(tempMeshFile, govEqn.numVars(), extrapolatedBC, extrapolatedBC, extrapolatedBC, extrapolatedBC);
                    if (!tempMeshFile.delete()) {
                        System.out.println("Unable to delete temporary file: " + tempMeshFile);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Mesh file is not found.");
                }
                return mesh;
            }

            private CellNeighborCalculator cellNeighborCalculator = new FaceBasedCellNeighbors();
            private CellGradientCalculator cellGradientCalculator = new LeastSquareCellGradient(
                    mesh, cellNeighborCalculator);
            private final SolutionReconstructor reconstructor
                    = new VKLimiterReconstructor(mesh, govEqn, cellNeighborCalculator);
            private final RiemannSolver riemannSolver = new HLLRiemannSolver(govEqn);
            List<ResidualCalculator> residuals = List.of(
                    new ConvectionResidual(reconstructor, riemannSolver, mesh),
                    new DiffusionResidual(mesh, govEqn));
            private final SpaceDiscretization spaceDiscretization = new SpaceDiscretization(
                    mesh, cellGradientCalculator, residuals);
            TimeStep timeStep = new LocalTimeStep(mesh, govEqn);
            private final TimeIntegrator timeIntegrator = new ExplicitSSPRK2TimeIntegrator(
                    mesh, spaceDiscretization, timeStep, govEqn.numVars());

            private final Convergence convergence
                    = new Convergence(new double[]{
                    1e-3, 1e-3, 1e-3, 1e-3, 1e-3, 1e-3, 1e-3
            });

            private final Config config = createConfig();

            private Config createConfig() {
                Config config = new Config();
                config.setMaxIterations(200);
                try {
                    config.setWorkingDirectory(new File("test/test_data/volume_fraction_advection/"));
                } catch (IOException e) {
                    System.out.println("Unable to create working directory.");
                }
                return config;
            }

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
                double radius = 0.5;
                Point circleCenter = new Point(2, 2.75, 0);
                double slotWidth = 0.20;
                double slotY = circleCenter.y + 0.1;
                Point domainCenter = new Point(2, 2, 0);
                double omega = 2.0 * Math.PI;
                return new FunctionInitializer(p -> {
                    double dist = new Vector(p, circleCenter).mag();
                    double C = dist < radius ? 1 : 0;
                    C = p.y < slotY && Math.abs(p.x - circleCenter.x) < slotWidth / 2 ? 0 : C;
                    double u = -omega * (p.y - domainCenter.y);
                    double v = omega * (p.x - domainCenter.x);
                    double w = 0.0;
                    return new double[]{C, u, v, w, 0, 0, 0};
                });
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
    }

    private void run(ProblemDefinition problem) {
        Mesh mesh = problem.mesh();
        GoverningEquations govEqn = problem.govEqn();
        Config config = problem.config();
        Convergence convergence = problem.convergence();
        TimeIntegrator timeIntegrator = problem.timeIntegrator();
        timeIntegrator.setCourantNum(1.0);

        double real_dt = 0.001;
        int numRealTimeSteps = 20;
        TimeDiscretization timeDiscretization = new TwoPointTimeDiscretization(
                mesh, govEqn, real_dt);
        timeIntegrator.setTimeDiscretization(timeDiscretization);

        problem.solutionInitializer().initialize(mesh, govEqn);
        double[] initialC = saveC(mesh);

        int[] iterationCount = new int[numRealTimeSteps];

        VTKWriter vtkWriter = new VTKWriter(mesh, govEqn);
        double time = 0;
        for (int realTimeStep = 0; realTimeStep < numRealTimeSteps; realTimeStep++) {
            System.out.println("Time: " + time);
            vtkWriter.write(new File(config.getWorkingDirectory(), String.format("sol%05d.vtu", realTimeStep)));
            int totalIterations = 0;
            for (int iter = 0; iter < config.getMaxIterations(); iter++) {
                timeIntegrator.updateCellAverages();
                setupInterfaceNormal(mesh);
                double[] totalResidual = timeIntegrator.currentTotalResidual(config.getConvergenceNorm());
                System.out.println(iter + ": " + Arrays.toString(totalResidual));
                if (convergence.hasConverged(totalResidual)) {
                    System.out.println("Converged.");
                    totalIterations = iter + 1;
                    break;
                }
            }
            iterationCount[realTimeStep] = (totalIterations == 0)
                    ? config.getMaxIterations() : totalIterations;

            timeDiscretization.shiftSolution();

            time += real_dt;
            if (realTimeStep == 0) {
                timeDiscretization = new ThreePointTimeDiscretization(mesh, govEqn, real_dt);
                timeIntegrator.setTimeDiscretization(timeDiscretization);
            }
        }
        vtkWriter.write(new File(config.getWorkingDirectory(), String.format("sol%05d.vtu", numRealTimeSteps)));

        int[] expectedIterationCount = {
                19, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13
        };
        System.out.println(Arrays.toString(iterationCount));
        System.out.println("Iteration summary = " + IntStream.of(iterationCount).summaryStatistics());

        double[] finalC = saveC(mesh);
        double error = errorNorm(initialC, finalC);
        System.out.println("Error : " + error);

        Assert.assertArrayEquals(expectedIterationCount, iterationCount);
    }

    @Test
    public void solver() {
        ProblemDefinition problem = createProblemDef();
        run(problem);
    }

    public static void main(String[] args) {
        new SolverTransient2DVolumeFractionAdvectionTest().solver();
    }

    private void setupInterfaceNormal(Mesh mesh) {
        mesh.cellStream().forEach(this::setupInterfaceNormal);
    }

    private final Vector zeroVector = new Vector(0, 0, 0);

    private void setupInterfaceNormal(Cell cell) {
        Vector gradC = cell.gradientU[0];
        double magGradC = gradC.mag();

        Vector interfaceNormal = magGradC > 1e-6 ? gradC.mult(1.0 / magGradC) : zeroVector;

        cell.U[4] = interfaceNormal.x;
        cell.U[5] = interfaceNormal.y;
        cell.U[6] = interfaceNormal.z;
    }

    private double[] saveC(Mesh mesh) {
        return mesh.cellStream()
                .mapToDouble(cell -> cell.U[0])
                .toArray();
    }

    private double errorNorm(double[] initialC, double[] finalC) {
        double sumSqr = IntStream.range(0, initialC.length)
                .mapToDouble(i -> initialC[i] - finalC[i])
                .map(diff -> diff * diff)
                .sum() / initialC.length;

        return Math.sqrt(sumSqr);
    }
}