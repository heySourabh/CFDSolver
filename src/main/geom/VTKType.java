package main.geom;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum VTKType {
    VTK_VERTEX(1, 0),
    VTK_POLY_VERTEX(2, 0),
    VTK_LINE(3, 1),
    VTK_POLY_LINE(4, 1),
    VTK_TRIANGLE(5, 2),
    VTK_TRIANGLE_STRIP(6, 2),
    VTK_POLYGON(7, 2),
    VTK_PIXEL(8, 2),
    VTK_QUAD(9, 2),
    VTK_TETRA(10, 3),
    VTK_VOXEL(11, 3),
    VTK_HEXAHEDRON(12, 3),
    VTK_WEDGE(13, 3),
    VTK_PYRAMID(14, 3),
    VTK_QUADRATIC_EDGE(21, 1),
    VTK_QUADRATIC_TRIANGLE(22, 2),
    VTK_QUADRATIC_QUAD(23, 2),
    VTK_QUADRATIC_TETRA(24, 3),
    VTK_QUADRATIC_HEXAHEDRON(25, 3);

    public final int ID;
    public final int dim;

    VTKType(int id, int dim) {
        this.ID = id;
        this.dim = dim;
    }

    public static VTKType get(int id) {
        return Arrays.stream(VTKType.values())
                .filter(e -> e.ID == id)
                .findAny().orElseThrow(() -> new NoSuchElementException("No VTKType for id: " + id));
    }

    /**
     * Get number of points for the VTKType if defined.
     * @return Number of points or Zero (0) for VTK_POLY_VERTEX, VTK_POLY_LINE, VTK_POLYGON, VTK_TRIANGLE_STRIP.
     */
    public int numPoints() {
        return switch (this) {
            case VTK_POLY_VERTEX, VTK_POLY_LINE, VTK_POLYGON, VTK_TRIANGLE_STRIP -> 0;
            case VTK_VERTEX -> 1;
            case VTK_LINE -> 2;
            case VTK_TRIANGLE, VTK_QUADRATIC_EDGE -> 3;
            case VTK_PIXEL, VTK_QUAD, VTK_TETRA -> 4;
            case VTK_PYRAMID -> 5;
            case VTK_WEDGE, VTK_QUADRATIC_TRIANGLE -> 6;
            case VTK_VOXEL, VTK_HEXAHEDRON, VTK_QUADRATIC_QUAD -> 8;
            case VTK_QUADRATIC_TETRA -> 10;
            case VTK_QUADRATIC_HEXAHEDRON -> 20;
        };
    }
}
