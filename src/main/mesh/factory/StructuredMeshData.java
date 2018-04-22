package main.mesh.factory;

import main.geom.Geometry;
import main.geom.Point;
import main.geom.factory.Hexahedron;
import main.geom.factory.Line;
import main.geom.factory.Quad;
import main.geom.factory.Vertex;
import main.mesh.Dimension;
import main.mesh.MeshData;
import main.util.DataFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class StructuredMeshData implements MeshData {

    private final Dimension dims;
    private final ArrayList<Point> points;
    private final ArrayList<Geometry> cellGeom;
    private final String[] boundaryNames;
    private final ArrayList<ArrayList<Geometry>> boundaryFaces;

    public static void main(String[] args) throws FileNotFoundException {
        new StructuredMeshData(new File("input/mesh_structured_3d.dat"));
    }

    private final int xi, eta, zeta;

    public StructuredMeshData(File meshFile) throws FileNotFoundException {
        // *********** Read points from file ***********
        points = new ArrayList<>();

        try (DataFileReader meshFileReader = new DataFileReader(meshFile, "%")) {
            dims = Dimension.getDimension(meshFileReader.readIntParameter("dimension"));

            xi = meshFileReader.readIntParameter("xi");
            switch (dims) {
                case ONE_DIM:
                    eta = 1;
                    zeta = 1;
                    break;
                case TWO_DIM:
                    eta = meshFileReader.readIntParameter("eta");
                    zeta = 1;
                    break;
                case THREE_DIM:
                    eta = meshFileReader.readIntParameter("eta");
                    zeta = meshFileReader.readIntParameter("zeta");
                    break;
                default:
                    throw new IllegalStateException("Invalid dimension.");
            }

            // Ignore the header line, "x y z"
            meshFileReader.nextLine();

            for (int i = 0; i < xi; i++) {
                for (int j = 0; j < eta; j++) {
                    for (int k = 0; k < zeta; k++) {
                        String[] xyz = meshFileReader.nextLine().split("\\s+");
                        double x = Double.parseDouble(xyz[0]);
                        double y = Double.parseDouble(xyz[1]);
                        double z = Double.parseDouble(xyz[2]);
                        points.add(new Point(x, y, z));
                    }
                }
            }
        }


        // *********** Set up cell geometry ***********
        cellGeom = new ArrayList<>();
        switch (dims) {
            case ONE_DIM:
                for (int i = 0; i < xi - 1; i++) {
                    Geometry geom = new Line(points.get(i), points.get(i + 1));
                    cellGeom.add(geom);
                }
                break;
            case TWO_DIM:
                for (int i = 0; i < xi - 1; i++) {
                    for (int j = 0; j < eta - 1; j++) {
                        int ij = index(i, j);
                        int ip1j = index(i + 1, j);
                        int ip1jp1 = index(i + 1, j + 1);
                        int ijp1 = index(i, j + 1);

                        Geometry geom = new Quad(points.get(ij), points.get(ip1j),
                                points.get(ip1jp1), points.get(ijp1));
                        cellGeom.add(geom);
                    }
                }
                break;
            case THREE_DIM:
                for (int i = 0; i < xi - 1; i++) {
                    for (int j = 0; j < eta - 1; j++) {
                        for (int k = 0; k < zeta - 1; k++) {
                            int ijk = index(i, j, k);
                            int ip1jk = index(i + 1, j, k);
                            int ip1jp1k = index(i + 1, j + 1, k);
                            int ijp1k = index(i, j + 1, k);

                            int ijkp1 = index(i, j, k + 1);
                            int ip1jkp1 = index(i + 1, j, k + 1);
                            int ip1jp1kp1 = index(i + 1, j + 1, k + 1);
                            int ijp1kp1 = index(i, j + 1, k + 1);

                            Geometry geom = new Hexahedron(
                                    points.get(ijk), points.get(ip1jk), points.get(ip1jp1k), points.get(ijp1k),
                                    points.get(ijkp1), points.get(ip1jkp1), points.get(ip1jp1kp1), points.get(ijp1kp1));
                            cellGeom.add(geom);
                        }
                    }
                }
                break;
        }


        // *********** Setup boundary names ***********
        switch (dims) {
            case ONE_DIM:
                boundaryNames = new String[]{"xi min", "xi max"};
                break;
            case TWO_DIM:
                boundaryNames = new String[]{"xi min", "xi max", "eta min", "eta max"};
                break;
            case THREE_DIM:
                boundaryNames = new String[]{"xi min", "xi max", "eta min", "eta max", "eta min", "eta max"};
                break;
            default:
                throw new IllegalStateException("No such dimension.");
        }


        // *********** Setup boundary faces ***********
        boundaryFaces = new ArrayList<>();
        switch (dims) {
            case ONE_DIM: {
                ArrayList<Geometry> xiMinFaces = new ArrayList<>();
                xiMinFaces.add(new Vertex(points.get(0)));
                boundaryFaces.add(xiMinFaces);

                ArrayList<Geometry> xiMaxFaces = new ArrayList<>();
                xiMaxFaces.add(new Vertex(points.get(points.size() - 1)));
                boundaryFaces.add(xiMaxFaces);
            }
            break;
            case TWO_DIM: {
                int i, j;
                ArrayList<Geometry> xiMinFaces = new ArrayList<>();
                i = 0;
                for (j = 0; j < eta - 1; j++) {
                    int ij = index(i, j);
                    int ijp1 = index(i, j + 1);
                    xiMinFaces.add(new Line(points.get(ij), points.get(ijp1)));
                }
                boundaryFaces.add(xiMinFaces);

                ArrayList<Geometry> xiMaxFaces = new ArrayList<>();
                i = xi - 1;
                for (j = 0; j < eta - 1; j++) {
                    int ij = index(i, j);
                    int ijp1 = index(i, j + 1);
                    xiMaxFaces.add(new Line(points.get(ij), points.get(ijp1)));
                }
                boundaryFaces.add(xiMaxFaces);

                ArrayList<Geometry> etaMinFaces = new ArrayList<>();
                j = 0;
                for (i = 0; i < xi - 1; i++) {
                    int ij = index(i, j);
                    int ip1j = index(i + 1, j);
                    etaMinFaces.add(new Line(points.get(ij), points.get(ip1j)));
                }
                boundaryFaces.add(etaMinFaces);

                ArrayList<Geometry> etaMaxFaces = new ArrayList<>();
                j = eta - 1;
                for (i = 0; i < xi - 1; i++) {
                    int ij = index(i, j);
                    int ip1j = index(i + 1, j);
                    etaMaxFaces.add(new Line(points.get(ij), points.get(ip1j)));
                }
                boundaryFaces.add(etaMaxFaces);
            }
            break;
            case THREE_DIM: {
                int i, j, k;
                ArrayList<Geometry> xiMinFaces = new ArrayList<>();
                i = 0;
                for (j = 0; j < eta - 1; j++) {
                    for (k = 0; k < zeta - 1; k++) {
                        int ijk = index(i, j, k);
                        int ijp1k = index(i, j + 1, k);
                        int ijp1kp1 = index(i, j + 1, k + 1);
                        int ijkp1 = index(i, j, k + 1);
                        xiMinFaces.add(new Quad(points.get(ijk), points.get(ijp1k),
                                points.get(ijp1kp1), points.get(ijkp1)));
                    }
                }
                boundaryFaces.add(xiMinFaces);

                ArrayList<Geometry> xiMaxFaces = new ArrayList<>();
                i = xi - 1;
                for (j = 0; j < eta - 1; j++) {
                    for (k = 0; k < zeta - 1; k++) {
                        int ijk = index(i, j, k);
                        int ijp1k = index(i, j + 1, k);
                        int ijp1kp1 = index(i, j + 1, k + 1);
                        int ijkp1 = index(i, j, k + 1);
                        xiMaxFaces.add(new Quad(points.get(ijk), points.get(ijp1k),
                                points.get(ijp1kp1), points.get(ijkp1)));
                    }
                }
                boundaryFaces.add(xiMaxFaces);

                ArrayList<Geometry> etaMinFaces = new ArrayList<>();
                j = 0;
                for (i = 0; i < xi - 1; i++) {
                    for (k = 0; k < zeta - 1; k++) {
                        int ijk = index(i, j, k);
                        int ip1jk = index(i + 1, j, k);
                        int ip1jkp1 = index(i + 1, j, k + 1);
                        int ijkp1 = index(i, j, k + 1);
                        etaMinFaces.add(new Quad(points.get(ijk), points.get(ip1jk),
                                points.get(ip1jkp1), points.get(ijkp1)));
                    }
                }
                boundaryFaces.add(etaMinFaces);

                ArrayList<Geometry> etaMaxFaces = new ArrayList<>();
                j = eta - 1;
                for (i = 0; i < xi - 1; i++) {
                    for (k = 0; k < zeta - 1; k++) {
                        int ijk = index(i, j, k);
                        int ip1jk = index(i + 1, j, k);
                        int ip1jkp1 = index(i + 1, j, k + 1);
                        int ijkp1 = index(i, j, k + 1);
                        etaMaxFaces.add(new Quad(points.get(ijk), points.get(ip1jk),
                                points.get(ip1jkp1), points.get(ijkp1)));
                    }
                }
                boundaryFaces.add(etaMaxFaces);

                ArrayList<Geometry> zetaMinFaces = new ArrayList<>();
                k = 0;
                for (i = 0; i < xi - 1; i++) {
                    for (j = 0; j < eta - 1; j++) {
                        int ijk = index(i, j, k);
                        int ip1jk = index(i + 1, j, k);
                        int ip1jp1k = index(i + 1, j + 1, k);
                        int ijp1k = index(i, j + 1, k);
                        zetaMinFaces.add(new Quad(points.get(ijk), points.get(ip1jk),
                                points.get(ip1jp1k), points.get(ijp1k)));
                    }
                }
                boundaryFaces.add(zetaMinFaces);

                ArrayList<Geometry> zetaMaxFaces = new ArrayList<>();
                k = zeta - 1;
                for (i = 0; i < xi - 1; i++) {
                    for (j = 0; j < eta - 1; j++) {
                        int ijk = index(i, j, k);
                        int ip1jk = index(i + 1, j, k);
                        int ip1jp1k = index(i + 1, j + 1, k);
                        int ijp1k = index(i, j + 1, k);
                        zetaMaxFaces.add(new Quad(points.get(ijk), points.get(ip1jk),
                                points.get(ip1jp1k), points.get(ijp1k)));
                    }
                }
                boundaryFaces.add(zetaMaxFaces);
            }
            break;
        }
    }

    private int index(int i, int j, int k) {
        return i * (eta * zeta) + j * zeta + k;
    }

    private int index(int i, int j) {
        return index(i, j, 0);
    }

    @Override
    public Dimension dims() {
        return dims;
    }

    @Override
    public ArrayList<Point> points() {
        return points;
    }

    @Override
    public ArrayList<Geometry> cellGeom() {
        return cellGeom;
    }

    @Override
    public String[] boundaryNames() {
        return boundaryNames;
    }

    @Override
    public ArrayList<ArrayList<Geometry>> boundaryFaces() {
        return boundaryFaces;
    }
}
