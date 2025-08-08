package main.mesh;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BoundaryTest {

    @Test
    public void constructor_nullability_test() {
        // null name
        assertThrows(NullPointerException.class, () -> new Boundary(null, List.of(), null));
        // null faces
        assertThrows(NullPointerException.class, () -> new Boundary("name", null, null));
    }

    @Test
    public void bc_optionality_test() {
        Boundary bnd = new Boundary("bnd name", List.of(), null);
        assertEquals(Optional.empty(), bnd.bc());
    }

    @Test
    public void trying_to_set_null_bc() {
        Boundary bnd = new Boundary("bnd name", List.of(), null);
        assertThrows(NullPointerException.class, () -> bnd.setBC(null));
    }
}