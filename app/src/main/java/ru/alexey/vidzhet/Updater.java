package ru.alexey.vidzhet;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// Проверка обновлений: на GitHub лежит version.txt с номером последней сборки
public class Updater {
    static final String BASE = "https://github.com/Alexey-Print/vidzhet/releases/download/latest/";
    static final String CH = "updates";

    static long current(Context c) {
        try { return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).getLongVersionCode(); }
        catch (Exception e) { return 0; }
    }

    static boolean hasUpdate(Context c) {
        return c.getSharedPreferences(ClockWidget.P, Context.MODE_PRIVATE).getLong("remoteVer", 0) > current(c);
    }

    static HttpURLConnection open(String url) throws Exception {
        HttpURLConnection h = (HttpURLConnection) new URL(url).openConnection();
        h.setConnectTimeout(15000);
        h.setReadTimeout(30000);
        h.setInstanceFollowRedirects(true);
        return h;
    }

    // вызывать НЕ из главного потока
    static boolean check(Context c, boolean notify) {
        try {
            HttpURLConnection h = open(BASE + "version.txt");
            String s;
            try (InputStream in = h.getInputStream()) {
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[256];
                int r;
                while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
                s = bo.toString("UTF-8").trim();
            }
            long remote = Long.parseLong(s);
            android.content.SharedPreferences p = c.getSharedPreferences(ClockWidget.P, Context.MODE_PRIVATE);
            long before = p.getLong("remoteVer", 0);
            p.edit().putLong("remoteVer", remote).apply();
            boolean upd = remote > current(c);
            if (upd && notify && remote != before) notifyUpdate(c);
            return upd;
        } catch (Exception e) {
            return false;
        }
    }

    static void channels(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CH, "Обновления", NotificationManager.IMPORTANCE_DEFAULT));
    }

    static void notifyUpdate(Context c) {
        channels(c);
        PendingIntent pi = PendingIntent.getActivity(c, 20,
                new Intent(c, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(c, CH)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Часы-календарь: есть обновление")
                .setContentText("Нажмите, чтобы обновить")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        try { c.getSystemService(NotificationManager.class).notify(1, n); } catch (Exception ignored) {}
    }

    // скачать и открыть установщик
    static void install(Activity a) {
        if (!a.getPackageManager().canRequestPackageInstalls()) {
            Toast.makeText(a, "Разрешите установку для «Часы-календарь» и нажмите «Обновить» ещё раз", Toast.LENGTH_LONG).show();
            a.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + a.getPackageName())));
            return;
        }
        Toast.makeText(a, "Скачиваю обновление…", Toast.LENGTH_SHORT).show();
        Handler ui = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                File f = new File(a.getCacheDir(), "update.apk");
                HttpURLConnection h = open(BASE + "vidzhet.apk");
                try (InputStream in = h.getInputStream(); FileOutputStream out = new FileOutputStream(f)) {
                    byte[] buf = new byte[16384];
                    int r;
                    while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
                }
                ui.post(() -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(FilesProvider.UPDATE, "application/vnd.android.package-archive");
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    a.startActivity(i);
                });
            } catch (Exception e) {
                ui.post(() -> Toast.makeText(a, "Не удалось скачать. Проверьте интернет", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
