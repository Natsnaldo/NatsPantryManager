package com.nats.pantrymanager;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecipeDetailActivity extends BackActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        long id = getIntent().getLongExtra("id", 0);
        DbHelper db = new DbHelper(this);
        Models.Recipe found = null;
        for (Models.Recipe r : db.recipes()) {
            if (r.id == id) {
                found = r;
                break;
            }
        }
        if (found == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(found.name);
        List<Models.Ingredient> missing = Matcher.missing(found, db.items());
        StringBuilder sb = new StringBuilder();
        if (missing.isEmpty()) {
            sb.append("\u2714 You have everything you need!\n\n");
        } else {
            sb.append("You still need\n");
            for (Models.Ingredient m : missing) {
                sb.append("\u2022 ").append(m.name).append(": ")
                        .append(Units.fmt(Math.round(m.quantity * 100) / 100.0))
                        .append(' ').append(m.unit).append('\n');
            }
            sb.append('\n');
        }
        sb.append("All ingredients\n");
        for (Models.Ingredient g : found.ingredients) {
            sb.append("\u2022 ").append(g.name).append(": ").append(Units.fmt(g.quantity)).append(' ').append(g.unit).append('\n');
        }
        sb.append("\nMethod\n").append(found.instructions);

        TextView tv = new TextView(this);
        tv.setTextSize(18);
        tv.setPadding(40, 40, 40, 40);
        tv.setText(sb.toString());

        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        setContentView(sv);
    }
}