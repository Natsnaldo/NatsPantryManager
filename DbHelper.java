package com.nats.pantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite storage for the pantry, recipes and recipe ingredients. */
class DbHelper extends SQLiteOpenHelper {
    // name|instructions|ingredient:quantity:unit;...
    private static final String[] SEED = {
        "Scrambled Eggs|Whisk the eggs with salt and cook gently in butter.|egg:3:pcs;butter:10:g;salt:1:tsp",
        "Pancakes|Mix everything into a batter and fry ladlefuls until golden.|flour:200:g;milk:300:ml;egg:2:pcs;sugar:1:tbsp",
        "Tomato Pasta|Boil pasta. Fry onion and garlic, add tomatoes, simmer, then toss together.|pasta:200:g;tomato:4:pcs;onion:1:pcs;garlic:2:pcs;olive oil:2:tbsp",
        "Rice and Beans|Cook rice. Fry onion, add beans, combine and season.|rice:200:g;bean:150:g;onion:1:pcs",
        "Cheese Omelette|Beat eggs with milk, pour into a pan, add cheese and fold.|egg:3:pcs;cheese:50:g;milk:50:ml",
        "Cheese Sandwich|Butter the bread, add cheese and toast.|bread:2:pcs;cheese:40:g;butter:10:g",
        "Vegetable Soup|Chop vegetables, simmer in salted water for 25 minutes.|potato:2:pcs;carrot:2:pcs;onion:1:pcs;water:500:ml;salt:1:tsp",
        "Mashed Potatoes|Boil potatoes, mash with butter, milk and salt.|potato:4:pcs;butter:30:g;milk:100:ml;salt:1:tsp",
        "Fried Rice|Stir-fry rice, carrot and egg, finish with soy sauce.|rice:200:g;egg:2:pcs;carrot:1:pcs;soy sauce:2:tbsp",
        "Banana Smoothie|Blend everything until smooth.|banana:2:pcs;milk:250:ml;honey:1:tbsp",
        "French Toast|Dip bread in egg, milk and sugar, fry until golden.|bread:4:pcs;egg:2:pcs;milk:100:ml;sugar:1:tsp",
        "Chicken Stir-fry|Fry chicken, add sliced vegetables, season with soy sauce.|chicken:300:g;onion:1:pcs;carrot:1:pcs;soy sauce:2:tbsp",
        "Greek Salad|Chop vegetables, crumble feta, dress with olive oil.|tomato:3:pcs;cucumber:1:pcs;feta:100:g;olive oil:2:tbsp",
        "Porridge|Simmer oats in milk, stirring, until thick. Sweeten.|oat:80:g;milk:250:ml;sugar:1:tsp",
        "Garlic Bread|Spread garlic butter on bread and bake until crisp.|bread:4:pcs;butter:40:g;garlic:2:pcs"
    };

    DbHelper(Context c) { super(c, "pantry.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,"
                + " quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipe(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, instructions TEXT)");
        db.execSQL("CREATE TABLE recipe_ingredient(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " recipe_id INTEGER NOT NULL REFERENCES recipe(id), name TEXT NOT NULL,"
                + " quantity REAL NOT NULL, unit TEXT NOT NULL)");
        for (String line : SEED) {
            String[] p = line.split("\\|");
            ContentValues cv = new ContentValues();
            cv.put("name", p[0]);
            cv.put("instructions", p[1]);
            long rid = db.insert("recipe", null, cv);
            for (String ing : p[2].split(";")) {
                String[] f = ing.split(":");
                ContentValues iv = new ContentValues();
                iv.put("recipe_id", rid);
                iv.put("name", f[0]);
                iv.put("quantity", Double.parseDouble(f[1]));
                iv.put("unit", f[2]);
                db.insert("recipe_ingredient", null, iv);
            }
        }
    }

    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredient");
        db.execSQL("DROP TABLE IF EXISTS recipe");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    void save(Models.Item i) {
        ContentValues cv = new ContentValues();
        cv.put("name", i.name);
        cv.put("quantity", i.quantity);
        cv.put("unit", i.unit);
        cv.put("expiry", i.expiry);
        if (i.id == 0) getWritableDatabase().insert("pantry", null, cv);
        else getWritableDatabase().update("pantry", cv, "id=?", new String[]{String.valueOf(i.id)});
    }

    void delete(long id) { getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)}); }

    void clearPantry() { getWritableDatabase().delete("pantry", null, null); }

    List<Models.Item> items() {
        List<Models.Item> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE")) {
            while (c.moveToNext()) {
                Models.Item i = new Models.Item();
                i.id = c.getLong(0); i.name = c.getString(1); i.quantity = c.getDouble(2);
                i.unit = c.getString(3); i.expiry = c.getString(4);
                out.add(i);
            }
        }
        return out;
    }

    Models.Item item(long id) {
        for (Models.Item i : items()) if (i.id == id) return i;
        return null;
    }

    List<Models.Recipe> recipes() {
        List<Models.Recipe> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipe", null, null, null, null, null, "name")) {
            while (c.moveToNext()) {
                Models.Recipe r = new Models.Recipe();
                r.id = c.getLong(0); r.name = c.getString(1); r.instructions = c.getString(2);
                try (Cursor ic = db.query("recipe_ingredient", null, "recipe_id=?",
                        new String[]{String.valueOf(r.id)}, null, null, "id")) {
                    while (ic.moveToNext()) {
                        Models.Ingredient g = new Models.Ingredient();
                        g.name = ic.getString(2); g.quantity = ic.getDouble(3); g.unit = ic.getString(4);
                        r.ingredients.add(g);
                    }
                }
                out.add(r);
            }
        }
        return out;
    }
}
