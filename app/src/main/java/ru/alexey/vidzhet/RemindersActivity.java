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
    LinearLayout checks;
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

        // важно: чтобы напоминания точно приходили (особенно на Xiaomi, Huawei, Honor и т.п.)
        root.addView(label("Чтобы напоминания точно приходили"));
        checks = new LinearLayout(this);
        checks.setOrientation(LinearLayout.VERTICAL);
        root.addView(checks);

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

    @Override protected void onResume() {
        super.onResume();
        if (checks != null) fillChecks();
    }

    static String brand() { return (android.os.Build.MANUFACTURER + "").toLowerCase(); }
    static boolean strictBrand() {
        String b = brand();
        return b.contains("xiaomi") || b.contains("redmi") || b.contains("poco") || b.contains("huawei")
                || b.contains("honor") || b.contains("oppo") || b.contains("vivo") || b.contains("realme") || b.contains("oneplus");
    }

    void fillChecks() {
        checks.removeAllViews();
        android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
        boolean notif = nm.areNotificationsEnabled();
        checkRow(notif, "Уведомления разрешены", "Уведомления выключены", "Включить", () -> {
            android.content.Intent i = new android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(i);
        });
        android.os.PowerManager pm = getSystemService(android.os.PowerManager.class);
        boolean batt = pm.isIgnoringBatteryOptimizations(getPackageName());
        checkRow(batt, "Экономия батареи не мешает", "Экономия батареи может усыпить", "Снять", () -> {
            try {
                startActivity(new android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        android.net.Uri.parse("package:" + getPackageName())));
            } catch (Exception e) { openAppSettings(); }
        });
        if (strictBrand()) {
            checkRow(false, null, "Автозапуск: включите для «Часы-календарь»", "Открыть", this::openAutostart);
            TextView h = new TextView(this);
            h.setText("На " + android.os.Build.MANUFACTURER + " телефон сам «усыпляет» программы. В открывшемся окне найдите «Часы-календарь» и включите автозапуск. Если окно не то — в «О приложении» включите «Автозапуск» и в «Контроль активности» выберите «Нет ограничений».");
            h.setTextColor(0xFF9A9A9A);
            h.setTextSize(12);
            h.setPadding(dp(4), dp(4), dp(4), 0);
            checks.addView(h);
        }
    }

    void checkRow(boolean ok, String okText, String badText, String btnText, Runnable fix) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(android.view.Gravity.CENTER_VERTICAL);
        r.setBackground(Ui.round(this, 0xFF1D2128, 10));
        r.setPadding(dp(10), dp(4), dp(6), dp(4));
        TextView t = new TextView(this);
        t.setText((ok ? "✓  " : "⚠  ") + (ok ? okText : badText));
        t.setTextColor(ok ? 0xFF66D17A : 0xFFFFC94D);
        t.setTextSize(14);
        r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        if (!ok) {
            Button b = new Button(this);
            b.setText(btnText);
            b.setAllCaps(false);
            b.setTextColor(0xFF3A1D00);
            b.setBackground(Ui.round(this, 0xFFFFC94D, 16));
            Ui.fit(b);
            b.setOnClickListener(v -> fix.run());
            r.addView(b, new LinearLayout.LayoutParams(dp(96), dp(36)));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(46));
        lp.bottomMargin = dp(4);
        checks.addView(r, lp);
    }

    void openAppSettings() {
        startActivity(new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:" + getPackageName())));
    }

    void openAutostart() {
        String[][] comps = {
                {"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"},
                {"com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
                {"com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
                {"com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"},
                {"com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"}};
        for (String[] c : comps) {
            try {
                startActivity(new android.content.Intent().setComponent(new android.content.ComponentName(c[0], c[1])));
                return;
            } catch (Exception ignored) {}
        }
        openAppSettings();
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
