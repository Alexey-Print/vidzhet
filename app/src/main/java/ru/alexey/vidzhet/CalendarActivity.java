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
    int offset = 0;
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
        prev.setOnClickListener(v -> { offset--; draw(); });
        Button next = arrow("›");
        next.setOnClickListener(v -> { offset++; draw(); });
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

        Button today = new Button(this);
        today.setText("Сегодня");
        today.setAllCaps(false);
        today.setTextSize(16);
        today.setTextColor(onAccent);
        today.setBackground(round(accent, 24));
        today.setOnClickListener(v -> { if (offset == 0) goHome(); else { offset = 0; draw(); } });
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(150), dp(46));
        tp.gravity = Gravity.CENTER_HORIZONTAL;
        tp.topMargin = dp(18);
        root.addView(today, tp);

        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null) return false;
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > dp(60) && Math.abs(dx) > Math.abs(e2.getY() - e1.getY())) {
                    offset += dx < 0 ? 1 : -1;
                    draw();
                    return true;
                }
                return false;
            }
        });
        setContentView(root);
        draw();
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
        for (int i = 0; i < 42; i++) {
            int day = i - off + 1;
            TextView t = cell(cellW, dp(52));
            if (day >= 1 && day <= n) {
                t.setText(String.valueOf(day));
                boolean we = (i % 7) >= 5;
                if (thisMonth && day == todayDay) {
                    GradientDrawable g = new GradientDrawable();
                    g.setShape(GradientDrawable.OVAL);
                    g.setColor(todayC);
                    g.setSize(dp(42), dp(42));
                    android.graphics.drawable.InsetDrawable in = new android.graphics.drawable.InsetDrawable(g,
                            Math.max(0, (cellW - dp(42)) / 2), dp(5), Math.max(0, (cellW - dp(42)) / 2), dp(5));
                    t.setBackground(in);
                    t.setTextColor(onToday);
                    t.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    t.setTextColor(we ? weekendC : 0xFFEEEEEE);
                }
            }
            grid.addView(t);
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
