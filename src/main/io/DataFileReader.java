package main.io;

import main.geom.Point;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataFileReader implements Closeable {

    private final Scanner fileScanner;
    private final String commentStr;

    public DataFileReader(File file) throws FileNotFoundException {
        this(file, null);
    }

    public DataFileReader(File file, String commentStr) throws FileNotFoundException {
        this.fileScanner = new Scanner(file);
        this.commentStr = commentStr;
    }

    public void close() {
        fileScanner.close();
    }

    /**
     * Read the next line from the file ignoring blank lines and text after comment string.
     *
     * @return Next valid line.
     */
    private String nextLine() {
        String nextLine = fileScanner.nextLine().trim();

        if (commentStr != null)
            nextLine = nextLine.split(commentStr)[0].trim();

        // Since blank lines are ignored
        if (nextLine.length() == 0)
            nextLine = nextLine();

        return nextLine;
    }

    public String readParameter(String param) {
        String[] tokens = nextLine().split("=");
        if (!tokens[0].trim().equals(param))
            throw new IllegalArgumentException("The expected parameter \"" + param + "\" does not exist at the location.");

        return tokens[1].trim();
    }

    public int readIntParameter(String param) {
        return Integer.parseInt(readParameter(param));
    }

    public double readDoubleParameter(String param) {
        return Double.parseDouble(readParameter(param));
    }

    public Point readXYZ() {
        String[] tokens = nextLine().split("\\s+");
        double x = Double.parseDouble(tokens[0].trim());
        double y = Double.parseDouble(tokens[1].trim());
        double z = Double.parseDouble(tokens[2].trim());

        return new Point(x, y, z);
    }

    public int[] readIntArray() {
        String[] tokens = nextLine().split("\\s+");
        int[] intArray = new int[tokens.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = Integer.parseInt(tokens[i]);
        }

        return intArray;
    }
}
