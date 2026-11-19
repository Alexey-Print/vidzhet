package ru.alexey.vidzhet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

// Уменьшает фото до квадратика 320×320 и поворачивает как надо
public class PhotoTools {
    interface Src { InputStream open() throws Exception; }

    static Bitmap load(Src src) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try (InputStream in = src.open()) { BitmapFactory.decodeStream(in, null, o); }
        int s = 1;
        while (Math.min(o.outWidth, o.outHeight) / (s * 2) >= 480) s *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = s;
        Bitmap bm;
        try (InputStream in = src.open()) { bm = BitmapFactory.decodeStream(in, null, o2); }
        if (bm == null) return null;
        int rot = 0;
        try (InputStream in = src.open()) {
            int or = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (or == ExifInterface.ORIENTATION_ROTATE_90) rot = 90;
            else if (or == ExifInterface.ORIENTATION_ROTATE_180) rot = 180;
            else if (or == ExifInterface.ORIENTATION_ROTATE_270) rot = 270;
        } catch (Exception ignored) {}
        int side = Math.min(bm.getWidth(), bm.getHeight());
        Matrix m = new Matrix();
        float sc = 320f / side;
        m.postScale(sc, sc);
        m.postRotate(rot);
        return Bitmap.createBitmap(bm, (bm.getWidth() - side) / 2, (bm.getHeight() - side) / 2, side, side, m, true);
    }

    // большое фото (до 1600 px) для кадрирования, уже повёрнутое как надо
    static Bitmap loadLarge(Src src) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try (InputStream in = src.open()) { BitmapFactory.decodeStream(in, null, o); }
        int s = 1;
        while (Math.max(o.outWidth, o.outHeight) / (s * 2) >= 1600) s *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = s;
        Bitmap bm;
        try (InputStream in = src.open()) { bm = BitmapFactory.decodeStream(in, null, o2); }
        if (bm == null) return null;
        int rot = 0;
        try (InputStream in = src.open()) {
            int or = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (or == ExifInterface.ORIENTATION_ROTATE_90) rot = 90;
            else if (or == ExifInterface.ORIENTATION_ROTATE_180) rot = 180;
            else if (or == ExifInterface.ORIENTATION_ROTATE_270) rot = 270;
        } catch (Exception ignored) {}
        if (rot == 0) return bm;
        Matrix m = new Matrix();
        m.postRotate(rot);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
    }

    static void copy(File a, File b) throws Exception {
        try (java.io.FileInputStream in = new java.io.FileInputStream(a); FileOutputStream out = new FileOutputStream(b)) {
            byte[] buf = new byte[16384];
            int r;
            while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
        }
    }

    static void save(Bitmap b, File f) throws Exception {
        try (FileOutputStream out = new FileOutputStream(f)) { b.compress(Bitmap.CompressFormat.JPEG, 88, out); }
    }
}
