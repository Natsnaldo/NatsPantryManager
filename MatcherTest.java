package com.nats.pantrymanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatcherTest {
    private static Models.Item item(String name, double qty, String unit, String expiry) {
        Models.Item i = new Models.Item();
        i.name = name; i.quantity = qty; i.unit = unit; i.expiry = expiry;
        return i;
    }

    private static Models.Recipe recipe(Object... triples) { // name, qty, unit, name, qty, unit...
        Models.Recipe r = new Models.Recipe();
        r.ingredients = new ArrayList<>();
        for (int k = 0; k < triples.length; k += 3) {
            Models.Ingredient g = new Models.Ingredient();
            g.name = (String) triples[k];
            g.quantity = ((Number) triples[k + 1]).doubleValue();
            g.unit = (String) triples[k + 2];
            r.ingredients.add(g);
        }
        return r;
    }

    private static List<Models.Item> pantry(Models.Item... items) { return Arrays.asList(items); }

    @Test public void exactQuantity_matches() {
        assertTrue(Matcher.canMake(recipe("egg", 3, "pcs"), pantry(item("egg", 3, "pcs", ""))));
    }

    @Test public void insufficientQuantity_doesNotMatch() {
        assertFalse(Matcher.canMake(recipe("egg", 3, "pcs"), pantry(item("egg", 2, "pcs", ""))));
    }

    @Test public void missingIngredient_doesNotMatch() {
        assertFalse(Matcher.canMake(recipe("egg", 2, "pcs", "milk", 100, "ml"), pantry(item("egg", 6, "pcs", ""))));
    }

    @Test public void kilogramsSatisfyGrams() {
        assertTrue(Matcher.canMake(recipe("flour", 200, "g"), pantry(item("flour", 0.5, "kg", ""))));
        assertFalse(Matcher.canMake(recipe("flour", 200, "g"), pantry(item("flour", 0.1, "kg", ""))));
    }

    @Test public void millilitresSatisfyTablespoons() {
        assertTrue(Matcher.canMake(recipe("olive oil", 2, "tbsp"), pantry(item("olive oil", 100, "ml", ""))));
    }

    @Test public void pluralPantryNamesMatchSingularRecipeNames() {
        assertTrue(Matcher.canMake(recipe("egg", 3, "pcs"), pantry(item("Eggs", 3, "pcs", ""))));
        assertTrue(Matcher.canMake(recipe("tomato", 4, "pcs"), pantry(item("Tomatoes", 4, "pcs", ""))));
    }

    @Test public void differentDimensionsNeverMatch() {
        assertFalse(Matcher.canMake(recipe("milk", 100, "ml"), pantry(item("milk", 1, "pcs", ""))));
    }

    @Test public void duplicatePantryRowsAreSummed() {
        assertTrue(Matcher.canMake(recipe("flour", 200, "g"),
                pantry(item("flour", 100, "g", ""), item("Flour", 100, "g", ""))));
    }

    @Test public void expiredStockIsIgnored() {
        assertFalse(Matcher.canMake(recipe("egg", 3, "pcs"), pantry(item("egg", 3, "pcs", "2000-01-01"))));
        assertTrue(Matcher.canMake(recipe("egg", 3, "pcs"), pantry(item("egg", 3, "pcs", "2999-01-01"))));
    }
}
