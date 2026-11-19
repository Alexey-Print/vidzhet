package ru.alexey.vidzhet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Calendar;

public class ClockWidget extends AppWidgetProvider {
    static final String ACTION_MIDNIGHT = "ru.alexey.vidzhet.MIDNIGHT";
    static final int[] CELLS = new int[42];
    static final int[] ROWS = {R.id.row0, R.id.row1, R.id.row2, R.id.row3, R.id.row4, R.id.row5};
    static final int[] HDR = {R.id.h0, R.id.h1, R.id.h2, R.id.h3, R.id.h4, R.id.h5, R.id.h6};
    static {
        int[] c = {R.id.c0,R.id.c1,R.id.c2,R.id.c3,R.id.c4,R.id.c5,R.id.c6,R.id.c7,R.id.c8,R.id.c9,
            R.id.c10,R.id.c11,R.id.c12,R.id.c13,R.id.c14,R.id.c15,R.id.c16,R.id.c17,R.id.c18,R.id.c19,
            R.id.c20,R.id.c21,R.id.c22,R.id.c23,R.id.c24,R.id.c25,R.id.c26,R.id.c27,R.id.c28,R.id.c29,
            R.id.c30,R.id.c31,R.id.c32,R.id.c33,R.id.c34,R.id.c35,R.id.c36,R.id.c37,R.id.c38,R.id.c39,
            R.id.c40,R.id.c41};
        System.arraycopy(c, 0, CELLS, 0, 42);
    }

