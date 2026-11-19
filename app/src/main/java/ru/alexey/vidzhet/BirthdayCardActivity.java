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
    int accent = Birthdays.PINK;

    int dp(float v) { return Ui.dp(this, v); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF111317);
        getWindow().setNavigationBarColor(0xFF111317);
        id = getIntent().getStringExtra("id");
    }

    @Override protected void onResume() {
        super.onResume();
        Birthdays.B b = id == null ? null : Birthdays.find(this, id);
        if (b == null) { finish(); return; }

        // у каждого человека своя гамма (постоянная): свечение и кольцо
        int[] th = Birthdays.theme(b.id);
        accent = th[1];

        FrameLayout frame = new FrameLayout(this);
        frame.addView(new Glow(this, th[0], b.id.hashCode(), dp(56 + 74), dp(74)), new FrameLayout.LayoutParams(-1, -1));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        frame.addView(root, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout ring = new FrameLayout(this);
        ring.setBackground(Ui.oval(accent));
        ring.setPadding(dp(4), dp(4), dp(4), dp(4));
        View av = Ui.avatar(this, b, 140);
        ring.addView(av, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(148), dp(148));
        rp.gravity = Gravity.CENTER_HORIZONTAL;
        rp.topMargin = dp(56);
        root.addView(ring, rp);

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
        setContentView(frame);
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
        iv.setColorFilter(accent);
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

    // однотонный фон, мягкое свечение за фото и редкие искорки
    static class Glow extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        static final int[] C = {0xFFFF6FA8, 0xFFFFC94D, 0xFF4FC3F7, 0xFFB388FF, 0xFF66D17A};
        final int glow, seed, cy, rad;
        Glow(Context c, int glow, int seed, int cy, int rad) {
            super(c); this.glow = glow; this.seed = seed; this.cy = cy; this.rad = rad;
        }

        @Override protected void onDraw(Canvas c) {
            c.drawColor(0xFF111317);
            float cx = getWidth() / 2f;
            p.setShader(new android.graphics.RadialGradient(cx, cy, rad * 3.2f,
                    new int[]{glow, (glow & 0x00FFFFFF) | 0x66000000, 0x00111317},
                    new float[]{0f, 0.55f, 1f}, android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0, 0, getWidth(), getHeight(), p);
            p.setShader(null);
            Random r = new Random(seed);
            float d = getResources().getDisplayMetrics().density;
            float textTop = cy + rad + 10 * d, textBottom = cy + rad + 290 * d;
            for (int i = 0; i < 60; i++) {
                float x = r.nextFloat() * getWidth(), y = r.nextFloat() * getHeight() * 0.9f;
                float s = (1.5f + r.nextInt(3)) * d;
                int col = C[r.nextInt(C.length)];
                if (Math.hypot(x - cx, y - cy) < rad + 18 * d) continue;
                if (y > textTop && y < textBottom) continue;
                p.setColor(col);
                p.setAlpha(200);
                c.drawCircle(x, y, s, p);
            }
        }
    }
}
