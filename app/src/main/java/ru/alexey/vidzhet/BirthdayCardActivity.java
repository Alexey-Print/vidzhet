package ru.alexey.vidzhet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Random;

// Карточка человека: «открытка» с конфетти
public class BirthdayCardActivity extends Activity {
    static final String[] WD = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"};
    String id;

    int dp(float v) { return Ui.dp(this, v); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF3A2330);
        getWindow().setNavigationBarColor(0xFF111317);
        id = getIntent().getStringExtra("id");
    }

    @Override protected void onResume() {
        super.onResume();
        Birthdays.B b = id == null ? null : Birthdays.find(this, id);
        if (b == null) { finish(); return; }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);

        // шапка с конфетти и фото на её краю
        FrameLayout top = new FrameLayout(this);
        View conf = new Confetti(this);
        top.addView(conf, new FrameLayout.LayoutParams(-1, dp(130)));
        FrameLayout ring = new FrameLayout(this);
        ring.setBackground(Ui.oval(Birthdays.PINK));
        ring.setPadding(dp(4), dp(4), dp(4), dp(4));
        View av = Ui.avatar(this, b, 132);
        ring.addView(av, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(dp(140), dp(140), Gravity.CENTER_HORIZONTAL);
        rp.topMargin = dp(60);
        top.addView(ring, rp);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(204)));

        TextView name = new TextView(this);
        name.setText(b.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(24);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = dp(10);
        root.addView(name, np);

        TextView date = new TextView(this);
        date.setText(b.day + " " + Birthdays.MONTHS_G[b.month - 1] + (b.year > 0 ? " " + b.year : ""));
        date.setTextColor(0xFFAAAAAA);
        date.setTextSize(15);
        date.setGravity(Gravity.CENTER);
        root.addView(date);

        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(10));
        root.addView(new View(this), sp);
        int age = Birthdays.ageNext(b);
        if (age > 0) root.addView(chip(R.drawable.ic_cake, "Исполнится " + age));
        int d = Birthdays.daysUntil(b);
        String wd = WD[Birthdays.next(b).get(Calendar.DAY_OF_WEEK) - 1];
        String when = d == 0 ? "Сегодня! 🎉" : d == 1 ? "Завтра · " + wd : "Через " + d + " " + days(d) + " · " + wd;
        root.addView(chip(R.drawable.ic_calendar, when));

        root.addView(new View(this), new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setPadding(dp(12), 0, dp(12), dp(14));
        Button back = new Button(this);
        back.setText("‹ Назад");
        back.setAllCaps(false);
        back.setTextColor(Color.WHITE);
        back.setBackground(Ui.round(this, 0xFF3A3F47, 22));
        Ui.fit(back);
        back.setOnClickListener(v -> finish());
        Button edit = new Button(this);
        edit.setText("Изменить");
        edit.setAllCaps(false);
        edit.setTextColor(0xFF08210E);
        edit.setBackground(Ui.round(this, 0xFF66D17A, 22));
        Ui.fit(edit);
        edit.setOnClickListener(v -> startActivity(new Intent(this, EditBirthdayActivity.class).putExtra("id", id)));
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, dp(46), 1);
        l1.rightMargin = dp(6);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, dp(46), 1);
        l2.leftMargin = dp(6);
        btns.addView(back, l1);
        btns.addView(edit, l2);
        root.addView(btns);
        setContentView(root);
    }

    static String days(int n) {
        int a = n % 100, b = n % 10;
        if (a >= 11 && a <= 14) return "дней";
        if (b == 1) return "день";
        if (b >= 2 && b <= 4) return "дня";
        return "дней";
    }

    LinearLayout chip(int icon, String text) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.HORIZONTAL);
        c.setGravity(Gravity.CENTER_VERTICAL);
        c.setBackground(Ui.round(this, 0xFF1D2128, 12));
        c.setPadding(dp(14), dp(10), dp(14), dp(10));
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setImageResource(icon);
        c.addView(iv, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);
        t.setPadding(dp(12), 0, 0, 0);
        c.addView(t);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(14), dp(5), dp(14), dp(5));
        c.setLayoutParams(lp);
        return c;
    }

    // разноцветное конфетти на тёмно-розовом фоне
    static class Confetti extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        static final int[] C = {0xFFFF6FA8, 0xFFFFC94D, 0xFF4FC3F7, 0xFF66D17A, 0xFFB388FF, 0xFFFF8A3D};
        Confetti(Context c) { super(c); }

        @Override protected void onDraw(Canvas c) {
            c.drawColor(0xFF3A2330);
            Random r = new Random(7);
            float d = getResources().getDisplayMetrics().density;
            for (int i = 0; i < 46; i++) {
                p.setColor(C[i % C.length]);
                p.setAlpha(210);
                float x = r.nextFloat() * getWidth(), y = r.nextFloat() * getHeight();
                float s = (3 + r.nextInt(4)) * d;
                c.save();
                c.rotate(r.nextInt(90), x, y);
                if (i % 3 == 0) c.drawCircle(x, y, s / 1.4f, p);
                else c.drawRect(x - s, y - s / 2.5f, x + s, y + s / 2.5f, p);
                c.restore();
            }
        }
    }
}
