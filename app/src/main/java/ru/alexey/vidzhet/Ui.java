package ru.alexey.vidzhet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

// Общие мелочи для экранов
public class Ui {
    static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    static GradientDrawable round(Context c, int color, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, r));
        return g;
    }

    // надпись на кнопке всегда в одну строку: если не влезает — шрифт сам уменьшается
    static void fit(android.widget.Button b) {
        b.setMaxLines(1);
        b.setPadding(dp(b.getContext(), 6), 0, dp(b.getContext(), 6), 0);
        b.setAutoSizeTextTypeUniformWithConfiguration(10, 16, 1, TypedValue.COMPLEX_UNIT_SP);
    }

    static GradientDrawable oval(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    // круглая аватарка: фото или инициалы
    static View avatar(Context c, Birthdays.B b, int sizeDp) {
        FrameLayout f = new FrameLayout(c);
        Bitmap bm = Birthdays.photo(c, b.id);
        if (bm != null) {
            ImageView iv = new ImageView(c);
            iv.setImageBitmap(bm);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View v, Outline o) { o.setOval(0, 0, v.getWidth(), v.getHeight()); }
            });
            iv.setClipToOutline(true);
            f.addView(iv, new FrameLayout.LayoutParams(-1, -1));
        } else {
            TextView t = new TextView(c);
            t.setText(Birthdays.initials(b));
            t.setTextColor(Birthdays.PINK);
            t.setTextSize(sizeDp > 40 ? 16 : 12);
            t.setGravity(Gravity.CENTER);
            t.setBackground(oval(0xFF3A2330));
            f.addView(t, new FrameLayout.LayoutParams(-1, -1));
        }
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return f;
    }

    // строка: аватарка, имя, подпись, справа текст
    static LinearLayout personRow(Context c, Birthdays.B b, String right, int rightColor) {
        LinearLayout r = new LinearLayout(c);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(c, 6), dp(c, 6), dp(c, 6), dp(c, 6));
        r.addView(avatar(c, b, 38));
        LinearLayout tx = new LinearLayout(c);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(dp(c, 10), 0, dp(c, 6), 0);
        TextView n = new TextView(c);
        n.setText(b.name);
        n.setTextColor(Color.WHITE);
        n.setTextSize(15);
        n.setTypeface(null, android.graphics.Typeface.BOLD);
        n.setSingleLine(true);
        n.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView i = new TextView(c);
        i.setText(Birthdays.info(b));
        i.setTextColor(0xFFAAAAAA);
        i.setTextSize(12);
        tx.addView(n);
        tx.addView(i);
        r.addView(tx, new LinearLayout.LayoutParams(0, -2, 1));
        if (right != null) {
            TextView d = new TextView(c);
            d.setText(right);
            d.setTextColor(rightColor);
            d.setTextSize(13);
            r.addView(d);
        }
        return r;
    }
}
