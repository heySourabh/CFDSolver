package main.util;

import main.geom.Point;
import main.geom.VTKType;
import main.io.DataFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static java.util.Arrays.copyOfRange;

public class Su2ToCfduConverter {

    private final File s2uFile;
    private final File cfduFile;

    public Su2ToCfduConverter(File s2uFile, File cfduFile) {
        this.s2uFile = s2uFile;
        this.cfduFile = cfduFile;
    }

    public void convert() throws FileNotFoundException {
        try (DataFileReader su2Reader = new DataFileReader(this.s2uFile, "%");
             PrintWriter cfduWriter = new PrintWriter(cfduFile)) {
            int numDimensions = su2Reader.readIntParameter("NDIME");

            int numElements = su2Reader.readIntParameter("NELEM");
            Su2Element[] elements = new Su2Element[numElements];
            for (int ei = 0; ei < numElements; ei++) {
                int[] conn = su2Reader.readIntArray();
                VTKType vtkType = VTKType.get(conn[0]);
                elements[ei] = new Su2Element(vtkType, copyOfRange(conn, 1, vtkType.numPoints() + 1));
            }

            int numPoints = su2Reader.readIntParameter("NPOIN");
            Point[] points = new Point[numPoints];
            for (int i = 0; i < numPoints; i++) {
                points[i] = createPoint(su2Reader.readDoubleArray(), numDimensions);
            }

            int numBndMarkers = su2Reader.readIntParameter("NMARK");
            Su2Boundary[] boundaries = new Su2Boundary[numBndMarkers];
            for (int bnd = 0; bnd < numBndMarkers; bnd++) {
                String markerTag = su2Reader.readParameter("MARKER_TAG");
                int numBndElements = su2Reader.readIntParameter("MARKER_ELEMS");
                Su2Element[] bndElements = new Su2Element[numBndElements];
                for (int ei = 0; ei < numBndElements; ei++) {
                    int[] conn = su2Reader.readIntArray();
                    VTKType vtkType = VTKType.get(conn[0]);
                    bndElements[ei] = new Su2Element(vtkType, copyOfRange(conn, 1, vtkType.numPoints() + 1));
                }
                boundaries[bnd] = new Su2Boundary(markerTag, bndElements);
            }

            // ------ Finished reading su2 file, now writing to cfdu file

            cfduWriter.println("dimension = " + numDimensions);
            cfduWriter.println("mode = ASCII");

            cfduWriter.println("points = " + points.length);
            for (Point point : points) {
                cfduWriter.printf("%1.8g %1.8g %1.8g%n", point.x, point.y, point.z);
            }

            cfduWriter.println("elements = " + elements.length);
            for (Su2Element element : elements) {
                cfduWriter.printf("%d", element.vtkType.ID);
                for (int i = 0; i < element.connectivity.length; i++) {
                    cfduWriter.printf(" %d", element.connectivity[i]);
                }
                cfduWriter.println();
            }

            cfduWriter.println("boundaries = " + boundaries.length);
            for (Su2Boundary boundary : boundaries) {
                cfduWriter.println("bname = " + boundary.tag);
                cfduWriter.println("bfaces = " + boundary.bndElements.length);
                for (Su2Element bndElement : boundary.bndElements) {
                    cfduWriter.printf("%d", bndElement.vtkType.ID);
                    for (int i = 0; i < bndElement.connectivity.length; i++) {
                        cfduWriter.printf(" %d", bndElement.connectivity[i]);
                    }
                    cfduWriter.println();
                }
            }
        }
    }

    private Point createPoint(double[] pointData, int dimension) {
        return switch (dimension) {
            case 2 -> new Point(pointData[0], pointData[1], 0.0);
            case 3 -> new Point(pointData[0], pointData[1], pointData[2]);
            default -> throw new IllegalArgumentException("Unable to create point of dimension " + dimension);
        };
    }

    private record Su2Element(VTKType vtkType, int[] connectivity) {
    }

    private record Su2Boundary(String tag, Su2Element[] bndElements) {
    }
}
