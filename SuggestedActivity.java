package com.nats.pantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SuggestedActivity extends BackActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_list);
        setTitle("Suggested Recipes");
        DbHelper db = new DbHelper(this);
        List<Models.Item> pantry = db.items();
        List<Models.Recipe> ready = new ArrayList<>(), partial = new ArrayList<>();
        List<String> readyLabels = new ArrayList<>(), partialLabels = new ArrayList<>();
        for (Models.Recipe r : db.recipes()) {
            int missing = Matcher.missing(r, pantry).size();
            if (missing == 0) {
                ready.add(r);
                readyLabels.add(r.name + "\n✔ You have everything");
            } else {
                partial.add(r);
                partialLabels.add(r.name + "\nMissing " + missing + " of " + r.ingredients.size() + " ingredients");
            }
        }
        List<Models.Recipe> shown = new ArrayList<>(ready);
        shown.addAll(partial);
        List<String> labels = new ArrayList<>(readyLabels);
        labels.addAll(partialLabels);

        ListView list = findViewById(R.id.list);
        list.setEmptyView(findViewById(R.id.empty));
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
        list.setOnItemClickListener((p, v, pos, id) -> startActivity(
                new Intent(this, RecipeDetailActivity.class).putExtra("id", shown.get(pos).id)));
    }
}