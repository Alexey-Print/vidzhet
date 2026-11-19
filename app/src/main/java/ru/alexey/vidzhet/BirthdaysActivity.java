package ru.alexey.vidzhet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

// Список дней рождения: ближайшие сверху
public class BirthdaysActivity extends Activity {
    LinearLayout list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF111317);
        getWindow().setNavigationBarColor(0xFF111317);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);
        root.setPadding(Ui.dp(this, 12), Ui.dp(this, 36), Ui.dp(this, 12), Ui.dp(this, 12));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = new TextView(this);
        t.setText("Дни рождения");
        t.setTextColor(Color.WHITE);
        t.setTextSize(22);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        head.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Button add = new Button(this);
        add.setText("+");
        add.setTextSize(24);
        add.setTextColor(0xFF4B1528);
        add.setPadding(0, 0, 0, Ui.dp(this, 3));
        add.setBackground(Ui.oval(Birthdays.PINK));
        add.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
                .setItems(new String[]{"Добавить одного", "Вставить список из буфера"}, (d, w) -> {
                    if (w == 0) startActivity(new Intent(this, EditBirthdayActivity.class));
                    else importClipboard();
                }).show());
        Button bell = new Button(this);
        bell.setText("🔔");
        bell.setTextSize(18);
        bell.setPadding(0, 0, 0, 0);
        bell.setBackground(Ui.oval(0xFF2A2E35));
        bell.setOnClickListener(v -> startActivity(new Intent(this, RemindersActivity.class)));
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46));
        bl.rightMargin = Ui.dp(this, 8);
        head.addView(bell, bl);
        head.addView(add, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        root.addView(head);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1);
        sp.topMargin = Ui.dp(this, 10);
        root.addView(sv, sp);

        // внизу: «Назад» — в календарь, «Выйти» — на главный экран
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        Button back = new Button(this);
        back.setText("‹ Назад");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(Ui.round(this, 0xFF3A3F47, 22));
        Ui.fit(back);
        back.setOnClickListener(v -> finish());
        Button exit = new Button(this);
        exit.setText("Выйти");
        exit.setAllCaps(false);
        exit.setTextColor(Color.WHITE);
        exit.setBackground(Ui.round(this, 0xFF3A3F47, 22));
        Ui.fit(exit);
        exit.setOnClickListener(v -> {
            Intent h = new Intent(Intent.ACTION_MAIN);
            h.addCategory(Intent.CATEGORY_HOME);
            h.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(h);
            finishAffinity();
        });
        LinearLayout.LayoutParams n1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        n1.rightMargin = Ui.dp(this, 6);
        LinearLayout.LayoutParams n2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        n2.leftMargin = Ui.dp(this, 6);
        nav.addView(back, n1);
        nav.addView(exit, n2);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = Ui.dp(this, 8);
        root.addView(nav, np);
        setContentView(root);
    }

    // строки вида «25.02.1998 Марина Герина» или «15.03 Волков Володя»
    void importClipboard() {
        android.content.ClipboardManager cm = getSystemService(android.content.ClipboardManager.class);
        String text = "";
        try { text = cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString(); } catch (Exception ignored) {}
        java.util.List<Birthdays.B> all = Birthdays.load(this);
        int added = 0, skipped = 0;
        java.util.regex.Pattern pt = java.util.regex.Pattern.compile("^\\s*(\\d{1,2})[.\\-/](\\d{1,2})(?:[.\\-/](\\d{4}))?\\s+(.+?)\\s*$");
        long idBase = System.currentTimeMillis();
        for (String line : text.split("\\r?\\n")) {
            java.util.regex.Matcher m = pt.matcher(line);
            if (!m.matches()) continue;
            int d = Integer.parseInt(m.group(1)), mo = Integer.parseInt(m.group(2));
            int y = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
            String name = m.group(4);
            if (mo < 1 || mo > 12 || d < 1 || d > 31) continue;
            boolean dup = false;
            for (Birthdays.B o : all) if (o.name.equalsIgnoreCase(name) && o.day == d && o.month == mo) dup = true;
            if (dup) { skipped++; continue; }
            Birthdays.B b = new Birthdays.B();
            b.id = String.valueOf(idBase + added);
            b.name = name; b.day = d; b.month = mo; b.year = y;
            all.add(b);
            added++;
        }
        Birthdays.save(this, all);
        ClockWidget.updateAll(this);
        android.widget.Toast.makeText(this, added == 0 && skipped == 0
                ? "В буфере нет списка. Скопируйте список целиком и попробуйте снова"
                : "Добавлено: " + added + (skipped > 0 ? ", уже были: " + skipped : ""), android.widget.Toast.LENGTH_LONG).show();
        onResume();
    }

    @Override protected void onResume() {
        super.onResume();
        Reminders.schedule(this);
        if (android.os.Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 6);
        }
        list.removeAllViews();
        List<Birthdays.B> all = Birthdays.sorted(this);
        if (all.isEmpty()) {
            TextView e = new TextView(this);
            e.setText("Пока никого нет.\nНажмите «+», чтобы добавить");
            e.setTextColor(0xFF888888);
            e.setTextSize(15);
            e.setGravity(Gravity.CENTER);
            e.setPadding(0, Ui.dp(this, 60), 0, 0);
            list.addView(e);
            return;
        }
        for (Birthdays.B b : all) {
            int d = Birthdays.daysUntil(b);
            LinearLayout r = Ui.personRow(this, b, Birthdays.daysText(d), d <= 1 ? Birthdays.PINK : 0xFF66D17A);
            r.setOnClickListener(v -> startActivity(new Intent(this, BirthdayCardActivity.class).putExtra("id", b.id)));
            list.addView(r);
            android.view.View line = new android.view.View(this);
            line.setBackgroundColor(0xFF2A2E35);
            list.addView(line, new LinearLayout.LayoutParams(-1, 1));
        }
    }
}
