package main.geom;

/**
 * Helper methods for calculation of geometry properties.
 * Look at my repository on GitHub:
 * <a href="https://github.com/heySourabh/GeometryCalculations">
 * https://github.com/heySourabh/GeometryCalculations
 * </a>
 * for further details and derivations.
 *
 * @author Sourabh Bhat (heySourabh@gmail.com)
 */
public class GeometryHelper {

    private static class BoundingBox {
        final double xMin, xMax, yMin, yMax, zMin, zMax;

        private BoundingBox(double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.zMin = zMin;
            this.zMax = zMax;
        }

        private Point midPoint() {
            return new Point(
                    (xMin + xMax) * 0.5,
                    (yMin + yMax) * 0.5,
                    (zMin + zMax) * 0.5);
        }
    }

    private static class Triangle {
        final Point[] points;

        private Triangle(Point p0, Point p1, Point p2) {
            points = new Point[]{p0, p1, p2};
        }

        private Point[] points() {
            return points;
        }

        private static Triangle create(main.geom.factory.Triangle triangle) {
            Point[] p = triangle.points();

            return new Triangle(p[0], p[1], p[2]);
        }

        private static Triangle[] create(main.geom.factory.Triangle[] triangle) {
            Triangle[] newTriangles = new Triangle[triangle.length];
            for (int i = 0; i < triangle.length; i++) {
                newTriangles[i] = create(triangle[i]);
            }

            return newTriangles;
        }
    }