    // настройки
    static final String P = "nastroiki";
    static int time(SharedPreferences p)    { return p.getInt("time", 4); }
    static int today(SharedPreferences p)   { return p.getInt("today", 4); }
    static int weekend(SharedPreferences p) { return p.getInt("weekend", 1); }
    static int second(SharedPreferences p)  { return p.getInt("second", 0); }
    static boolean showSec(SharedPreferences p) { return p.getBoolean("showSec", true); }
    static int date(SharedPreferences p)    { return p.getInt("date", 10); }
    static int wday(SharedPreferences p)    { return p.getInt("wday", 11); }
    static boolean nums(SharedPreferences p) { return p.getBoolean("nums", false); }
    static int alpha(SharedPreferences p)   { return p.getInt("alpha", 100); }

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        updateAll(c);
        scheduleMidnight(c);
    }
    @Override public void onEnabled(Context c) { scheduleMidnight(c); }

    @Override public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (ACTION_MIDNIGHT.equals(a) || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a) || Intent.ACTION_DATE_CHANGED.equals(a)
                || Intent.ACTION_LOCALE_CHANGED.equals(a) || Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            updateAll(c);
            scheduleMidnight(c);
            if (ACTION_MIDNIGHT.equals(a) || Intent.ACTION_BOOT_COMPLETED.equals(a)) {
                final PendingResult pr = goAsync();
                final Context app = c.getApplicationContext();
                new Thread(() -> { try { Updater.check(app, true); } finally { pr.finish(); } }).start();
            }
        }
    }

    static void scheduleMidnight(Context c) {
        Calendar n = Calendar.getInstance();
        n.add(Calendar.DAY_OF_MONTH, 1);
        n.set(Calendar.HOUR_OF_DAY, 0); n.set(Calendar.MINUTE, 0);
        n.set(Calendar.SECOND, 5); n.set(Calendar.MILLISECOND, 0);
        Intent i = new Intent(c, ClockWidget.class).setAction(ACTION_MIDNIGHT);
        PendingIntent pi = PendingIntent.getBroadcast(c, 1, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.set(AlarmManager.RTC, n.getTimeInMillis(), pi);
    }

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, ClockWidget.class));
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    static RemoteViews build(Context c) {
        SharedPreferences p = c.getSharedPreferences(P, Context.MODE_PRIVATE);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);

        v.setInt(R.id.bg, "setImageAlpha", Math.round(alpha(p) * 255 / 100f));
        v.setTextColor(R.id.time, Pal.COLORS[time(p)]);
        int dc = Pal.COLORS[date(p)];
        v.setTextColor(R.id.date, dc);
        v.setTextColor(R.id.wday, Pal.COLORS[wday(p)]);
        Icon dial = nums(p) ? Icon.createWithBitmap(Dial.make(0xFFFFFFFF))
                : Icon.createWithResource(c, R.drawable.dial);
        v.setIcon(R.id.analog, "setDial", dial);
        v.setIcon(R.id.analog_nosec, "setDial", dial);
        boolean sec = showSec(p);
        v.setViewVisibility(R.id.analog, sec ? View.VISIBLE : View.GONE);
        v.setViewVisibility(R.id.analog_nosec, sec ? View.GONE : View.VISIBLE);
        if (sec) v.setIcon(R.id.analog, "setSecondHand", Icon.createWithResource(c, Pal.SECOND[second(p)]));

        int we = Pal.COLORS[weekend(p)];
        v.setTextColor(HDR[5], we);
        v.setTextColor(HDR[6], we);

        Calendar cal = Calendar.getInstance();
        int todayDay = cal.get(Calendar.DAY_OF_MONTH);
        int curY = cal.get(Calendar.YEAR), curM = cal.get(Calendar.MONTH) + 1;
        java.util.List<Birthdays.B> allB = Birthdays.load(c);
        java.util.List<Birthdays.B> todayB = Birthdays.on(allB, curY, curM, todayDay);
        if (todayB.isEmpty()) {
            v.setViewVisibility(R.id.wday, View.VISIBLE);
            v.setViewVisibility(R.id.bday, View.GONE);
        } else {
            String t = Birthdays.shortName(todayB.get(0));
            if (todayB.size() > 1) t += " +" + (todayB.size() - 1);
            v.setTextViewText(R.id.bday, t);
            v.setViewVisibility(R.id.wday, View.GONE);
            v.setViewVisibility(R.id.bday, View.VISIBLE);
        }
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int off = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int n = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int tc = Pal.COLORS[today(p)];
        int onToday = isLight(tc) ? 0xFF101010 : 0xFFFFFFFF;
        for (int i = 0; i < 42; i++) {
            int day = i - off + 1;
            int id = CELLS[i];
            if (day < 1 || day > n) {
                v.setTextViewText(id, "");
                v.setInt(id, "setBackgroundResource", 0);
                continue;
            }
            v.setTextViewText(id, String.valueOf(day));
            v.setTextViewCompoundDrawables(id, 0, 0, 0,
                    Birthdays.on(allB, curY, curM, day).isEmpty() ? R.drawable.bdot_none : R.drawable.bdot);
            boolean wkend = (i % 7) >= 5;
            if (day == todayDay) {
                v.setInt(id, "setBackgroundResource", Pal.TODAY[today(p)]);
                v.setTextColor(id, onToday);
            } else {
                v.setInt(id, "setBackgroundResource", 0);
                v.setTextColor(id, wkend ? we : 0xFFEEEEEE);
            }
        }
        v.setViewVisibility(ROWS[5], off + n > 35 ? View.VISIBLE : View.GONE);

        // нажатия
        PendingIntent settings = PendingIntent.getActivity(c, 10,
                new Intent(c, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.time, settings);

        PendingIntent alarms = PendingIntent.getActivity(c, 11,
                new Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.analog, alarms);
        v.setOnClickPendingIntent(R.id.analog_nosec, alarms);

        PendingIntent calPi = PendingIntent.getActivity(c, 12,
                new Intent(c, CalendarActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.cal, calPi);
        return v;
    }

    static boolean isLight(int col) {
        int r = (col >> 16) & 255, g = (col >> 8) & 255, b = col & 255;
        return (r * 299 + g * 587 + b * 114) / 1000 > 150;
    }
}
