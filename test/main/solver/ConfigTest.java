package main.solver;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void getWorkingDirectory() {
        Config config = new Config();
        File defaultPath = new File("./solver/");
        assertEquals(defaultPath, config.getWorkingDirectory());
    }

    @Test
    public void setWorkingDirectory() throws IOException {
        Config config = new Config();
        File workingDirectoryPath = new File("./test/test_data/test_wd");
        config.setWorkingDirectory(workingDirectoryPath);
        assertTrue(workingDirectoryPath.exists());
        if (!workingDirectoryPath.delete()) {
            System.out.println("Unable to delete created folder: " + workingDirectoryPath.toString());
        }
    }

    @Test
    public void setAndGetMaxIterations() {
        Config config = new Config();
        int maxIter = 123478;
        config.setMaxIterations(maxIter);
        assertEquals(maxIter, config.getMaxIterations());
    }

    @Test
    public void setAndGetConvergenceNorm() {
        Config config = new Config();
        Norm norm = Norm.INFINITY_NORM;
        config.setConvergenceNorm(norm);
        assertEquals(norm, config.getConvergenceNorm());

        norm = Norm.TWO_NORM;
        config.setConvergenceNorm(norm);
        assertEquals(norm, config.getConvergenceNorm());

        norm = Norm.ONE_NORM;
        config.setConvergenceNorm(norm);
        assertEquals(norm, config.getConvergenceNorm());
    }
}