package main;

import main.geom.Vector;
import main.io.VTKWriter;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
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

public class Solver2DScalarDiffusionTest {

    private double courantNumber = 1.5;
    private GoverningEquations govEqn = createGovEqn();

    private GoverningEquations createGovEqn() {
        return new ScalarDiffusion(3.5);
    }

    private Mesh mesh = create2DMesh();

    private Mesh create2DMesh() {
        Mesh mesh;
        try {
            File meshFile = new File("test/test_data/tempMeshFile.cfds");
            try (FileWriter writer = new FileWriter(meshFile)) {
                writer.write("" +
                        "dimension = 2\n" +
                        "mode      = ASCII\n" +
                        "xi        = 6\n" +
                        "eta       = 6\n" +
                        "-2.0  3.0   0.0\n" +
                        "-2.0  4.0   0.0\n" +
                        "-2.0  5.0   0.0\n" +
                        "-2.0  6.0   0.0\n" +
                        "-2.0  7.0   0.0\n" +
                        "-2.0  8.0   0.0\n" +
                        "-0.4  3.0   0.3\n" +
                        "-0.4  4.0   0.3\n" +
                        "-0.4  5.0   0.3\n" +
                        "-0.4  6.0   0.3\n" +
                        "-0.4  7.0   0.3\n" +
                        "-0.4  8.0   0.3\n" +
                        "1.2   3.0   0.6\n" +
                        "1.2   4.0   0.6\n" +
                        "1.2   5.0   0.6\n" +
                        "1.2   6.0   0.6\n" +
                        "1.2   7.0   0.6\n" +
                        "1.2   8.0   0.6\n" +
                        "2.8   3.0   0.9\n" +
                        "2.8   4.0   0.9\n" +
                        "2.8   5.0   0.9\n" +
                        "2.8   6.0   0.9\n" +
                        "2.8   7.0   0.9\n" +
                        "2.8   8.0   0.9\n" +
                        "4.4   3.0   1.2\n" +
                        "4.4   4.0   1.2\n" +
                        "4.4   5.0   1.2\n" +
                        "4.4   6.0   1.2\n" +
                        "4.4   7.0   1.2\n" +
                        "4.4   8.0   1.2\n" +
                        "6.0   3.0   1.5\n" +
                        "6.0   4.0   1.5\n" +
                        "6.0   5.0   1.5\n" +
                        "6.0   6.0   1.5\n" +
                        "6.0   7.0   1.5\n" +
                        "6.0   8.0   1.5\n");
            }

            mesh = new Structured2DMesh(meshFile, govEqn.numVars(),
                    new ExtrapolatedBC(govEqn), new ExtrapolatedBC(govEqn),
                    new ExtrapolatedBC(govEqn), new ExtrapolatedBC(govEqn));

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
            return "Problem definition for testing 2D diffusion.";
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
                    // p.x > 1.1 && p.x < 2.9
                    // &&
                    p.y > 4.9 && p.y < 6.1
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
            Cell cij = mesh.cells().get(12);
            Cell cim1j = mesh.cells().get(11);
            Cell cip1j = mesh.cells().get(13);
            Cell cijm1 = mesh.cells().get(7);
            Cell cijp1 = mesh.cells().get(17);
            double expectedSol = expectedValueOfCell(cij, cim1j, cip1j, cijm1, cijp1);
            timeIntegrator.updateCellAverages();
            double actualSol = cij.U[0];
            assertEquals(expectedSol, actualSol, 1e-4);
        }
        new VTKWriter(new File("test/test_data/scalar2D_diffusion.vtu"), mesh, problem.govEqn()).write();
    }

    private double expectedValueOfCell(Cell cij, Cell cim1j, Cell cip1j, Cell cijm1, Cell cijp1) {
        double uim1j = cim1j.U[0];
        double uij = cij.U[0];
        double uip1j = cip1j.U[0];
        double uijm1 = cijm1.U[0];
        double uijp1 = cijp1.U[0];

        double dxSqr = new Vector(cij.shape.centroid, cip1j.shape.centroid).magSqr();
        double dySqr = new Vector(cij.shape.centroid, cijp1.shape.centroid).magSqr();
        double d2u_dx2 = (uim1j - 2.0 * uij + uip1j) / dxSqr;
        double d2u_dy2 = (uijm1 - 2.0 * uij + uijp1) / dySqr;

        double alpha = govEqn.diffusion().maxAbsDiffusivity(cij.U);
        double dt = courantNumber * (dxSqr * dySqr) / 4.0 / alpha / (2.0 * dxSqr + 2.0 * dySqr);

        return uij + dt * alpha * (d2u_dx2 + d2u_dy2);
    }
}
