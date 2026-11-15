package com.example.gymnotes;

import org.junit.Test;
import static org.junit.Assert.*;

public class SetInputTest {
    @Test public void acceptsDecimalCommaAndBodyweight() {
        assertEquals(12.5, SetInput.weight("12,5"), 0);
        assertEquals(0, SetInput.weight("0"), 0);
        assertEquals(Integer.valueOf(12), SetInput.reps("12"));
    }
    @Test public void rejectsIncompleteAndNonFiniteInput() {
        for (String value : new String[]{"", ".", "NaN", "Infinity", "-1", "10001", "1,2,3"})
            assertNull(value, SetInput.weight(value));
    }
    @Test public void rejectsInvalidRepetitions() {
        for (String value : new String[]{"", "0", "-1", "1.5", "10001", "999999999999"})
            assertNull(value, SetInput.reps(value));
    }
}
