package com.anpr.parking.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PlateUtilTest {
    @Test
    void normalizesSpacesDashesAndCase() {
        assertEquals("T123ABC", PlateUtil.normalize(" t 123-abc "));
    }

    @Test
    void validatesLengthAndCharacters() {
        assertTrue(PlateUtil.isValid("T123ABC"));
        assertFalse(PlateUtil.isValid("T1"));
        assertFalse(PlateUtil.isValid(""));
    }
}
