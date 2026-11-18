package ru.alexey.vidzhet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    static final String LINK = "https://github.com/Alexey-Print/vidzhet/releases/download/latest/vidzhet.apk";
    SharedPreferences p;
    FrameLayout preview;
    LinearLayout root;

    int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = getSharedPreferences(ClockWidget.P, MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.BLACK);
        build();
    }

    void build() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));

        preview = new FrameLayout(this);
        root.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));
        refresh();

        // кнопки сразу под виджетом — всегда видны
        Button save = new Button(this);
        save.setText("Сохранить");
        save.setAllCaps(false);
        save.setTextSize(15);
        save.setTextColor(0xFF08210E);
        save.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF66D17A));
        save.setOnClickListener(v -> {
            ClockWidget.updateAll(this);
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
            finish();
        });
        Button share = new Button(this);
        share.setText("Поделиться");
        share.setAllCaps(false);
        share.setTextSize(15);
        share.setTextColor(Color.WHITE);
        share.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3A3F47));
        share.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
            i.setType("application/vnd.android.package-archive");
            i.putExtra(android.content.Intent.EXTRA_STREAM, ApkProvider.URI);
            i.setClipData(android.content.ClipData.newRawUri(ApkProvider.NAME, ApkProvider.URI));
            i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(i, "Отправить программу"));
        });
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, dp(44), 1);
        l1.rightMargin = dp(6);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, dp(44), 1);
        l2.leftMargin = dp(6);
        btns.addView(share, l1);
        btns.addView(save, l2);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = dp(4);
        root.addView(btns, bp);

        colorRow("Дата", "date", null);
        colorRow("День недели", "wday", null);
        colorRow("Цифровые часы", "time", null);
        colorRow("Кружок «сегодня»", "today", null);
        colorRow("Выходные дни", "weekend", null);

        Switch sw = new Switch(this);
        sw.setChecked(ClockWidget.showSec(p));
        int[][] st = {{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new android.content.res.ColorStateList(st, new int[]{0xFF66D17A, 0xFFBDBDBD}));
        sw.setTrackTintList(new android.content.res.ColorStateList(st, new int[]{0xFF2E6B3A, 0xFF555555}));
        sw.setOnCheckedChangeListener((v, on) -> { p.edit().putBoolean("showSec", on).apply(); refresh(); });
        colorRow("Секундная стрелка", "second", sw);

        // фон: подпись и ползунок в одну строку
        LinearLayout fon = new LinearLayout(this);
        fon.setOrientation(LinearLayout.HORIZONTAL);
        fon.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView al = label("Фон " + ClockWidget.alpha(p) + "%");
        al.setPadding(0, 0, 0, 0);
        fon.addView(al, new LinearLayout.LayoutParams(dp(90), -2));
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(ClockWidget.alpha(p));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int v, boolean u) {
                al.setText("Фон " + v + "%");
                p.edit().putInt("alpha", v).apply(); refresh();
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
        fon.addView(sb, new LinearLayout.LayoutParams(0, dp(36), 1));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(6);
        root.addView(fon, fp);

        TextView hint = new TextView(this);
        String ver = "";
        try { ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception ignored) {}
        hint.setText("Цифры — настройки, календарь — большой календарь, стрелки — будильник. Версия " + ver);
        hint.setTextColor(0xFF9A9A9A);
        hint.setTextSize(11);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(4);
        root.addView(hint, hp);

        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.setBackgroundColor(0xFF111317);
        sv.addView(root);
        setContentView(sv);
    }

    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(14);
        t.setPadding(0, dp(4), 0, 0);
        return t;
    }

    void colorRow(String title, String key, View extra) {
        if (extra == null) {
            root.addView(label(title));
        } else {
            LinearLayout h = new LinearLayout(this);
            h.setOrientation(LinearLayout.HORIZONTAL);
            h.setGravity(Gravity.CENTER_VERTICAL);
            h.addView(label(title), new LinearLayout.LayoutParams(0, -2, 1));
            h.addView(extra);
            root.addView(h);
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int sel = p.getInt(key, defaultFor(key));
        View[] rings = new View[Pal.COLORS.length];
        for (int i = 0; i < Pal.COLORS.length; i++) {
            FrameLayout cell = new FrameLayout(this);
            View sw = new View(this);
            sw.setBackgroundResource(Pal.SWATCH[i]);
            FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER);
            cell.addView(sw, sp);
            View ring = new View(this);
            ring.setBackgroundResource(R.drawable.ring);
            ring.setVisibility(i == sel ? View.VISIBLE : View.INVISIBLE);
            cell.addView(ring, new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER));
            rings[i] = ring;
            final int idx = i;
            cell.setOnClickListener(v -> {
                p.edit().putInt(key, idx).apply();
                for (int k = 0; k < rings.length; k++) rings[k].setVisibility(k == idx ? View.VISIBLE : View.INVISIBLE);
                refresh();
            });
            row.addView(cell, new LinearLayout.LayoutParams(0, dp(30), 1));
        }
        root.addView(row);
    }

    int defaultFor(String key) {
        switch (key) {
            case "time": return ClockWidget.time(p);
            case "today": return ClockWidget.today(p);
            case "date": return ClockWidget.date(p);
            case "wday": return ClockWidget.wday(p);
            case "weekend": return ClockWidget.weekend(p);
            default: return ClockWidget.second(p);
        }
    }

    void refresh() {
        ClockWidget.updateAll(this);
        preview.removeAllViews();
        try {
            View v = ClockWidget.build(this).apply(this, preview);
            preview.addView(v);
        } catch (Exception ignored) {}
    }
}
