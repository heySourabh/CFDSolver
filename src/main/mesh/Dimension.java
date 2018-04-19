package main.mesh;

public enum Dimension {
    ONE_DIM(1), TWO_DIM(2), THREE_DIM(3);

    public final int dim;

    Dimension(int dim) {
        this.dim = dim;
    }

    public static Dimension getDimension(int dim) {
        switch (dim) {
            case 1:
                return ONE_DIM;
            case 2:
                return TWO_DIM;
            case 3:
                return THREE_DIM;
            default:
                throw new IllegalArgumentException("No enum for dim " + dim);
        }
    }
}
