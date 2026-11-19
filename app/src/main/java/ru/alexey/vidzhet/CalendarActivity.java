package ru.alexey.vidzhet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

// Большой календарь: листание месяцев стрелками и пальцем
public class CalendarActivity extends Activity {
    static final String[] MN = {"Январь","Февраль","Март","Апрель","Май","Июнь","Июль",
            "Август","Сентябрь","Октябрь","Ноябрь","Декабрь"};
    int offset = 0, selDay = 0;
    LinearLayout cards;
    java.util.List<Birthdays.B> all;
    TextView title;
    GridLayout grid;
    SharedPreferences p;
    GestureDetector gd;
    int accent, onAccent, todayC, onToday, weekendC;

    int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    GradientDrawable round(int color, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = getSharedPreferences(ClockWidget.P, MODE_PRIVATE);
        accent = Pal.COLORS[ClockWidget.time(p)];
        onAccent = ClockWidget.isLight(accent) ? 0xFF101010 : 0xFFFFFFFF;
        todayC = Pal.COLORS[ClockWidget.today(p)];
        onToday = ClockWidget.isLight(todayC) ? 0xFF101010 : 0xFFFFFFFF;
        weekendC = Pal.COLORS[ClockWidget.weekend(p)];
        getWindow().setStatusBarColor(0xFF111317);
        getWindow().setNavigationBarColor(0xFF111317);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);
        root.setPadding(dp(14), dp(40), dp(14), dp(16));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        Button prev = arrow("‹");
        prev.setOnClickListener(v -> { offset--; selDay = 0; draw(); });
        Button next = arrow("›");
        next.setOnClickListener(v -> { offset++; selDay = 0; draw(); });
        title = new TextView(this);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        head.addView(prev, new LinearLayout.LayoutParams(dp(52), dp(52)));
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        head.addView(next, new LinearLayout.LayoutParams(dp(52), dp(52)));
        root.addView(head);

        grid = new GridLayout(this);
        grid.setColumnCount(7);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, -2);
        gp.topMargin = dp(18);
        root.addView(grid, gp);

        cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(8);
        root.addView(cards, cp);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        Button today = new Button(this);
        today.setText("Сегодня");
        today.setAllCaps(false);
        today.setTextSize(15);
        today.setTextColor(onAccent);
        today.setBackground(round(accent, 24));
        today.setOnClickListener(v -> { if (offset == 0) goHome(); else { offset = 0; selDay = 0; draw(); } });
        Button bd = new Button(this);
        bd.setText("Дни рождения");
        bd.setAllCaps(false);
        bd.setTextSize(15);
        bd.setTextColor(0xFF4B1528);
        bd.setBackground(round(Birthdays.PINK, 24));
        bd.setOnClickListener(v -> startActivity(new android.content.Intent(this, BirthdaysActivity.class)));
        LinearLayout.LayoutParams t1 = new LinearLayout.LayoutParams(0, dp(46), 1);
        t1.rightMargin = dp(6);
        LinearLayout.LayoutParams t2 = new LinearLayout.LayoutParams(0, dp(46), 1);
        t2.leftMargin = dp(6);
        Ui.fit(today);
        Ui.fit(bd);
        btns.addView(today, t1);
        btns.addView(bd, t2);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(12);
        root.addView(btns, tp);

        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null) return false;
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > dp(60) && Math.abs(dx) > Math.abs(e2.getY() - e1.getY())) {
                    offset += dx < 0 ? 1 : -1;
                    selDay = 0;
                    draw();
                    return true;
                }
                return false;
            }
        });
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        if (title != null) draw();
    }

    @Override public boolean dispatchTouchEvent(MotionEvent e) {
        gd.onTouchEvent(e);
        return super.dispatchTouchEvent(e);
    }

    void goHome() {
        android.content.Intent h = new android.content.Intent(android.content.Intent.ACTION_MAIN);
        h.addCategory(android.content.Intent.CATEGORY_HOME);
        h.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
        finishAndRemoveTask();
    }

    Button arrow(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(26);
        b.setTextColor(onAccent);
        b.setPadding(0, 0, 0, dp(4));
        b.setBackground(round(accent, 26));
        return b;
    }

    void draw() {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, offset);
        title.setText(MN[c.get(Calendar.MONTH)] + " " + c.get(Calendar.YEAR));
        boolean thisMonth = c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                && c.get(Calendar.YEAR) == now.get(Calendar.YEAR);
        int todayDay = now.get(Calendar.DAY_OF_MONTH);
        int off = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int n = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        grid.removeAllViews();
        int cellW = (getResources().getDisplayMetrics().widthPixels - dp(28)) / 7;
        String[] hdr = {"Пн","Вт","Ср","Чт","Пт","Сб","Вс"};
        for (int i = 0; i < 7; i++) {
            TextView t = cell(cellW, dp(30));
            t.setText(hdr[i]);
            t.setTextSize(14);
            t.setTextColor(i >= 5 ? weekendC : 0xFF8A8A8A);
            grid.addView(t);
        }
        all = Birthdays.load(this);
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH) + 1;
        if (selDay == 0 && thisMonth) selDay = todayDay;
        int cellsN = off + n > 35 ? 42 : 35;
        for (int i = 0; i < cellsN; i++) {
            final int day = i - off + 1;
            LinearLayout cellBox = new LinearLayout(this);
            cellBox.setOrientation(LinearLayout.VERTICAL);
            cellBox.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cellW;
            lp.height = dp(48);
            cellBox.setLayoutParams(lp);
            if (day >= 1 && day <= n) {
                TextView t = new TextView(this);
                t.setGravity(Gravity.CENTER);
                t.setTextSize(19);
                t.setText(String.valueOf(day));
                boolean we = (i % 7) >= 5;
                boolean isToday = thisMonth && day == todayDay;
                if (isToday) {
                    t.setBackground(Ui.oval(todayC));
                    t.setTextColor(onToday);
                    t.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    t.setTextColor(we ? weekendC : 0xFFEEEEEE);
                    if (day == selDay) {
                        GradientDrawable g = new GradientDrawable();
                        g.setShape(GradientDrawable.OVAL);
                        g.setStroke(dp(1.5f), Birthdays.PINK);
                        t.setBackground(g);
                    }
                }
                cellBox.addView(t, new LinearLayout.LayoutParams(dp(36), dp(36)));
                android.view.View dot = new android.view.View(this);
                if (!Birthdays.on(all, y, m, day).isEmpty()) dot.setBackground(Ui.oval(Birthdays.PINK));
                LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(dp(6), dp(6));
                dl.topMargin = dp(2);
                cellBox.addView(dot, dl);
                cellBox.setOnClickListener(v -> { selDay = day; draw(); });
            }
            grid.addView(cellBox);
        }
        // карточки выбранного дня
        cards.removeAllViews();
        if (selDay > 0) {
            for (Birthdays.B b : Birthdays.on(all, y, m, selDay)) {
                LinearLayout r = Ui.personRow(this, b, null, 0);
                r.setBackground(round(0xFF1D2128, 12));
                r.setOnClickListener(v -> startActivity(new android.content.Intent(this, BirthdayCardActivity.class).putExtra("id", b.id)));
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
                rp.bottomMargin = dp(4);
                cards.addView(r, rp);
            }
        }
    }

    TextView cell(int w, int h) {
        TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = w;
        lp.height = h;
        t.setLayoutParams(lp);
        return t;
    }
}
