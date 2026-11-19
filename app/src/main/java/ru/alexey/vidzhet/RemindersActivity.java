package ru.alexey.vidzhet;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

// Настройка напоминаний: когда, во сколько, текст поздравления. Без ползунков — только выбор
public class RemindersActivity extends Activity {
    Set<String> days;
    int hour;

    int dp(float v) { return Ui.dp(this, v); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF111317);
        getWindow().setNavigationBarColor(0xFF111317);
        days = Reminders.days(this);
        hour = Reminders.hour(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);
        root.setPadding(dp(14), dp(36), dp(14), dp(14));
        TextView t = new TextView(this);
        t.setText("Напоминания");
        t.setTextColor(Color.WHITE);
        t.setTextSize(22);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(t);

        root.addView(label("Когда напоминать (нажмите, чтобы отметить)"));
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        Button[] db = new Button[Reminders.DAYS.length];
        for (int i = 0; i < db.length; i++) {
            final String key = String.valueOf(Reminders.DAYS[i]);
            Button x = chip(Reminders.DAY_NAMES[i]);
            db[i] = x;
            paint(x, days.contains(key));
            x.setOnClickListener(v -> {
                if (days.contains(key)) days.remove(key); else days.add(key);
                paint(x, days.contains(key));
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(40), 1);
            lp.leftMargin = i == 0 ? 0 : dp(4);
            r1.addView(x, lp);
        }
        root.addView(r1);

        root.addView(label("Во сколько"));
        LinearLayout r2 = new LinearLayout(this);
        r2.setOrientation(LinearLayout.HORIZONTAL);
        Button[] hb = new Button[Reminders.HOURS.length];
        for (int i = 0; i < hb.length; i++) {
            final int h = Reminders.HOURS[i];
            Button x = chip(h + ":00");
            hb[i] = x;
            paint(x, h == hour);
            x.setOnClickListener(v -> {
                hour = h;
                for (int k = 0; k < hb.length; k++) paint(hb[k], Reminders.HOURS[k] == hour);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(40), 1);
            lp.leftMargin = i == 0 ? 0 : dp(4);
            r2.addView(x, lp);
        }
        root.addView(r2);

        root.addView(label("Текст поздравления ({имя} заменится на имя)"));
        EditText txt = new EditText(this);
        txt.setText(Reminders.text(this));
        txt.setTextColor(Color.WHITE);
        txt.setTextSize(15);
        txt.setBackground(Ui.round(this, 0xFF1D2128, 10));
        txt.setPadding(dp(10), dp(8), dp(10), dp(8));
        txt.setMinLines(3);
        root.addView(txt, new LinearLayout.LayoutParams(-1, -2));

        root.addView(new android.view.View(this), new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        Button back = new Button(this);
        back.setText("‹ Назад");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(Ui.round(this, 0xFF3A3F47, 22));
        Ui.fit(back);
        back.setOnClickListener(v -> finish());
        Button save = new Button(this);
        save.setText("Сохранить");
        save.setAllCaps(false);
        save.setTextColor(0xFF08210E);
        save.setBackground(Ui.round(this, 0xFF66D17A, 22));
        Ui.fit(save);
        save.setOnClickListener(v -> {
            String tx = txt.getText().toString().trim();
            Reminders.prefs(this).edit()
                    .putStringSet("days", new java.util.HashSet<>(days))
                    .putInt("hour", hour)
                    .putString("text", tx.isEmpty() ? Reminders.DEF_TEXT : tx)
                    .apply();
            Reminders.schedule(this);
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
            finish();
        });
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, dp(46), 1);
        l1.rightMargin = dp(6);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, dp(46), 1);
        l2.leftMargin = dp(6);
        btns.addView(back, l1);
        btns.addView(save, l2);
        root.addView(btns);
        setContentView(root);
    }

    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(0xFFAAAAAA);
        t.setTextSize(13);
        t.setPadding(0, dp(16), 0, dp(6));
        return t;
    }

    Button chip(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        Ui.fit(b);
        return b;
    }

    void paint(Button b, boolean on) {
        b.setBackground(Ui.round(this, on ? 0xFF66D17A : 0xFF2A2E35, 18));
        b.setTextColor(on ? 0xFF08210E : 0xFFDDDDDD);
    }
}
