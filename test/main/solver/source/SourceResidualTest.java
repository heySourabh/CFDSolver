package main.solver.source;

import main.geom.Point;
import main.geom.Vector;
import main.mesh.Cell;
import main.mesh.Mesh;
import main.mesh.factory.Structured2DMesh;
import main.physics.goveqn.*;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SourceResidualTest {

    @Test
    public void updateCellResiduals() {
        GoverningEquations govEqn = createGovEqn();
        Mesh mesh = create2DMesh(20, 30, govEqn);

        // set up cell U and gradientU
        for (Cell cell : mesh.cells()) {
            for (int var = 0; var < govEqn.numVars(); var++) {
                Point p = cell.shape.centroid;
                double avgVal = p.x * 2 + p.y * p.x + p.z * (5.0 + var * 2.1);
                Vector gradVal = new Vector(4.5 * p.x, 7.5 * p.y, 8.3 * p.x + 7 * p.z + var);

                cell.U[var] = avgVal;
                cell.gradientU[var] = gradVal;
            }
        }

        SourceResidual sourceResidual = new SourceResidual(mesh, govEqn);
        sourceResidual.updateCellResiduals();

        for (Cell cell : mesh.cells()) {
            double[] expectedSource = new double[govEqn.numVars()];
            for (int i = 0; i < expectedSource.length; i++) {
                expectedSource[i] = -test_source(cell.U, cell.gradientU)[i] * cell.shape.volume;
            }

            assertArrayEquals(expectedSource, cell.residual, 1e-15);
        }
    }

    private Mesh create2DMesh(int numXCells, int numYCells, GoverningEquations govEqn) {
        int numXNodes = numXCells + 1;
        int numYNodes = numYCells + 1;
        double minX = -4, minY = 3;
        double Lx = 5;
        double Ly = 0.8;
        double maxX = minX + Lx;
        double maxY = minY + Ly;
        File tempMeshFile = new File("test/test_data/tempFile");

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

        Mesh mesh = null;
        try {
            mesh = new Structured2DMesh(tempMeshFile, govEqn.numVars(), null, null, null, null);
            if (!tempMeshFile.delete()) {
                System.out.println("Unable to delete temporary file: " + tempMeshFile);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Mesh file is not found.");
        }
        return mesh;
    }

    private GoverningEquations createGovEqn() {
        return new GoverningEquations() {
            @Override
            public String description() {
                return null;
            }

            @Override
            public int numVars() {
                return 3;
            }

            @Override
            public String[] conservativeVarNames() {
                return new String[0];
            }

            @Override
            public String[] primitiveVarNames() {
                return new String[0];
            }

            @Override
            public double[] primitiveVars(double[] conservativeVars) {
                return new double[0];
            }

            @Override
            public double[] conservativeVars(double[] primitiveVars) {
                return new double[0];
            }

            @Override
            public Limits[] physicalLimits() {
                return new Limits[0];
            }

            @Override
            public Convection convection() {
                return null;
            }

            @Override
            public Diffusion diffusion() {
                return null;
            }

            Source source = (at, conservativeVars, gradConservativeVars) -> test_source(conservativeVars, gradConservativeVars);

            @Override
            public Source source() {
                return source;
            }
        };
    }

    private double[] test_source(double[] conservativeVars, Vector[] gradConservativeVars) {
        return new double[]{
                conservativeVars[0] * 5.8 + gradConservativeVars[0].x,
                conservativeVars[1] * 0.0 + gradConservativeVars[1].y + gradConservativeVars[2].x,
                conservativeVars[2] * -0.8 * gradConservativeVars[2].z
        };
    }
}