package main.solver.time;

public interface TimeStep {
    void updateCellTimeSteps(double courantNum);
}
