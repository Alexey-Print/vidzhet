package ru.alexey.vidzhet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;

// Кадрирование фото в кружок: двумя пальцами — увеличить, одним — сдвинуть
public class CropActivity extends Activity {
    CropView cv;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        Bitmap src = BitmapFactory.decodeFile(new File(getCacheDir(), "crop_src.jpg").getAbsolutePath());
        if (src == null) { finish(); return; }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(Ui.dp(this, 12), Ui.dp(this, 36), Ui.dp(this, 12), Ui.dp(this, 12));
        TextView t = new TextView(this);
        t.setText("Двумя пальцами — увеличить, одним — передвинуть");
        t.setTextColor(0xFFAAAAAA);
        t.setTextSize(13);
        t.setGravity(android.view.Gravity.CENTER);
        root.addView(t);
        cv = new CropView(this, src);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, 0, 1);
        cp.topMargin = Ui.dp(this, 8);
        root.addView(cv, cp);
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        Button cancel = new Button(this);
        cancel.setText("Отмена");
        cancel.setAllCaps(false);
        cancel.setTextColor(Color.WHITE);
        cancel.setBackground(Ui.round(this, 0xFF3A3F47, 22));
        Ui.fit(cancel);
        cancel.setOnClickListener(v -> finish());
        Button ok = new Button(this);
        ok.setText("Готово");
        ok.setAllCaps(false);
        ok.setTextColor(0xFF08210E);
        ok.setBackground(Ui.round(this, 0xFF66D17A, 22));
        Ui.fit(ok);
        ok.setOnClickListener(v -> {
            try {
                PhotoTools.save(cv.crop(), new File(getCacheDir(), "crop_out.jpg"));
                setResult(RESULT_OK);
            } catch (Exception ignored) {}
            finish();
        });
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        l1.rightMargin = Ui.dp(this, 6);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        l2.leftMargin = Ui.dp(this, 6);
        btns.addView(cancel, l1);
        btns.addView(ok, l2);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = Ui.dp(this, 10);
        root.addView(btns, bp);
        setContentView(root);
    }

    static class CropView extends View {
        final Bitmap src;
        final Matrix m = new Matrix();
        final Paint bmp = new Paint(Paint.FILTER_BITMAP_FLAG);
        final Paint dim = new Paint();
        final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        final ScaleGestureDetector sg;
        float scale, minScale, tx, ty, lastX, lastY;
        boolean ready;
        RectF circle = new RectF();

        CropView(Context c, Bitmap src) {
            super(c);
            this.src = src;
            dim.setColor(0xB0000000);
            ring.setColor(Color.WHITE);
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(Ui.dp(c, 2));
            sg = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector d) {
                    float ns = Math.max(minScale, Math.min(minScale * 8, scale * d.getScaleFactor()));
                    float f = ns / scale;
                    tx = d.getFocusX() - (d.getFocusX() - tx) * f;
                    ty = d.getFocusY() - (d.getFocusY() - ty) * f;
                    scale = ns;
                    clamp();
                    invalidate();
                    return true;
                }
            });
        }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            float d = Math.min(w, h) * 0.86f;
            circle.set((w - d) / 2, (h - d) / 2, (w + d) / 2, (h + d) / 2);
            minScale = d / Math.min(src.getWidth(), src.getHeight());
            scale = minScale;
            tx = w / 2f - src.getWidth() * scale / 2;
            ty = h / 2f - src.getHeight() * scale / 2;
            ready = true;
        }

        void clamp() {
            float bw = src.getWidth() * scale, bh = src.getHeight() * scale;
            tx = Math.min(circle.left, Math.max(circle.right - bw, tx));
            ty = Math.min(circle.top, Math.max(circle.bottom - bh, ty));
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            sg.onTouchEvent(e);
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = e.getX(); lastY = e.getY();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_POINTER_DOWN:
                    lastX = e.getX(0); lastY = e.getY(0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!sg.isInProgress() && e.getPointerCount() == 1) {
                        tx += e.getX() - lastX;
                        ty += e.getY() - lastY;
                        clamp();
                        invalidate();
                    }
                    lastX = e.getX(); lastY = e.getY();
                    break;
            }
            return true;
        }

        @Override protected void onDraw(Canvas c) {
            if (!ready) return;
            m.reset();
            m.postScale(scale, scale);
            m.postTranslate(tx, ty);
            c.drawBitmap(src, m, bmp);
            Path p = new Path();
            p.setFillType(Path.FillType.EVEN_ODD);
            p.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
            p.addOval(circle, Path.Direction.CW);
            c.drawPath(p, dim);
            c.drawOval(circle, ring);
        }

        Bitmap crop() {
            int S = 400;
            Bitmap out = Bitmap.createBitmap(S, S, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(out);
            c.drawColor(Color.BLACK);
            Matrix mm = new Matrix();
            mm.postScale(scale, scale);
            mm.postTranslate(tx - circle.left, ty - circle.top);
            float k = S / circle.width();
            mm.postScale(k, k);
            c.drawBitmap(src, mm, bmp);
            return out;
        }
    }
}
