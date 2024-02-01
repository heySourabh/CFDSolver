package main.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class Su2ToCfduConverterTest {

    @Test
    public void convert2DMeshFile() throws IOException {
        File inputFile = new File("test/test_data/su2_to_cfdu/square.su2");
        File outputFile = new File("test/test_data/su2_to_cfdu/square.cfdu");
        Su2ToCfduConverter.convert(inputFile, outputFile);

        String expectedOutput = """
                dimension = 2
                mode = ASCII
                points = 9
                0.0000000 0.0000000 0.0000000
                0.50000000 0.0000000 0.0000000
                1.0000000 0.0000000 0.0000000
                0.0000000 0.50000000 0.0000000
                0.50000000 0.50000000 0.0000000
                1.0000000 0.50000000 0.0000000
                0.0000000 1.0000000 0.0000000
                0.50000000 1.0000000 0.0000000
                1.0000000 1.0000000 0.0000000
                elements = 8
                5 0 1 3
                5 1 4 3
                5 1 2 4
                5 2 5 4
                5 3 4 6
                5 4 7 6
                5 4 5 7
                5 5 8 7
                boundaries = 4
                bname = lower
                bfaces = 2
                3 0 1
                3 1 2
                bname = right
                bfaces = 2
                3 2 5
                3 5 8
                bname = upper
                bfaces = 2
                3 8 7
                3 7 6
                bname = left
                bfaces = 2
                3 6 3
                3 3 0
                """;
        assertEquals(Files.readString(outputFile.toPath()), expectedOutput);
    }

    @Test
    public void convert3DMeshFile() throws IOException {
        File inputFile = new File("test/test_data/su2_to_cfdu/cube.su2");
        File outputFile = new File("test/test_data/su2_to_cfdu/cube.cfdu");
        Su2ToCfduConverter.convert(inputFile, outputFile);

        String expectedOutput = """
                dimension = 3
                mode = ASCII
                points = 14
                -1.0000000 -1.0000000 -1.0000000
                1.0000000 -1.0000000 -1.0000000
                1.0000000 1.0000000 -1.0000000
                -1.0000000 1.0000000 -1.0000000
                -1.0000000 1.0000000 1.0000000
                -1.0000000 -1.0000000 1.0000000
                1.0000000 -1.0000000 1.0000000
                1.0000000 1.0000000 1.0000000
                0.0000000 0.0000000 -1.0000000
                -1.0000000 0.0000000 0.0000000
                0.0000000 -1.0000000 0.0000000
                1.0000000 0.0000000 0.0000000
                0.0000000 1.0000000 0.0000000
                0.0000000 0.0000000 1.0000000
                elements = 24
                10 12 13 10 11
                10 9 13 10 12
                10 10 8 9 12
                10 12 10 8 11
                10 9 8 0 3
                10 5 0 9 10
                10 0 8 10 1
                10 9 3 4 12
                10 9 4 5 13
                10 11 6 10 1
                10 10 13 5 6
                10 7 6 13 11
                10 7 13 4 12
                10 2 3 8 12
                10 12 2 7 11
                10 11 8 2 1
                10 6 10 13 11
                10 0 8 9 10
                10 13 5 9 10
                10 13 9 4 12
                10 12 7 13 11
                10 3 9 8 12
                10 8 2 12 11
                10 11 10 8 1
                boundaries = 6
                bname = left
                bfaces = 4
                5 0 9 3
                5 5 9 0
                5 3 9 4
                5 4 9 5
                bname = right
                bfaces = 4
                5 2 11 1
                5 1 11 6
                5 7 11 2
                5 6 11 7
                bname = upper
                bfaces = 4
                5 3 12 2
                5 2 12 7
                5 4 12 3
                5 7 12 4
                bname = lower
                bfaces = 4
                5 1 10 0
                5 0 10 5
                5 6 10 1
                5 5 10 6
                bname = front
                bfaces = 4
                5 4 5 13
                5 7 4 13
                5 5 6 13
                5 6 7 13
                bname = back
                bfaces = 4
                5 0 1 8
                5 3 0 8
                5 1 2 8
                5 2 3 8
                """;
        assertEquals(Files.readString(outputFile.toPath()), expectedOutput);
    }
}