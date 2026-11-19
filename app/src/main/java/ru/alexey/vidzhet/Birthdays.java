package ru.alexey.vidzhet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

// Дни рождения: хранятся только в программе (не из контактов)
public class Birthdays {
    public static final int PINK = 0xFFFF6FA8;
    public static final String[] MONTHS = {"январь","февраль","март","апрель","май","июнь","июль",
            "август","сентябрь","октябрь","ноябрь","декабрь"};
    public static final String[] MONTHS_G = {"января","февраля","марта","апреля","мая","июня","июля",
            "августа","сентября","октября","ноября","декабря"};

    public static class B {
        public String id = String.valueOf(System.currentTimeMillis());
        public String name = "";
        public int day, month, year; // month 1..12, year 0 = не указан
    }

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("bdays", Context.MODE_PRIVATE); }

    public static List<B> load(Context c) {
        List<B> l = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(prefs(c).getString("list", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                B b = new B();
                b.id = o.getString("id"); b.name = o.getString("name");
                b.day = o.getInt("day"); b.month = o.getInt("month"); b.year = o.optInt("year", 0);
                l.add(b);
            }
        } catch (Exception ignored) {}
        return l;
    }

    public static void save(Context c, List<B> l) {
        JSONArray a = new JSONArray();
        try {
            for (B b : l) {
                JSONObject o = new JSONObject();
                o.put("id", b.id); o.put("name", b.name);
                o.put("day", b.day); o.put("month", b.month); o.put("year", b.year);
                a.put(o);
            }
        } catch (Exception ignored) {}
        prefs(c).edit().putString("list", a.toString()).apply();
    }

    public static B find(Context c, String id) {
        for (B b : load(c)) if (b.id.equals(id)) return b;
        return null;
    }

    public static File photoFile(Context c, String id) {
        File d = new File(c.getFilesDir(), "photos");
        d.mkdirs();
        return new File(d, id + ".jpg");
    }

    public static File photoSrcFile(Context c, String id) {
        File d = new File(c.getFilesDir(), "photos");
        d.mkdirs();
        return new File(d, id + "_src.jpg");
    }

    public static Bitmap photo(Context c, String id) {
        File f = photoFile(c, id);
        return f.exists() ? BitmapFactory.decodeFile(f.getAbsolutePath()) : null;
    }

    static Calendar todayStart() {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, 0); t.set(Calendar.MINUTE, 0);
        t.set(Calendar.SECOND, 0); t.set(Calendar.MILLISECOND, 0);
        return t;
    }

    public static Calendar next(B b) {
        Calendar t = todayStart();
        Calendar n = (Calendar) t.clone();
        for (int k = 0; k < 2; k++) {
            n.set(Calendar.DAY_OF_MONTH, 1);
            n.set(Calendar.MONTH, b.month - 1);
            n.set(Calendar.DAY_OF_MONTH, Math.min(b.day, n.getActualMaximum(Calendar.DAY_OF_MONTH)));
            if (!n.before(t)) return n;
            n.add(Calendar.YEAR, 1);
        }
        return n;
    }

    public static int daysUntil(B b) {
        return (int) Math.round((next(b).getTimeInMillis() - todayStart().getTimeInMillis()) / 86400000.0);
    }

    // сколько исполнится в ближайший день рождения (0 — год не указан)
    public static int ageNext(B b) {
        if (b.year <= 0) return 0;
        return next(b).get(Calendar.YEAR) - b.year;
    }

    public static List<B> sorted(Context c) {
        List<B> l = load(c);
        Collections.sort(l, (x, y) -> Integer.compare(daysUntil(x), daysUntil(y)));
        return l;
    }

    // кто родился в этот день (month 1..12); 29 февраля в невисокосный год — 28-го
    public static List<B> on(List<B> all, int year, int month, int day) {
        List<B> r = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, 1);
        int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (B b : all) if (b.month == month && Math.min(b.day, max) == day) r.add(b);
        return r;
    }

    public static String shortName(B b) {
        String[] w = b.name.trim().split("\\s+");
        if (w.length >= 2 && w[1].length() > 0) return w[0] + " " + w[1].charAt(0) + ".";
        return b.name.trim();
    }

    // имя для поздравления: первое слово
    public static String shortFirst(B b) {
        String[] w = b.name.trim().split("\\s+");
        return w.length > 0 ? w[0] : b.name;
    }

    public static String initials(B b) {
        String[] w = b.name.trim().split("\\s+");
        String s = "";
        for (int i = 0; i < Math.min(2, w.length); i++) if (w[i].length() > 0) s += w[i].charAt(0);
        return s.toUpperCase();
    }

    public static String info(B b) {
        String s = b.day + " " + MONTHS_G[b.month - 1];
        int age = ageNext(b);
        if (age > 0) s += " · исполнится " + age;
        return s;
    }

    // постоянная цветовая гамма человека: {свечение, яркий цвет}
    static final int[][] THEMES = {{0xFF78284F, 0xFFFF6FA8}, {0xFF4B2C7A, 0xFFB388FF}, {0xFF1E3F7A, 0xFF4FC3F7},
            {0xFF14534F, 0xFF4DD0C4}, {0xFF6E3A14, 0xFFFF8A3D}, {0xFF245A2C, 0xFF66D17A}};
    public static int[] theme(String id) { return THEMES[Math.abs(id.hashCode()) % THEMES.length]; }

    public static String daysText(int d) {
        if (d == 0) return "сегодня";
        if (d == 1) return "завтра";
        return d + " дн.";
    }
}
