package com.nats.pantrymanager;

import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

/** Add a new pantry item, or edit/delete an existing one (when an "id" extra is passed). */
public class ItemActivity extends BackActivity {
    private DbHelper db;
    private Models.Item item;
    private EditText name, qty, expiry;
    private Spinner unit;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DbHelper(this);
        long id = getIntent().getLongExtra("id", 0);
        boolean editing = id != 0;
        item = editing ? db.item(id) : new Models.Item();
        if (item == null) { finish(); return; } // item no longer exists
        setTitle(editing ? "Edit item" : "Add item");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        name = addField(root, "Item name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        qty = addField(root, "Quantity", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addLabel(root, "Unit");
        unit = new Spinner(this);
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Units.ALL));
        root.addView(unit);
        expiry = addField(root, "Expiry (optional, yyyy-MM-dd)", InputType.TYPE_CLASS_TEXT);

        Button saveBtn = new Button(this);
        saveBtn.setText("Save");
        saveBtn.setOnClickListener(v -> save());
        root.addView(saveBtn);

        if (editing) {
            if (item.name != null) name.setText(item.name);
            qty.setText(Units.fmt(item.quantity));
            if (item.expiry != null) expiry.setText(item.expiry);
            int unitIndex = Arrays.asList(Units.ALL).indexOf(item.unit);
            if (unitIndex >= 0) unit.setSelection(unitIndex);
            Button deleteBtn = new Button(this);
            deleteBtn.setText("Delete");
            deleteBtn.setOnClickListener(v -> { db.delete(item.id); finish(); });
            root.addView(deleteBtn);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void save() {
        String n = name.getText().toString().trim();
        String q = qty.getText().toString().trim();
        String e = expiry.getText().toString().trim();
        double quantity;
        if (n.isEmpty()) { error(name, "Name required"); return; }
        try { quantity = Double.parseDouble(q); }
        catch (NumberFormatException ex) { error(qty, "Enter a number"); return; }
        if (quantity <= 0) { error(qty, "Must be above 0"); return; }
        if (!e.isEmpty() && !Units.validDate(e)) {
            error(expiry, "Use yyyy-MM-dd"); return;
        }
        item.name = n; item.quantity = quantity;
        item.unit = (String) unit.getSelectedItem(); item.expiry = e;
        db.save(item);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    /** Shows the problem on the field and as a toast so a failed save is never silent. */
    private void error(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private EditText addField(LinearLayout root, String label, int inputType) {
        addLabel(root, label);
        EditText e = new EditText(this);
        e.setInputType(inputType);
        e.setSingleLine(true);
        root.addView(e);
        return e;
    }

    private void addLabel(LinearLayout root, String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setPadding(0, dp(12), 0, 0);
        root.addView(t);
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
