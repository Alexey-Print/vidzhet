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
        Button back = new Button(this);
        back.setText("‹");
        back.setTextSize(24);
        back.setTextColor(Color.WHITE);
        back.setPadding(0, 0, 0, Ui.dp(this, 3));
        back.setBackground(Ui.oval(0xFF3A3F47));
        back.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46));
        bl.rightMargin = Ui.dp(this, 10);
        head.addView(back, bl);
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
        add.setOnClickListener(v -> startActivity(new Intent(this, EditBirthdayActivity.class)));
        head.addView(add, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        root.addView(head);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1);
        sp.topMargin = Ui.dp(this, 10);
        root.addView(sv, sp);
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
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
            r.setOnClickListener(v -> startActivity(new Intent(this, EditBirthdayActivity.class).putExtra("id", b.id)));
            list.addView(r);
            android.view.View line = new android.view.View(this);
            line.setBackgroundColor(0xFF2A2E35);
            list.addView(line, new LinearLayout.LayoutParams(-1, 1));
        }
    }
}
