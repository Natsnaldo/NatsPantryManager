package com.nats.pantrymanager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Unit conversion, name normalisation and date helpers used by matching and the UI. */
final class Units {
    static final String[] ALL = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    /** Units only compare within the same dimension (mass, volume or count). */
    static String dimension(String u) {
        if (u == null) return "volume";
        switch (u) {
            case "g": case "kg": return "mass";
            case "pcs": return "count";
            default: return "volume";
        }
    }

    /** Converts to the base unit of the dimension: grams, millilitres or pieces. */
    static double toBase(double q, String u) {
        if (u == null) return q;
        switch (u) {
            case "kg": case "l": return q * 1000;
            case "tsp": return q * 5;
            case "tbsp": return q * 15;
            case "cup": return q * 250;
            default: return q;
        }
    }

    /** Lower-cases and strips plural endings so "Eggs", "egg" and "tomatoes", "tomato" match. */
    static String singular(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.endsWith("ies")) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes") || s.endsWith("ches") || s.endsWith("shes")
                || s.endsWith("xes") || s.endsWith("sses")) return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && !s.endsWith("ss")) return s.substring(0, s.length() - 1);
        return s;
    }

    static String fmt(double q) {
        return q == Math.rint(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    /** Whole days until expiry (negative if expired); Long.MAX_VALUE when no date is set. */
    static long daysLeft(String expiry) {
        if (expiry == null || expiry.isEmpty()) return Long.MAX_VALUE;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            Date expiryDate = sdf.parse(expiry);
            if (expiryDate == null) return Long.MAX_VALUE;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar exp = Calendar.getInstance();
            exp.setTime(expiryDate);
            exp.set(Calendar.HOUR_OF_DAY, 0);
            exp.set(Calendar.MINUTE, 0);
            exp.set(Calendar.SECOND, 0);
            exp.set(Calendar.MILLISECOND, 0);

            long diffMs = exp.getTimeInMillis() - today.getTimeInMillis();
            return diffMs / 86400000L;
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    static boolean validDate(String s) {
        if (s == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            sdf.parse(s);
            return s.matches("\\d{4}-\\d{2}-\\d{2}");
        } catch (Exception e) {
            return false;
        }
    }
}
