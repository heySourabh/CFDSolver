package main.solver.time;

public interface TimeDiscretization {

    void updateCellResiduals();

    double dt();

    void shiftSolution();
}
