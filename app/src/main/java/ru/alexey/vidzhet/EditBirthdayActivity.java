package ru.alexey.vidzhet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;
import java.util.List;

// Добавить / изменить день рождения
public class EditBirthdayActivity extends Activity {
    static final int REQ_CAM = 1, REQ_GAL = 2, REQ_CROP = 3;
    boolean newSrc;
    Birthdays.B b;
    boolean isNew;
    Bitmap newPhoto;
    boolean removePhoto;
    ImageView photoView;
    TextView photoHint;
    EditText name, day, year;
    Spinner month;

    int dp(float v) { return Ui.dp(this, v); }

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        getWindow().setStatusBarColor(0xFF111317);
        getWindow().setNavigationBarColor(0xFF111317);
        String id = getIntent().getStringExtra("id");
        b = id == null ? null : Birthdays.find(this, id);
        isNew = b == null;
        if (isNew) b = new Birthdays.B();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111317);
        root.setPadding(dp(14), dp(36), dp(14), dp(14));

        TextView t = new TextView(this);
        t.setText(isNew ? "Новый день рождения" : "День рождения");
        t.setTextColor(Color.WHITE);
        t.setTextSize(20);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(t);

        // фото
        FrameLayout ph = new FrameLayout(this);
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setStroke(dp(1.5f), 0xFF666666, dp(4), dp(3));
        ph.setBackground(ring);
        photoView = new ImageView(this);
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View v, Outline o) { o.setOval(0, 0, v.getWidth(), v.getHeight()); }
        });
        photoView.setClipToOutline(true);
        ph.addView(photoView, new FrameLayout.LayoutParams(-1, -1));
        photoHint = new TextView(this);
        photoHint.setText("фото");
        photoHint.setTextColor(0xFFAAAAAA);
        photoHint.setTextSize(13);
        photoHint.setGravity(Gravity.CENTER);
        ph.addView(photoHint, new FrameLayout.LayoutParams(-1, -1));
        ph.setOnClickListener(v -> askPhoto());
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(84), dp(84));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = dp(10);
        root.addView(ph, pp);
        Bitmap old = isNew ? null : Birthdays.photo(this, b.id);
        showPhoto(old);

        root.addView(label("Имя"));
        name = field("Например, Марина Герина", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setText(b.name);
        root.addView(name, new LinearLayout.LayoutParams(-1, dp(46)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout c1 = col(), c2 = col(), c3 = col();
        c1.addView(label("Число"));
        day = field("25", InputType.TYPE_CLASS_NUMBER);
        if (b.day > 0) day.setText(String.valueOf(b.day));
        c1.addView(day, new LinearLayout.LayoutParams(-1, dp(46)));
        c2.addView(label("Месяц"));
        month = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Birthdays.MONTHS) {
            @Override public View getView(int pos, View cv, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, cv, parent);
                tv.setTextColor(Color.WHITE);
                tv.setSingleLine(true);
                tv.setPadding(dp(10), 0, dp(4), 0);
                tv.setAutoSizeTextTypeUniformWithConfiguration(10, 16, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
                return tv;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month.setAdapter(ad);
        month.setBackground(Ui.round(this, 0xFF1D2128, 10));
        month.setSelection(b.month > 0 ? b.month - 1 : Calendar.getInstance().get(Calendar.MONTH));
        c2.addView(month, new LinearLayout.LayoutParams(-1, dp(46)));
        c3.addView(label("Год"));
        year = field("1998", InputType.TYPE_CLASS_NUMBER);
        if (b.year > 0) year.setText(String.valueOf(b.year));
        c3.addView(year, new LinearLayout.LayoutParams(-1, dp(46)));
        LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, -2, 0.8f);
        LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(0, -2, 2f);
        l2.leftMargin = dp(6); l2.rightMargin = dp(6);
        LinearLayout.LayoutParams l3 = new LinearLayout.LayoutParams(0, -2, 1.2f);
        row.addView(c1, l1); row.addView(c2, l2); row.addView(c3, l3);
        root.addView(row);

        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        if (!isNew) {
            Button del = button("Удалить", 0xFF5A2328, 0xFFFFB4B4);
            del.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setMessage("Удалить «" + b.name + "»?")
                    .setPositiveButton("Удалить", (d, w) -> delete())
                    .setNegativeButton("Отмена", null).show());
            btns.addView(del, weight(1, 0, 6));
        }
        Button cancel = button("Отмена", 0xFF3A3F47, Color.WHITE);
        cancel.setOnClickListener(v -> finish());
        btns.addView(cancel, weight(1, isNew ? 0 : 6, 6));
        Button save = button("Сохранить", 0xFF66D17A, 0xFF08210E);
        save.setOnClickListener(v -> save());
        btns.addView(save, weight(1, 6, 0));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = dp(18);
        root.addView(btns, bp);

        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.setBackgroundColor(0xFF111317);
        sv.addView(root);
        setContentView(sv);
    }

    LinearLayout.LayoutParams weight(float w, int l, int r) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(46), w);
        p.leftMargin = dp(l); p.rightMargin = dp(r);
        return p;
    }

    LinearLayout col() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        return c;
    }

    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(0xFFAAAAAA);
        t.setTextSize(12);
        t.setPadding(0, dp(10), 0, dp(3));
        t.setSingleLine(true);
        return t;
    }

    EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(0xFF666666);
        e.setTextColor(Color.WHITE);
        e.setTextSize(16);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setBackground(Ui.round(this, 0xFF1D2128, 10));
        e.setPadding(dp(10), 0, dp(10), 0);
        return e;
    }

    Button button(String s, int bg, int fg) {
        Button x = new Button(this);
        x.setText(s);
        x.setAllCaps(false);
        x.setTextSize(15);
        x.setTextColor(fg);
        x.setBackground(Ui.round(this, bg, 22));
        Ui.fit(x);
        return x;
    }

    void showPhoto(Bitmap bm) {
        photoView.setImageBitmap(bm);
        photoHint.setVisibility(bm == null ? View.VISIBLE : View.GONE);
    }

    void askPhoto() {
        boolean has = photoHint.getVisibility() == View.GONE;
        File cs = new File(getCacheDir(), "crop_src.jpg");
        boolean canCrop = has && (newSrc ? cs.exists() : Birthdays.photoSrcFile(this, b.id).exists());
        java.util.List<String> items = new java.util.ArrayList<>();
        items.add("Камера");
        items.add("Галерея");
        if (canCrop) items.add("Увеличить / сдвинуть");
        if (has) items.add("Убрать фото");
        new AlertDialog.Builder(this).setTitle("Фото").setItems(items.toArray(new String[0]), (d, w) -> {
            String it = items.get(w);
            if (it.equals("Камера")) camera();
            else if (it.equals("Галерея")) gallery();
            else if (it.startsWith("Увеличить")) {
                try {
                    if (!newSrc) PhotoTools.copy(Birthdays.photoSrcFile(this, b.id), cs);
                    startActivityForResult(new Intent(this, CropActivity.class), REQ_CROP);
                } catch (Exception e) { Toast.makeText(this, "Не получилось", Toast.LENGTH_SHORT).show(); }
            }
            else { newPhoto = null; newSrc = false; removePhoto = true; showPhoto(null); }
        }).show();
    }

    // снимок пишется прямо во внутреннюю папку программы — в галерею не попадает
    void camera() {
        try {
            new File(getCacheDir(), "cam.jpg").delete();
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, FilesProvider.CAM);
            i.setClipData(ClipData.newRawUri("cam", FilesProvider.CAM));
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(i, REQ_CAM);
        } catch (Exception e) {
            Toast.makeText(this, "Камера не открылась", Toast.LENGTH_SHORT).show();
        }
    }

    void gallery() {
        try {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, REQ_GAL);
        } catch (Exception e) {
            Toast.makeText(this, "Галерея не открылась", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK) return;
        try {
            if (req == REQ_CROP) {
                File out = new File(getCacheDir(), "crop_out.jpg");
                Bitmap bm = android.graphics.BitmapFactory.decodeFile(out.getAbsolutePath());
                if (bm != null) { newPhoto = bm; removePhoto = false; showPhoto(bm); }
                return;
            }
            Bitmap big = null;
            if (req == REQ_CAM) {
                File f = new File(getCacheDir(), "cam.jpg");
                big = PhotoTools.loadLarge(() -> new java.io.FileInputStream(f));
                f.delete();
            } else if (req == REQ_GAL && data != null && data.getData() != null) {
                android.net.Uri u = data.getData();
                big = PhotoTools.loadLarge(() -> getContentResolver().openInputStream(u));
            }
            if (big != null) {
                // сразу открываем кадрирование
                PhotoTools.save(big, new File(getCacheDir(), "crop_src.jpg"));
                newSrc = true;
                startActivityForResult(new Intent(this, CropActivity.class), REQ_CROP);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Не получилось взять фото", Toast.LENGTH_SHORT).show();
        }
    }

    void save() {
        String n = name.getText().toString().trim();
        if (n.isEmpty()) { Toast.makeText(this, "Впишите имя", Toast.LENGTH_SHORT).show(); return; }
        int d, m = month.getSelectedItemPosition() + 1, y = 0;
        try { d = Integer.parseInt(day.getText().toString().trim()); } catch (Exception e) { d = 0; }
        int maxDay = (m == 2) ? 29 : (m == 4 || m == 6 || m == 9 || m == 11) ? 30 : 31;
        if (d < 1 || d > maxDay) { Toast.makeText(this, "Число от 1 до " + maxDay, Toast.LENGTH_SHORT).show(); return; }
        String ys = year.getText().toString().trim();
        if (!ys.isEmpty()) {
            try { y = Integer.parseInt(ys); } catch (Exception e) { y = -1; }
            int now = Calendar.getInstance().get(Calendar.YEAR);
            if (y < 1900 || y > now) { Toast.makeText(this, "Год от 1900 до " + now + " или пусто", Toast.LENGTH_SHORT).show(); return; }
        }
        b.name = n; b.day = d; b.month = m; b.year = y;
        try {
            if (newPhoto != null) {
                PhotoTools.save(newPhoto, Birthdays.photoFile(this, b.id));
                if (newSrc) PhotoTools.copy(new File(getCacheDir(), "crop_src.jpg"), Birthdays.photoSrcFile(this, b.id));
            } else if (removePhoto) {
                Birthdays.photoFile(this, b.id).delete();
                Birthdays.photoSrcFile(this, b.id).delete();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Фото не сохранилось", Toast.LENGTH_SHORT).show();
        }
        List<Birthdays.B> all = Birthdays.load(this);
        boolean found = false;
        for (int i = 0; i < all.size(); i++) if (all.get(i).id.equals(b.id)) { all.set(i, b); found = true; }
        if (!found) all.add(b);
        Birthdays.save(this, all);
        ClockWidget.updateAll(this);
        Reminders.schedule(this);
        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
        finish();
    }

    void delete() {
        List<Birthdays.B> all = Birthdays.load(this);
        for (int i = all.size() - 1; i >= 0; i--) if (all.get(i).id.equals(b.id)) all.remove(i);
        Birthdays.save(this, all);
        Birthdays.photoFile(this, b.id).delete();
        Birthdays.photoSrcFile(this, b.id).delete();
        ClockWidget.updateAll(this);
        finish();
    }
}
