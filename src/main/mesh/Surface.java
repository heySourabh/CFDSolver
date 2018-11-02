package main.mesh;

import main.geom.Point;
import main.geom.Vector;

public class Surface {
    public final double area;
    public final Point centroid;
    private Vector unitNormal;
    private Vector unitTangent1;
    private Vector unitTangent2;

    public Surface(double area, Point centroid, Vector unitNormal) {
        this.area = area;
        this.centroid = centroid;
        setUnitNormal(unitNormal);
    }

    public void setUnitNormal(Vector unitNormal) {
        this.unitNormal = unitNormal;
        setUnitTangents();
    }

    public Vector unitNormal() {
        return unitNormal;
    }

    public Vector unitTangent1() {
        return unitTangent1;
    }

    public Vector unitTangent2() {
        return unitTangent2;
    }

    private void setUnitTangents() {
        Vector aVector;
        if (Math.abs(unitNormal.x) < 0.6) {
            aVector = new Vector(1, 0, 0);
        } else if (Math.abs(unitNormal.y) < 0.6) {
            aVector = new Vector(0, 1, 0);
        } else {
            aVector = new Vector(0, 0, 1);
        }

        Vector projection = unitNormal.mult(unitNormal.dot(aVector));
        this.unitTangent1 = aVector.sub(projection).unit();

        this.unitTangent2 = unitNormal.cross(unitTangent1).unit();
    }

    @Override
    public String toString() {
        return "Surface{" +
                "area=" + area +
                ", centroid=" + centroid +
                ", unitNormal=" + unitNormal +
                '}';
    }
}
