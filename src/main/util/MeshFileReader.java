package main.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MeshFileReader implements Closeable {

    private final Scanner fileScanner;
    private final String commentStr;

    public MeshFileReader(File file) throws FileNotFoundException {
        this(file, null);
    }

    public MeshFileReader(File file, String commentStr) throws FileNotFoundException {
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
    public String nextLine() {
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
            throw new IllegalArgumentException("The expected parameter " + param + " does not exist at the location.");
        return tokens[1].trim();
    }

    public int readIntParameter(String param) {
        return Integer.parseInt(readParameter(param));
    }

    public double readDoubleParameter(String param) {
        return Double.parseDouble(readParameter(param));
    }
}
