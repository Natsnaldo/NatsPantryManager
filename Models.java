package com.nats.pantrymanager;

import java.util.ArrayList;
import java.util.List;

/** Plain data holders shared by the database layer and the screens. */
final class Models {
    static class Item { long id; String name, unit, expiry; double quantity; }
    static class Ingredient { String name, unit; double quantity; }
    static class Recipe {
        long id; String name, instructions;
        List<Ingredient> ingredients = new ArrayList<>();
    }
}
