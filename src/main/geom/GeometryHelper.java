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

    public static class TriGeom {
        final Point[] points;

        public TriGeom(Point p0, Point p1, Point p2) {
            points = new Point[]{p0, p1, p2};
        }

        private Point[] points() {
            return points;
        }
    }

    private static BoundingBox boundingBox(TriGeom... triangles) {
        double xMin = triangles[0].points()[0].x;
        double xMax = triangles[0].points()[0].x;
        double yMin = triangles[0].points()[0].y;
        double yMax = triangles[0].points()[0].y;
        double zMin = triangles[0].points()[0].z;
        double zMax = triangles[0].points()[0].z;

        for (TriGeom triangle : triangles) {
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

    private static TriGeom translateTriangle(TriGeom tri, double dx, double dy, double dz) {
        Point[] p = tri.points();

        return new TriGeom(
                new Point(p[0].x + dx, p[0].y + dy, p[0].z + dz),
                new Point(p[1].x + dx, p[1].y + dy, p[1].z + dz),
                new Point(p[2].x + dx, p[2].y + dy, p[2].z + dz));
    }

    private static TriGeom[] translateTriangles(TriGeom[] triangles, double dx, double dy, double dz) {
        TriGeom[] newTriangles = new TriGeom[triangles.length];
        for (int i = 0; i < triangles.length; i++) {
            newTriangles[i] = translateTriangle(triangles[i], dx, dy, dz);
        }

        return newTriangles;
    }

    private static double volumeUnder(TriGeom tri) {
        Point[] p = tri.points();
        double xbar = (p[0].x + p[1].x + p[2].x) / 3.0;
        Vector vab = new Vector(p[0], p[1]);
        Vector vac = new Vector(p[0], p[2]);

        double ax = vab.y * vac.z - vac.y * vab.z; // x-component of cross product

        return ax * xbar * 0.5;
    }

    private static Vector scaledCentroidUnder(TriGeom tri) {
        Point[] p = tri.points();
        Point p0 = p[0];
        Point p1 = p[1];
        Point p2 = p[2];

        Vector v1 = new Vector(p0, p1);
        Vector v2 = new Vector(p0, p2);

        Vector areaVector = v1.cross(v2);
        double aT = areaVector.mag();
        if (aT < 1e-15) {
            return new Vector(0, 0, 0);
        }

        Vector unitNormal = areaVector.unit();

        double xc = aT * unitNormal.x
                * (p0.x * p0.x + p1.x * p1.x + p2.x * p2.x + p0.x * p1.x + p0.x * p2.x + p1.x * p2.x);
        double yc = aT * unitNormal.y
                * (p0.y * p0.y + p1.y * p1.y + p2.y * p2.y + p0.y * p1.y + p0.y * p2.y + p1.y * p2.y);
        double zc = aT * unitNormal.z
                * (p0.z * p0.z + p1.z * p1.z + p2.z * p2.z + p0.z * p1.z + p0.z * p2.z + p1.z * p2.z);

        return new Vector(xc, yc, zc);
    }

    private static double signedVolume(TriGeom[] triangles) {
        double volume = 0.0;
        for (TriGeom t : triangles) {
            volume += volumeUnder(t);
        }
        return volume;
    }

    /**
     * Calculates the volume of a solid formed by a set of triangles.
     * The triangles must have points either all-clockwise or all-anti-clockwise, looking from outside of the solid.
     *
     * @param triangles array of triangles
     * @return non-negative volume enclosed by the set of triangles
     */
    public static double volume(TriGeom[] triangles) {
        Point translateBy = boundingBox(triangles).midPoint();
        TriGeom[] newTriangles = translateTriangles(triangles, -translateBy.x, -translateBy.y, -translateBy.z);

        return Math.abs(signedVolume(newTriangles));
    }

    /**
     * Calculates the centroid point of a solid formed by a set of triangles.
     * The triangles must have points either all-clockwise or all-anti-clockwise, looking from outside of the solid.
     *
     * @param triangles array of triangles
     * @return centroid point of solid region enclosed by the set of triangles
     */
    public static Point centroid(TriGeom[] triangles) {
        Point translateBy = boundingBox(triangles).midPoint();
        TriGeom[] newTriangles = translateTriangles(triangles, -translateBy.x, -translateBy.y, -translateBy.z);

        double vol = signedVolume(newTriangles);
        Vector centroidPos = new Vector(0, 0, 0);

        for (TriGeom t : newTriangles) {
            centroidPos = centroidPos.add(scaledCentroidUnder(t));
        }
        centroidPos = centroidPos.mult(1.0 / vol / 24.0);
        centroidPos = centroidPos.add(new Vector(translateBy.x, translateBy.y, translateBy.z));

        return new Point(centroidPos.x, centroidPos.y, centroidPos.z);
    }
}
