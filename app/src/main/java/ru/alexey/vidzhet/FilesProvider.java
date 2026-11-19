package ru.alexey.vidzhet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

// Отдаёт файлы программы другим приложениям: сам APK (поделиться),
// снимок камеры (пишется сразу внутрь программы) и скачанное обновление
public class FilesProvider extends ContentProvider {
    static final String AUTH = "ru.alexey.vidzhet.files";
    static final String NAME = "Часы-календарь.apk";
    public static final Uri APK = Uri.parse("content://" + AUTH + "/apk/" + Uri.encode(NAME));
    public static final Uri CAM = Uri.parse("content://" + AUTH + "/cam/cam.jpg");
    public static final Uri UPDATE = Uri.parse("content://" + AUTH + "/update/" + Uri.encode(NAME));

    File file(Uri u) {
        String k = u.getPathSegments().isEmpty() ? "" : u.getPathSegments().get(0);
        if ("cam".equals(k)) return new File(getContext().getCacheDir(), "cam.jpg");
        if ("update".equals(k)) return new File(getContext().getCacheDir(), "update.apk");
        return new File(getContext().getApplicationInfo().sourceDir);
    }

    @Override public boolean onCreate() { return true; }

    @Override public String getType(Uri uri) {
        return uri.getPathSegments().size() > 0 && "cam".equals(uri.getPathSegments().get(0))
                ? "image/jpeg" : "application/vnd.android.package-archive";
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = file(uri);
        boolean cam = "cam".equals(uri.getPathSegments().get(0));
        return ParcelFileDescriptor.open(f, cam ? ParcelFileDescriptor.parseMode(mode.contains("w") ? "rwt" : "r")
                : ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] proj, String sel, String[] args, String sort) {
        File f = file(uri);
        MatrixCursor c = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
        c.addRow(new Object[]{uri.getLastPathSegment(), f.length()});
        return c;
    }

    @Override public Uri insert(Uri uri, ContentValues v) { return null; }
    @Override public int delete(Uri uri, String s, String[] a) { return 0; }
    @Override public int update(Uri uri, ContentValues v, String s, String[] a) { return 0; }
}
