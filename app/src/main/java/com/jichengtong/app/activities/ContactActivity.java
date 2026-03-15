package com.jichengtong.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.jichengtong.app.R;
import java.io.InputStream;
import java.io.OutputStream;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_right);
        toolbar.getNavigationIcon().setAutoMirrored(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_call_12348).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:12348"));
            startActivity(intent);
        });

        findViewById(R.id.btn_copy_wechat).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("微信号", "LawServicePro"));
            Toast.makeText(this, "微信号已复制，请打开微信添加好友", Toast.LENGTH_LONG).show();
        });

        ImageView qrImage = findViewById(R.id.wechat_qr_image);
        try {
            InputStream is = getAssets().open("images/wechat_qr.png");
            Bitmap bm = BitmapFactory.decodeStream(is);
            qrImage.setImageBitmap(bm);
            is.close();
        } catch (Exception ignored) {}
        qrImage.setOnLongClickListener(v -> {
            saveQrToGallery();
            return true;
        });

        findViewById(R.id.btn_save_qr).setOnClickListener(v -> saveQrToGallery());
    }

    private void saveQrToGallery() {
        try {
            ImageView qrImage = findViewById(R.id.wechat_qr_image);
            Bitmap bitmap = ((BitmapDrawable) qrImage.getDrawable()).getBitmap();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "遗产通_法律咨询微信_" + System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/遗产通");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                Toast.makeText(this, "二维码已保存到相册", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败，请截图保存", Toast.LENGTH_SHORT).show();
        }
    }
}
