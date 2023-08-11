package main.io;

import main.geom.Point;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static main.util.TestHelper.assertThrows;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DataFileReaderTest {

    @Test
    public void readWithComments() throws IOException {
        File tempFile = new File("test/test_data/dataFileTest.cfd");

        try (FileWriter writer = new FileWriter(tempFile)) {
            String textWithComments = """
                    % Comment line
                    int_param1=56
                    IntParam2 =    85
                    DoubleParam1 = 2452 % This a double param
                    str1 = Just a plain line
                    987 34 76
                    45 72 25 98 41 23 14 57 92 12    % int array
                    line=empty""";
            writer.write(textWithComments);
        }
        try (DataFileReader fileReader = new DataFileReader(tempFile, "%")) {

            assertEquals(56, fileReader.readIntParameter("int_param1"));
            assertEquals(85, fileReader.readIntParameter("IntParam2"));
            assertEquals(2452, fileReader.readDoubleParameter("DoubleParam1"), 1e-15);
            assertEquals("Just a plain line", fileReader.readParameter("str1"));
            assertEquals(0, new Point(987, 34, 76).distance(fileReader.readXYZ()), 1e-15);
            assertArrayEquals(new int[]{45, 72, 25, 98, 41, 23, 14, 57, 92, 12}, fileReader.readIntArray());

            assertThrows(IllegalArgumentException.class, () -> fileReader.readParameter("Dummy")); // line=empty
        }

        if (!tempFile.delete()) System.out.println("Unable to delete temporary file: " + tempFile);
    }

    @Test
    public void readWithoutComments() throws IOException {
        File tempFile = new File("test/test_data/dataFileTest.cfd");

        try (FileWriter writer = new FileWriter(tempFile)) {
            String textWithoutComments = """
                    int_param1=56
                    IntParam2 =    85
                    DoubleParam1 = 2452\s
                    str1 = Just a plain line
                    987 34 76
                    45 72 25 98 41 23 14 57 92 12
                    """;
            writer.write(textWithoutComments);
        }
        try (DataFileReader fileReader = new DataFileReader(tempFile)) {

            assertEquals(56, fileReader.readIntParameter("int_param1"));
            assertEquals(85, fileReader.readIntParameter("IntParam2"));
            assertEquals(2452, fileReader.readDoubleParameter("DoubleParam1"), 1e-15);
            assertEquals("Just a plain line", fileReader.readParameter("str1"));
            assertEquals(0, new Point(987, 34, 76).distance(fileReader.readXYZ()), 1e-15);
            assertArrayEquals(new int[]{45, 72, 25, 98, 41, 23, 14, 57, 92, 12}, fileReader.readIntArray());
        }

        if (!tempFile.delete()) System.out.println("Unable to delete temporary file: " + tempFile);
    }

    @Test
    public void file_with_only_comment_on_a_line() throws IOException {
        File tempFile = new File("test/test_data/dataFileTest.cfd");

        try (FileWriter writer = new FileWriter(tempFile)) {
            String textWithoutComments = """
                    %
                    int_param1=56
                    IntParam2 =    85
                    DoubleParam1= 2452\s
                    str1 = Just a plain line
                    987 34 76
                    45 72 25 98 41 23 14 57 92 12
                    """;
            writer.write(textWithoutComments);
        }
        try (DataFileReader fileReader = new DataFileReader(tempFile, "%")) {

            assertEquals(56, fileReader.readIntParameter("int_param1"));
            assertEquals(85, fileReader.readIntParameter("IntParam2"));
            assertEquals(2452, fileReader.readDoubleParameter("DoubleParam1"), 1e-15);
            assertEquals("Just a plain line", fileReader.readParameter("str1"));
            assertEquals(0, new Point(987, 34, 76).distance(fileReader.readXYZ()), 1e-15);
            assertArrayEquals(new int[]{45, 72, 25, 98, 41, 23, 14, 57, 92, 12}, fileReader.readIntArray());
        }

        if (!tempFile.delete()) System.out.println("Unable to delete temporary file: " + tempFile);
    }
}