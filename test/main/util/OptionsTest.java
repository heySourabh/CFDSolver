package main.util;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class OptionsTest {
    @Test
    public void test_throws_when_argument_is_not_present() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument",
                "--incomplete",
        };
        Options options = new Options(args);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> options.get("not-arg"));
        assertEquals("Cannot find argument named: --not-arg", exception.getMessage());
        exception = assertThrows(NoSuchElementException.class, () -> options.get("incomplete"));
        assertEquals("Cannot find argument named: --incomplete", exception.getMessage());
    }

    @Test
    public void test_string_option() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);

        assertEquals("String argument", options.get("name"));
        assertEquals("String argument", options.get("--name"));
    }

    @Test
    public void test_default_value() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);

        assertEquals("String argument", options.getOrDefault("--name", "Default argument"));
        assertEquals("Default argument", options.getOrDefault("unknown", "Default argument"));
    }

    @Test
    public void test_int_argument() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);

        assertEquals(3, options.getInt("Mach"));
    }

    @Test
    public void test_int_argument_with_default_value() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);

        assertEquals(3, options.getIntOrDefault("Mach", 4));
        assertEquals(6, options.getIntOrDefault("Count", 6));
    }

    @Test
    public void test_int_parsing_exception() {
        String[] args = new String[]{
                "--Mach", "3.5",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> options.getInt("Mach"));
        assertEquals("Unable to parse value '3.5' as integer for the argument named 'Mach'", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () -> options.getIntOrDefault("Mach", 5));
        assertEquals("Unable to parse value '3.5' as integer for the argument named 'Mach'", exception.getMessage());
    }

    @Test
    public void test_double_argument() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);

        assertEquals(2.5, options.getDouble("AoA"), 1e-15);
        assertEquals(3, options.getDouble("Mach"), 1e-15);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> options.getDouble("name"));
        assertEquals("Unable to parse value 'String argument' as double for the argument named 'name'", exception.getMessage());
    }

    @Test
    public void test_double_argument_with_default_value() {
        String[] args = new String[]{
                "--Mach", "3",
                "--AoA", "2.5",
                "--name", "String argument"
        };
        Options options = new Options(args);
        assertEquals(2.5, options.getDoubleOrDefault("AoA", 2.5), 1e-15);
        assertEquals(3, options.getDoubleOrDefault("undefined", 3), 1e-15);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> options.getDoubleOrDefault("name", 2.5));
        assertEquals("Unable to parse value 'String argument' as double for the argument named 'name'", exception.getMessage());
    }
}