    private static BoundingBox boundingBox(Triangle... triangles) {
        double xMin = triangles[0].points()[0].x;
        double xMax = triangles[0].points()[0].x;
        double yMin = triangles[0].points()[0].y;
        double yMax = triangles[0].points()[0].y;
        double zMin = triangles[0].points()[0].z;
        double zMax = triangles[0].points()[0].z;

        for (Triangle triangle : triangles) {
            for (int j = 0; j < 3; j++) {
                Point p = triangle.points()[j];
                xMin = Math.min(xMin, p.x);
                xMax = Math.max(xMax, p.x);
                yMin = Math.min(yMin, p.y);
                yMax = Math.max(yMax, p.y);
                zMin = Math.min(zMin, p.z);
                zMax = Math.max(zMax, p.z);
            }
        }

        return new BoundingBox(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    private static Triangle translateTriangle(Triangle tri, double dx, double dy, double dz) {
        Point[] p = tri.points();

        return new Triangle(
                new Point(p[0].x + dx, p[0].y + dy, p[0].z + dz),
                new Point(p[1].x + dx, p[1].y + dy, p[1].z + dz),
                new Point(p[2].x + dx, p[2].y + dy, p[2].z + dz));
    }

    private static Triangle[] translateTriangles(Triangle[] triangles, double dx, double dy, double dz) {
        Triangle[] newTriangles = new Triangle[triangles.length];
        for (int i = 0; i < triangles.length; i++) {
            newTriangles[i] = translateTriangle(triangles[i], dx, dy, dz);
        }

        return newTriangles;
    }

    private static double volumeUnder(Triangle tri) {
        Point[] p = tri.points();
        double xbar = (p[0].x + p[1].x + p[2].x) / 3.0;
        Vector vab = new Vector(p[0], p[1]);
        Vector vac = new Vector(p[0], p[2]);

        double ax = vab.y * vac.z - vac.y * vab.z; // x-component of cross product

        return ax * xbar * 0.5;
    }

    private static Vector centroidTetra(Point p0, Point p1, Point p2, Point p3) {
        return new Vector(
                (p0.x + p1.x + p2.x + p3.x) * 0.25,
                (p0.y + p1.y + p2.y + p3.y) * 0.25,
                (p0.z + p1.z + p2.z + p3.z) * 0.25
        );
    }

    private static double volumeTetra(Point p0, Point p1, Point p2, Point p3) {

        Vector v30 = new Vector(p3, p0);
        Vector v31 = new Vector(p3, p1);
        Vector v32 = new Vector(p3, p2);

        return v30.dot(v31.cross(v32)) / 6.0;
    }

    private static Vector centroidTriangle(Triangle tri) {
        Point[] p = tri.points();

        return new Vector(
                (p[0].x + p[1].x + p[2].x) / 3.0,
                (p[0].y + p[1].y + p[2].y) / 3.0,
                (p[0].z + p[1].z + p[2].z) / 3.0
        );
    }

    private static double areaTriangle(Triangle triangle) {
        Point[] p = triangle.points();
        Vector v1 = new Vector(p[0], p[1]);
        Vector v2 = new Vector(p[0], p[2]);

        return v1.cross(v2).mag() * 0.5;
    }

    private static Vector scaledCentroidOfVolumeUnder(Triangle tri) {
        Point[] p = tri.points();
        // Break the wedge into three tetra
        Point p0 = p[0];
        Point p1 = p[1];
        Point p2 = p[2];
        // x-projection of points
        Point p3 = new Point(0, p0.y, p0.z);
        Point p4 = new Point(0, p1.y, p1.z);
        Point p5 = new Point(0, p2.y, p2.z);

        // Create tetrahedrons using points such that the external face is ordered clockwise
        Vector v0 = centroidTetra(p0, p1, p2, p4).mult(volumeTetra(p0, p1, p2, p4));
        Vector v1 = centroidTetra(p3, p5, p4, p0).mult(volumeTetra(p3, p5, p4, p0));
        Vector v2 = centroidTetra(p2, p4, p5, p0).mult(volumeTetra(p2, p4, p5, p0));

        return v0.add(v1).add(v2);
    }

    private static double signedVolume(Triangle[] triangles) {
        double volume = 0.0;
        for (Triangle t : triangles) {
            volume += volumeUnder(t);
        }
        return volume;
    }

    /**
     * Calculates the volume of a solid formed by a set of triangles.
     * The triangles must have points either all-clockwise or all-anti-clockwise, looking from outside of the solid.
     *
     * @param triangleArray array of triangles
     * @return non-negative volume enclosed by the set of triangles
     */
    public static double volume(main.geom.factory.Triangle[] triangleArray) {
        Triangle[] triangles = Triangle.create(triangleArray);
        Point translateBy = boundingBox(triangles).midPoint();
        Triangle[] newTriangles = translateTriangles(triangles, -translateBy.x, -translateBy.y, -translateBy.z);

        return Math.abs(signedVolume(newTriangles));
    }

    /**
     * Calculates the centroid point of a solid formed by a set of triangles.
     * The triangles must have points either all-clockwise or all-anti-clockwise, looking from outside of the solid.
     *
     * @param triangleArray array of triangles
     * @return centroid point of solid region enclosed by the set of triangles
     */
    public static Point centroid(main.geom.factory.Triangle[] triangleArray) {
        Triangle[] triangles = Triangle.create(triangleArray);
        Point translateBy = boundingBox(triangles).midPoint();
        Triangle[] newTriangles = translateTriangles(triangles, -translateBy.x, -translateBy.y, -translateBy.z);

        double vol = signedVolume(newTriangles);
        Vector centroidPos = new Vector(0, 0, 0);

        for (Triangle t : newTriangles) {
            centroidPos = centroidPos.add(scaledCentroidOfVolumeUnder(t));
        }
        centroidPos = centroidPos.mult(1.0 / vol);
        centroidPos = centroidPos.add(new Vector(translateBy.x, translateBy.y, translateBy.z));

        return new Point(centroidPos.x, centroidPos.y, centroidPos.z);
    }

    /**
     * Calculates the centroid point of a surface formed by a set of triangles.
     *
     * @param triangleArray array of triangles
     * @return centroid point of surface formed by the set of triangles
     */
    public static Point surfaceCentroid(main.geom.factory.Triangle[] triangleArray) {
        Triangle[] triangles = Triangle.create(triangleArray);
        Vector centroid = new Vector(0, 0, 0);
        double totalArea = 0.0;
        for (Triangle t : triangles) {
            double area = areaTriangle(t);
            centroid = centroidTriangle(t).mult(area).add(centroid);
            totalArea += area;
        }

        centroid = centroid.mult(1.0 / totalArea);

        return new Point(centroid.x, centroid.y, centroid.z);
    }
}
