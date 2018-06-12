package main.solver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Config {
    private File workingDirectory = new File("./solver/");
    private int maxIterations = 1000;
    private Norm convergenceNorm = Norm.TWO_NORM;

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) throws IOException {
        Files.createDirectories(workingDirectory.toPath());
        this.workingDirectory = workingDirectory;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Norm getConvergenceNorm() {
        return convergenceNorm;
    }

    public void setConvergenceNorm(Norm convergenceNorm) {
        this.convergenceNorm = convergenceNorm;
    }
}
