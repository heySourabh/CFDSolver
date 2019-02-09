package main;

import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Structured1DMesh;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.GoverningEquations;
import main.physics.goveqn.factory.ScalarDiffusion;
import main.solver.*;
import main.solver.diffusion.DiffusionResidual;
import main.solver.problem.ProblemDefinition;
import main.solver.time.ExplicitEulerTimeIntegrator;
import main.solver.time.LocalTimeStep;
import main.solver.time.TimeIntegrator;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Solver1DScalarDiffusionTest {

    private double courantNumber = 1.5;
    private GoverningEquations govEqn = createGovEqn();

    private GoverningEquations createGovEqn() {
        return new ScalarDiffusion(1.5);
    }

    private Mesh mesh = createMesh();

    private Mesh createMesh() {
        Mesh mesh;
        try {
            File meshFile = new File("test/test_data/tempMeshFile.cfds");
            try (FileWriter writer = new FileWriter(meshFile)) {
                writer.write("" +
                        "dimension = 1\n" +
                        "mode      = ASCII\n" +
                        "xi        = 6\n" +
                        "-1.0  2.0   3.0  \n" +
                        "0.2   3.2   1.4  \n" +
                        "1.4   4.4   -0.2 \n" +
                        "2.6   5.6   -1.8 \n" +
                        "3.8   6.8   -3.4 \n" +
                        "5.0   8.0   -5.0 \n");
            }

            mesh = new Structured1DMesh(meshFile, govEqn.numVars(), new ExtrapolatedBC(govEqn), new ExtrapolatedBC(govEqn));

            if (!meshFile.delete()) {
                System.out.println("Unable to delete temporary file: " + meshFile);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create mesh.");
        }

        return mesh;
    }

    private TimeIntegrator timeIntegrator = createTimeIntegrator();

    private TimeIntegrator createTimeIntegrator() {
        CellGradientCalculator cellGradientCalculator = new ZeroCellGradient(mesh);
        return new ExplicitEulerTimeIntegrator(mesh,
                new SpaceDiscretization(mesh, cellGradientCalculator, List.of(new DiffusionResidual(mesh, govEqn))),
                new LocalTimeStep(mesh, govEqn), govEqn.numVars());
    }

    private Config config = createConfig();

    private Config createConfig() {
        Config config = new Config();
        config.setMaxIterations(10);

        return config;
    }

    private ProblemDefinition problem = new ProblemDefinition() {
        @Override
        public String description() {
            return "Problem definition for testing diffusion.";
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
            return new FunctionInitializer(p ->
                    p.x > 1.3 && p.x < 2.7
                            ? new double[]{2.0}
                            : new double[]{1.0});
        }

        @Override
        public TimeIntegrator timeIntegrator() {
            return timeIntegrator;
        }

        @Override
        public Convergence convergence() {
            return new Convergence(new double[]{1e-3});
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
        timeIntegrator.setCourantNum(courantNumber);
        int iter = 0;
        for (; iter < problem.config().getMaxIterations(); iter++) {
            Cell ci = mesh.cells().get(2);
            Cell cim1 = mesh.cells().get(1);
            Cell cip1 = mesh.cells().get(3);
            double expectedSol = expectedValueOfCell(cim1, ci, cip1);
            timeIntegrator.updateCellAverages();
            double actualSol = ci.U[0];
            assertEquals(expectedSol, actualSol, 1e-15);
        }
        new VTKWriter(mesh, problem.govEqn()).write(new File("test/test_data/scalar1D_diffusion.vtu"));
    }

    private double expectedValueOfCell(Cell cim1, Cell ci, Cell cip1) {
        double uim1 = cim1.U[0];
        double ui = ci.U[0];
        double uip1 = cip1.U[0];

        double dxSqr = new Vector(ci.shape.centroid, cip1.shape.centroid).magSqr();
        double d2u_dx2 = (uim1 - 2.0 * ui + uip1) / dxSqr;

        double alpha = govEqn.diffusion().maxAbsDiffusivity(ci.U);
        double dt = courantNumber * dxSqr / 8.0 / alpha;

        return ui + dt * alpha * d2u_dx2;
    }
}
