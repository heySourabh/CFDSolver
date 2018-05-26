package main.solver;

public interface TimeStep {
    void updateCellTimeSteps(double courantNum);
}
