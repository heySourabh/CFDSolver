package main.mesh;

import main.geom.Geometry;
import main.geom.Point;

import java.util.ArrayList;

public interface MeshData {
    Dimension dims();

    ArrayList<Point> points();

    ArrayList<Geometry> cellGeom();

    String[] boundaryNames();

    ArrayList<ArrayList<Geometry>> boundaryFaces();
}
