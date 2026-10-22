package com.nats.pantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/** Home screen: the pantry list with add, suggested recipes and settings actions. */
public class MainActivity extends Activity {
    private DbHelper db;
    private ListView list;
    private List<Models.Item> items = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        db = new DbHelper(this);
        list = findViewById(R.id.list);
        list.setEmptyView(findViewById(R.id.empty));
        list.setOnItemClickListener((p, v, pos, id) ->
                startActivity(new Intent(this, ItemActivity.class).putExtra("id", items.get(pos).id)));
        findViewById(R.id.add).setOnClickListener(v -> startActivity(new Intent(this, ItemActivity.class)));
        findViewById(R.id.suggest).setOnClickListener(v -> startActivity(new Intent(this, SuggestedActivity.class)));
        findViewById(R.id.settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    /** targetSdk 35+ forces edge-to-edge, so content would sit under the action bar; pad it down. */
    @Override public void onContentChanged() {
        super.onContentChanged();
        View content = findViewById(android.R.id.content);
        TypedValue tv = new TypedValue();
        int bar = getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)
                ? TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) : 0;
        content.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop() + bar,
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());
            return WindowInsets.CONSUMED;
        });
        content.requestApplyInsets();
    }

    @Override protected void onResume() {
        super.onResume();
        boolean warn = getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("warn", true);
        items = db.items();
        List<String> rows = new ArrayList<>();
        for (Models.Item i : items) {
            String row = i.name + " — " + Units.fmt(i.quantity) + " " + i.unit;
            if (i.expiry != null && !i.expiry.isEmpty()) row += "  (exp " + i.expiry + ")";
            long d = Units.daysLeft(i.expiry);
            if (warn && d < 0) row += "  EXPIRED";
            else if (warn && d <= 3) row += "  expires soon";
            rows.add(row);
        }
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }
}
