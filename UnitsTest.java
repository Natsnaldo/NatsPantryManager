package com.nats.pantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UnitsTest {
    @Test public void singular_handlesCommonPlurals() {
        assertEquals("egg", Units.singular("Eggs"));
        assertEquals("tomato", Units.singular("Tomatoes"));
        assertEquals("berry", Units.singular("berries"));
        assertEquals("sandwich", Units.singular("sandwiches"));
        assertEquals("oat", Units.singular(" Oats "));
    }

    @Test public void singular_leavesSingularNamesAlone() {
        assertEquals("cheese", Units.singular("cheese"));
        assertEquals("glass", Units.singular("glass"));
        assertEquals("olive oil", Units.singular("Olive Oil"));
    }

    @Test public void toBase_convertsToGramsMillilitresAndPieces() {
        assertEquals(2000.0, Units.toBase(2, "kg"), 0.001);
        assertEquals(1500.0, Units.toBase(1.5, "l"), 0.001);
        assertEquals(30.0, Units.toBase(2, "tbsp"), 0.001);
        assertEquals(5.0, Units.toBase(1, "tsp"), 0.001);
        assertEquals(250.0, Units.toBase(1, "cup"), 0.001);
        assertEquals(3.0, Units.toBase(3, "pcs"), 0.001);
    }

    @Test public void dimension_groupsUnits() {
        assertEquals("mass", Units.dimension("kg"));
        assertEquals("volume", Units.dimension("tbsp"));
        assertEquals("count", Units.dimension("pcs"));
    }

    @Test public void fmt_dropsTrailingZero() {
        assertEquals("3", Units.fmt(3.0));
        assertEquals("2.5", Units.fmt(2.5));
    }

    @Test public void daysLeft_handlesMissingAndPastDates() {
        assertEquals(Long.MAX_VALUE, Units.daysLeft(""));
        assertEquals(Long.MAX_VALUE, Units.daysLeft(null));
        assertTrue(Units.daysLeft("2000-01-01") < 0);
        assertTrue(Units.daysLeft("2999-01-01") > 0);
    }

    @Test public void validDate_requiresIsoFormat() {
        assertTrue(Units.validDate("2026-10-02"));
        assertFalse(Units.validDate("02/10/2026"));
        assertFalse(Units.validDate("tomorrow"));
    }
}
