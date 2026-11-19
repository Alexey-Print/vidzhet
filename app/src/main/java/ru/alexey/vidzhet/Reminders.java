package ru.alexey.vidzhet;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Напоминания о днях рождения. Включены по умолчанию: за 3 дня, накануне и в сам день, в 9:00
public class Reminders extends BroadcastReceiver {
    static final String ACT_RUN = "ru.alexey.vidzhet.BD_RUN";
    static final String ACT_SNOOZE = "ru.alexey.vidzhet.BD_SNOOZE";
    static final String CH = "birthdays";
    public static final int[] DAYS = {7, 3, 1, 0};
    public static final String[] DAY_NAMES = {"за неделю", "за 3 дня", "накануне", "в сам день"};
    public static final int[] HOURS = {8, 9, 10, 12};
    public static final String DEF_TEXT = "{имя}, с днём рождения! 🎉 Здоровья, счастья и всего самого доброго!";

    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("reminders", Context.MODE_PRIVATE); }
    public static Set<String> days(Context c) {
        return new HashSet<>(prefs(c).getStringSet("days", new HashSet<>(java.util.Arrays.asList("3", "1", "0"))));
    }
    public static int hour(Context c) { return prefs(c).getInt("hour", 9); }
    public static String text(Context c) { return prefs(c).getString("text", DEF_TEXT); }
    static String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime()); }

    // поставить будильник на ближайшее время напоминаний; если сегодня время прошло, а мы ещё не проверяли — проверить сразу
    public static void schedule(Context c) {
        Calendar n = Calendar.getInstance();
        n.set(Calendar.HOUR_OF_DAY, hour(c)); n.set(Calendar.MINUTE, 0);
        n.set(Calendar.SECOND, 0); n.set(Calendar.MILLISECOND, 0);
        boolean ranToday = today().equals(prefs(c).getString("lastRun", ""));
        if (n.getTimeInMillis() <= System.currentTimeMillis()) {
            if (!ranToday) { run(c); }
            n.add(Calendar.DAY_OF_MONTH, 1);
        }
        PendingIntent pi = PendingIntent.getBroadcast(c, 30, new Intent(c, Reminders.class).setAction(ACT_RUN),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, n.getTimeInMillis(), pi);
    }

    @Override public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (ACT_SNOOZE.equals(a)) {
            String id = i.getStringExtra("id");
            Calendar t = Calendar.getInstance();
            t.add(Calendar.DAY_OF_MONTH, 1);
            prefs(c).edit().putString("snooze_" + id, new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(t.getTime())).apply();
            c.getSystemService(NotificationManager.class).cancel(i.getIntExtra("nid", 0));
            return;
        }
        if (ACT_RUN.equals(a)) run(c);
        schedule(c);
    }

    static void run(Context c) {
        SharedPreferences p = prefs(c);
        String td = today();
        p.edit().putString("lastRun", td).apply();
        Set<String> sel = days(c);
        for (Birthdays.B b : Birthdays.load(c)) {
            int d = Birthdays.daysUntil(b);
            boolean snoozed = td.equals(p.getString("snooze_" + b.id, ""));
            if (sel.contains(String.valueOf(d)) || (snoozed && d >= 0)) post(c, b, d);
        }
    }

    static void post(Context c, Birthdays.B b, int d) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        NotificationChannel ch = new NotificationChannel(CH, "Дни рождения", NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(ch);
        int accent = Birthdays.theme(b.id)[1];
        int nid = 1000 + Math.abs(b.id.hashCode() % 100000);
        String name = b.name.trim();
        int age = Birthdays.ageNext(b);
        String title, sub;
        String wd = BirthdayCardActivity.WD[Birthdays.next(b).get(Calendar.DAY_OF_WEEK) - 1];
        if (d == 0) {
            title = "Сегодня день рождения у " + name + "!";
            sub = age > 0 ? "Исполнилось " + age : "Не забудьте поздравить";
        } else if (d == 1) {
            title = "Завтра день рождения у " + name;
            sub = (age > 0 ? "Исполнится " + age + " · " : "") + "не забудьте поздравить";
        } else {
            title = "Через " + d + " " + BirthdayCardActivity.days(d) + " день рождения у " + name;
            sub = (age > 0 ? "Исполнится " + age + " · " : "") + wd + ", " + b.day + " " + Birthdays.MONTHS_G[b.month - 1];
        }
        PendingIntent open = PendingIntent.getActivity(c, nid,
                new Intent(c, BirthdayCardActivity.class).putExtra("id", b.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder nb = new Notification.Builder(c, CH)
                .setSmallIcon(R.drawable.ic_cake_notify)
                .setColor(accent)
                .setLargeIcon(round(c, b, accent))
                .setContentTitle(title)
                .setContentText(sub)
                .setContentIntent(open)
                .setAutoCancel(true);
        if (d <= 1) {
            Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, text(c).replace("{имя}", b.name.trim()));
            Intent chooser = Intent.createChooser(send, "Поздравить").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent congr = PendingIntent.getActivity(c, nid + 1, chooser,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            nb.addAction(new Notification.Action.Builder(null, "Поздравить", congr).build());
            nb.addAction(new Notification.Action.Builder(null, "Открыть", open).build());
        } else {
            nb.addAction(new Notification.Action.Builder(null, "Открыть", open).build());
            PendingIntent sn = PendingIntent.getBroadcast(c, nid + 2,
                    new Intent(c, Reminders.class).setAction(ACT_SNOOZE).putExtra("id", b.id).putExtra("nid", nid),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            nb.addAction(new Notification.Action.Builder(null, "Напомнить завтра", sn).build());
        }
        try { nm.notify(nid, nb.build()); } catch (Exception ignored) {}
    }

    // круглое фото с цветным кольцом для уведомления
    static Icon round(Context c, Birthdays.B b, int accent) {
        int S = 192;
        Bitmap out = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(out);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(accent);
        cv.drawCircle(S / 2f, S / 2f, S / 2f, p);
        Bitmap ph = Birthdays.photo(c, b.id);
        float r = S / 2f - 10;
        if (ph != null) {
            Bitmap sc = Bitmap.createScaledBitmap(ph, (int) (2 * r), (int) (2 * r), true);
            Paint sp = new Paint(Paint.ANTI_ALIAS_FLAG);
            sp.setShader(new android.graphics.BitmapShader(sc, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP));
            cv.translate(10, 10);
            cv.drawCircle(r, r, r, sp);
        } else {
            p.setColor(0xFF1D2128);
            cv.drawCircle(S / 2f, S / 2f, r, p);
            p.setColor(accent);
            p.setTextSize(64);
            p.setTextAlign(Paint.Align.CENTER);
            p.setFakeBoldText(true);
            cv.drawText(Birthdays.initials(b), S / 2f, S / 2f - (p.ascent() + p.descent()) / 2, p);
        }
        return Icon.createWithBitmap(out);
    }
}
