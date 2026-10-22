package com.nats.pantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Matcher {
    static boolean canMake(Models.Recipe r, List<Models.Item> pantry) {
        return missing(r, pantry).isEmpty();
    }

    static List<Models.Ingredient> missing(Models.Recipe r, List<Models.Item> pantry) {
        Map<String, Double> have = new HashMap<>();
        for (Models.Item i : pantry) {
            if (Units.daysLeft(i.expiry) < 0) continue;
            have.merge(key(i.name, i.unit), Units.toBase(i.quantity, i.unit), Double::sum);
        }
        List<Models.Ingredient> out = new ArrayList<>();
        for (Models.Ingredient g : r.ingredients) {
            Double h = have.get(key(g.name, g.unit));
            double need = Units.toBase(g.quantity, g.unit);
            if (h == null || h + 1e-9 < need) {
                Models.Ingredient m = new Models.Ingredient();
                m.name = g.name;
                m.unit = g.unit;
                double shortBase = need - (h == null ? 0 : h);
                m.quantity = g.quantity * (shortBase / need);
                out.add(m);
            }
        }
        return out;
    }

    private static String key(String name, String unit) {
        return Units.singular(name) + "|" + Units.dimension(unit);
    }
}