package ru.alexey.vidzhet;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

// Циферблат с цифрами 1–12 (рисуется картинкой)
public class Dial {
    static Bitmap make(int color) {
        int S = 320;
        Bitmap b = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        float cx = S / 2f, R = S / 2f - 6;
        Paint minor = new Paint(Paint.ANTI_ALIAS_FLAG);
        minor.setColor(0xFF777777);
        minor.setStrokeWidth(4);
        minor.setStrokeCap(Paint.Cap.ROUND);
        Paint major = new Paint(minor);
        major.setColor(0xFFDDDDDD);
        major.setStrokeWidth(7);
        for (int i = 0; i < 60; i++) {
            double a = Math.toRadians(i * 6);
            boolean big = i % 5 == 0;
            float r2 = big ? R - 16 : R - 9;
            c.drawLine(cx + (float) (R * Math.sin(a)), cx - (float) (R * Math.cos(a)),
                    cx + (float) (r2 * Math.sin(a)), cx - (float) (r2 * Math.cos(a)), big ? major : minor);
        }
        Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);
        t.setColor(color);
        t.setTextSize(40);
        t.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        t.setTextAlign(Paint.Align.CENTER);
        float rn = R - 44;
        for (int h = 1; h <= 12; h++) {
            double a = Math.toRadians(h * 30);
            float x = cx + (float) (rn * Math.sin(a));
            float y = cx - (float) (rn * Math.cos(a)) - (t.ascent() + t.descent()) / 2;
            c.drawText(String.valueOf(h), x, y, t);
        }
        return b;
    }
}
