package ru.alexey.vidzhet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

// Циферблаты и стрелки картинками. Размер точно как у обычного циферблата (160dp),
// иначе часы рисуют стрелки не в масштабе.
public class Dial {
    static int size(Context c) { return Math.round(160 * c.getResources().getDisplayMetrics().density); }

    static Bitmap canvas(Context c) {
        int S = size(c);
        Bitmap b = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
        b.setDensity(c.getResources().getDisplayMetrics().densityDpi);
        return b;
    }

    // style 1 — чёрточки и цифры, style 2 — крупные цифры как на наручных часах
    static Bitmap make(Context ctx, int style, int color) {
        Bitmap b = canvas(ctx);
        int S = b.getWidth();
        Canvas c = new Canvas(b);
        float k = S / 200f;                 // 200 — условные единицы, как у векторных стрелок
        float cx = S / 2f, R = 94 * k;
        Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);
        t.setColor(color);
        t.setTextAlign(Paint.Align.CENTER);
        float rn;
        if (style == 2) {
            t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            t.setTextSize(34 * k);
            rn = 76 * k;
        } else {
            t.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            Paint minor = new Paint(Paint.ANTI_ALIAS_FLAG);
            minor.setColor(0xFF777777);
            minor.setStrokeWidth(2.5f * k);
            minor.setStrokeCap(Paint.Cap.ROUND);
            Paint major = new Paint(minor);
            major.setColor(0xFFDDDDDD);
            major.setStrokeWidth(5 * k);
            for (int i = 0; i < 60; i++) {
                double a = Math.toRadians(i * 6);
                boolean big = i % 5 == 0;
                float r2 = (big ? 80 : 87) * k;
                c.drawLine(cx + (float) (R * Math.sin(a)), cx - (float) (R * Math.cos(a)),
                        cx + (float) (r2 * Math.sin(a)), cx - (float) (r2 * Math.cos(a)), big ? major : minor);
            }
            t.setTextSize(22 * k);
            rn = 64 * k;
        }
        for (int h = 1; h <= 12; h++) {
            double a = Math.toRadians(h * 30);
            float x = cx + (float) (rn * Math.sin(a));
            float y = cx - (float) (rn * Math.cos(a)) - (t.ascent() + t.descent()) / 2;
            c.drawText(String.valueOf(h), x, y, t);
        }
        return b;
    }

    // тонкая стрелка «как на часах»: длина и толщина в условных единицах (радиус 100)
    static Bitmap hand(Context ctx, float len, float width, int color) {
        Bitmap b = canvas(ctx);
        int S = b.getWidth();
        float k = S / 200f, cx = S / 2f;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStrokeWidth(width * k);
        p.setStrokeCap(Paint.Cap.ROUND);
        new Canvas(b).drawLine(cx, cx + 6 * k, cx, cx - len * k, p);
        return b;
    }
}
