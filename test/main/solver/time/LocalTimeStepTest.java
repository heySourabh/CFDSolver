package main.solver.time;

import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Face;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.goveqn.*;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LocalTimeStepTest {

    private final static double maxAbsEigenvalue = 120.789;
    private final static double maxAbsDiffusivity = 1.5;
    private final static double courantNumber = 0.8;
    private final GoverningEquations convectiveGovEqn = new ConvectionGoverningEquations();
    private final GoverningEquations diffusionGovEqn = new DiffusionGoverningEquations();

    @Test
    public void timeStepWithOnlyConvection() throws IOException {
        Mesh mesh = mesh();
        TimeStep timeStep = new LocalTimeStep(mesh, convectiveGovEqn);
        timeStep.updateCellTimeSteps(courantNumber, Double.POSITIVE_INFINITY);

        for (Cell cell : mesh.cells()) {
            double expected_dt = expectedTimeStepWithOnlyConvection(cell.shape.volume, cell.faces);
            double actual_dt = cell.dt;

            assertEquals(expected_dt, actual_dt, 1e-15);
        }
    }

    @Test
    public void timeStepWithOnlyDiffusion() throws IOException {
        Mesh mesh = mesh();
        TimeStep timeStep = new LocalTimeStep(mesh, diffusionGovEqn);
        timeStep.updateCellTimeSteps(courantNumber, Double.POSITIVE_INFINITY);

        for (Cell cell : mesh.cells()) {
            double expected_dt = expectedTimeStepWithOnlyDiffusion(cell.shape.volume, cell.faces);
            double actual_dt = cell.dt;

            assertEquals(expected_dt, actual_dt, 1e-15);
        }
    }

    private double expectedTimeStepWithOnlyConvection(double volume, List<Face> faces) {
        double denominator = faces.stream()
                .mapToDouble(f -> f.surface.area)
                .map(a -> a * maxAbsEigenvalue)
                .sum();

        return courantNumber * volume / denominator;
    }

    private double expectedTimeStepWithOnlyDiffusion(double volume, List<Face> faces) {
        double denominator = faces.stream()
                .mapToDouble(f -> f.surface.area)
                .map(a -> a * a * maxAbsDiffusivity)
                .sum() * 4.0 / volume;

        return courantNumber * volume / denominator;
    }

    private Mesh mesh() throws IOException {
        File tempFile = new File("test/test_data/tempMeshFile.cfds");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("" +
                    "dimension = 2\n" +
                    "mode = ASCII\n" +
                    "xi = 4\n" +
                    "eta = 4\n" +
                    "2.000000   0.000000  0.0\n" +
                    "2.000000   1.666667  0.0\n" +
                    "2.000000   3.333333  0.0\n" +
                    "2.000000   5.000000  0.0\n" +
                    "0.000000   0.000000  0.0\n" +
                    "0.000000   1.666667  0.0\n" +
                    "0.000000   3.333333  0.0\n" +
                    "0.000000   5.000000  0.0\n" +
                    "-2.000000  0.000000  0.0\n" +
                    "-2.000000  1.666667  0.0\n" +
                    "-2.000000  3.333333  0.0\n" +
                    "-2.000000  5.000000  0.0\n" +
                    "-4.000000  0.000000  0.0\n" +
                    "-4.000000  1.666667  0.0\n" +
                    "-4.000000  3.333333  0.0\n" +
                    "-4.000000  5.000000  0.0\n");
        }

        Mesh mesh = new Structured2DMesh(tempFile, convectiveGovEqn.numVars(), null, null, null, null);

        if (!tempFile.delete()) {
            System.out.println("Unable to delete the temporary file: " + tempFile);
        }

        return mesh;
    }

    private class ConvectionGoverningEquations extends BaseGoverningEquations {
        private final Convection convection = new Convection() {
            @Override
            public double[] flux(double[] conservativeVars, Vector unitNormal) {
                return null;
            }

            @Override
            public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
                return null;
            }

            @Override
            public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
                return maxAbsEigenvalue;
            }
        };

        @Override
        public Convection convection() {
            return convection;
        }
    }

    private class DiffusionGoverningEquations extends BaseGoverningEquations {
        private final Diffusion diffusion = new Diffusion() {
            @Override
            public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
                return null;
            }

            @Override
            public double maxAbsDiffusivity(double[] conservativeVars) {
                return maxAbsDiffusivity;
            }
        };

        @Override
        public Diffusion diffusion() {
            return diffusion;
        }
    }

    private class BaseGoverningEquations implements GoverningEquations {
        @Override
        public String description() {
            return null;
        }

        @Override
        public int numVars() {
            return 0;
        }

        @Override
        public String[] conservativeVarNames() {
            return null;
        }

        @Override
        public String[] primitiveVarNames() {
            return null;
        }

        @Override
        public double[] primitiveVars(double[] conservativeVars) {
            return null;
        }

        @Override
        public double[] conservativeVars(double[] primitiveVars) {
            return null;
        }

        @Override
        public Limits[] physicalLimits() {
            return new Limits[0];
        }

        @Override
        public Convection convection() {
            return new Convection() {
                @Override
                public double[] flux(double[] conservativeVars, Vector unitNormal) {
                    return null;
                }

                @Override
                public double[] sortedEigenvalues(double[] conservativeVars, Vector unitNormal) {
                    return null;
                }

                @Override
                public double maxAbsEigenvalues(double[] conservativeVars, Vector unitNormal) {
                    return 0.0;
                }
            };
        }

        @Override
        public Diffusion diffusion() {
            return new Diffusion() {
                @Override
                public double[] flux(double[] conservativeVars, Vector[] gradConservativeVars, Vector unitNormal) {
                    return null;
                }

                @Override
                public double maxAbsDiffusivity(double[] conservativeVars) {
                    return 0.0;
                }
            };
        }

        @Override
        public Source source() {
            return null;
        }
    }
}
