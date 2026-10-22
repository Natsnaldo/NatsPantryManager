package com.nats.pantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Settings: toggle expiry warnings and clear the pantry. */
public class SettingsActivity extends BackActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Settings");
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        CheckBox warn = new CheckBox(this);
        warn.setText("Show expiry warnings in the pantry list");
        warn.setChecked(prefs.getBoolean("warn", true));
        warn.setOnCheckedChangeListener((v, on) -> prefs.edit().putBoolean("warn", on).apply());

        Button clear = new Button(this);
        clear.setText("Clear all pantry items");
        clear.setOnClickListener(v -> {
            new DbHelper(this).clearPantry();
            Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
        });
        root.addView(warn);
        root.addView(clear);
        setContentView(root);
    }
}
