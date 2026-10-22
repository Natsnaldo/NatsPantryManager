package com.nats.pantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;

/** Base class for secondary screens: adds an up arrow in the action bar that returns to the previous screen. */
abstract class BackActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override public boolean onOptionsItemSelected(MenuItem m) {
        if (m.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(m);
    }
}
