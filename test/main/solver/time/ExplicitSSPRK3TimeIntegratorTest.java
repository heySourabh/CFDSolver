package main.solver.time;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.bc.BoundaryCondition;
import main.physics.bc.ExtrapolatedBC;
import main.physics.goveqn.*;
import main.solver.*;
import main.solver.source.SourceResidual;
import main.util.DoubleArray;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static main.util.DoubleArray.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExplicitSSPRK3TimeIntegratorTest {

    @Test
    public void updateCellAverages() throws Exception {
        // Create governing equation (with 2 variables) such that
        // the time-step calculation should be predictable using the convective eigenvalues
        // and the source term is defined as a function of variables
        GoverningEquations govEqn = createGovEqn();

        double[] Un = {
                0.4, 78.5
        };
        // Create a mesh having a single cell
        Mesh mesh = createMesh(govEqn);
        Cell cell = mesh.cells().get(0);

        // calculate time-step
        double ev = govEqn.convection().maxAbsEigenvalues(Un, null);
        double dt = cell.shape.volume / (ev * 0.2 * 2 + ev * 0.5 * 2);

        // update time-step based on CFL
        double courantNum = 0.8;
        dt *= courantNum;

        // Using SSPRK formula calculate the expected updated value of variables
        double[] U0 = copyOf(Un);
        // step 1
        double[] residual = multiply(sourceTerms(U0), dt);
        double[] U1 = add(U0, residual);

        // step 2
        residual = multiply(sourceTerms(U1), dt);
        double[] U2 = add(add(multiply(U0, 3.0 / 4.0),
                        multiply(U1, 1.0 / 4.0)),
                multiply(residual, 1.0 / 4.0));

        // step 3
        residual = multiply(sourceTerms(U2), dt);
        double[] U3 = add(add(multiply(U0, 1.0 / 3.0),
                        multiply(U2, 2.0 / 3.0)),
                multiply(residual, 2.0 / 3.0));

        // Update the solution by calling the method
        CellNeighborCalculator cellNeighborCalculator = new NodeBasedCellNeighbors();
        SpaceDiscretization spaceDiscretization = new SpaceDiscretization(mesh,
                new LeastSquareCellGradient(mesh, cellNeighborCalculator),
                List.of(new SourceResidual(mesh, govEqn))
        );
        TimeIntegrator timeIntegrator = new ExplicitSSPRK3TimeIntegrator(mesh,
                spaceDiscretization,
                new LocalTimeStep(mesh, govEqn),
                govEqn.numVars());
        timeIntegrator.setCourantNum(courantNum);

        copy(Un, cell.U);
        timeIntegrator.updateCellAverages();

        // assert that time step is calculated properly
        assertEquals(dt, cell.dt, 1e-15);

        // assert that the expected value is same as the calculated value
        assertArrayEquals(U3, cell.U, 1e-15);

        // assert that the expected residual is same as calculated residual
        double[] totalResidual = divide(abs(subtract(U3, U0)), dt);
        double[] varMag = abs(U3);
        totalResidual = divide(totalResidual, varMag);

        assertArrayEquals(totalResidual, timeIntegrator.currentTotalResidual(Norm.ONE_NORM), 1e-15);
    }

    private GoverningEquations createGovEqn() {
        return new GoverningEquations() {
            @Override
            public String description() {
                return "Dummy";
            }

            @Override
            public int numVars() {
                return 2;
            }

            @Override
            public String[] conservativeVarNames() {
                return new String[]{
                        "U0", "U1"
                };
            }

            @Override
            public String[] primitiveVarNames() {
                return new String[]{
                        "U0", "U1"
                };
            }

            @Override
            public double[] primitiveVars(double[] conservativeVars) {
                return DoubleArray.copyOf(conservativeVars);
            }

            @Override
            public double[] conservativeVars(double[] primitiveVars) {
                return DoubleArray.copyOf(primitiveVars);
            }

            @Override
            public Limits[] physicalLimits() {
                return new Limits[]{
                        Limits.INFINITE,
                        Limits.INFINITE
                };
            }

            @Override
            public Convection convection() {
                return new Convection() {
                    @Override
                    public double[] flux(double[] conservativeVars, Vector unitNormal) {
                        return new double[numVars()];
                    }

                    @Override
                    public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
                        return new double[numVars()];
                    }

                    @Override
                    public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
                        return conservativeVars[0] + conservativeVars[1] * 0.14; // some arbitrary combination
                    }
                };
            }

            @Override
            public Diffusion diffusion() {
                return new ZeroDiffusion(numVars());
            }

            @Override
            public Source source() {
                return (at, conservativeVars, gradConservativeVars) -> sourceTerms(conservativeVars);
            }
        };
    }

    private double[] sourceTerms(double[] U) { // an arbitrary source vector
        return new double[]{
                U[0] + U[1],
                U[0] * U[0] + 2 * U[1]
        };
    }

    private Mesh createMesh(GoverningEquations govEqn) throws Exception {
        double dx = 0.2;
        double dy = 0.5;

        String meshFileContents = "" +
                                  "dimension = 2\n" +
                                  "mode = ASCII\n" +
                                  "xi = 2\n" +
                                  "eta = 2\n" +
                                  "0 0 0\n" +
                                  "0 " + dy + " 0\n" +
                                  dx + " 0 0\n" +
                                  dx + " " + dy + " 0\n";

        File meshFile = new File("test/test_data/tempMeshFile.cfds");

        try (FileWriter fileWriter = new FileWriter(meshFile)) {
            fileWriter.write(meshFileContents);
        }

        BoundaryCondition bc = new ExtrapolatedBC(govEqn);

        Structured2DMesh mesh = new Structured2DMesh(meshFile, govEqn.numVars(), bc, bc, bc, bc);

        Files.delete(meshFile.toPath());

        return mesh;
    }
}