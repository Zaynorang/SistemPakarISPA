package com.example.sistempakarispa;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar

public class AboutActivity extends AppCompatActivity {

    Toolbar toolbar; // Deklarasikan toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mengatur layout untuk AboutActivity dari file XML
        setContentView(R.layout.activity_about);

        // Memanggil metode untuk mengatur bilah status
        setStatusBar();

        // Menginisialisasi toolbar dari layout
        toolbar = findViewById(R.id.toolbar);
        // Mengatur toolbar sebagai action bar untuk activity ini
        setSupportActionBar(toolbar);

        // Mengatur tombol kembali di toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Menampilkan tombol back
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Menyembunyikan judul default toolbar
            // Anda bisa menambahkan judul kustom jika diperlukan, misalnya:
            // getSupportActionBar().setTitle("Tentang Aplikasi");
        }

        // Contoh: Menampilkan teks tentang aplikasi (Anda bisa mengisi ini sesuai kebutuhan)
        // TextView tvAboutContent = findViewById(R.id.tvAboutContent);
        // if (tvAboutContent != null) {
        // tvAboutContent.setText("Aplikasi Sistem Pakar Penyakit ISPA v1.0\n\n" +
        // "Dikembangkan untuk membantu pengguna dalam mendiagnosa gejala ISPA dan " +
        // "memberikan informasi dasar mengenai penanganan.\n\n" +
        // "© 2024 Nama Pengembang");
        // }
    }

    /**
     * Metode untuk mengatur tampilan bilah status (status bar).
     * Membuat bilah status transparan dan menyesuaikan ikon.
     */
    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Metode utilitas statis untuk mengatur bendera jendela (window flags).
     * Digunakan oleh setStatusBar untuk mengubah properti bilah status.
     *
     * @param activity Objek Activity saat ini.
     * @param bits     Bendera jendela yang akan diatur.
     * @param on       Nilai boolean untuk mengaktifkan atau menonaktifkan bendera.
     */
    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (on) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }

    /**
     * Metode ini dipanggil ketika sebuah item menu dipilih.
     * Digunakan untuk menangani tombol kembali di toolbar.
     *
     * @param item Item menu yang dipilih.
     * @return true jika event ditangani, false jika tidak.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Jika tombol back (home) di toolbar ditekan
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Lakukan aksi kembali
            return true;
        }
        return super.onOptionsItemSelected(item); // Panggil implementasi superclass untuk item menu lainnya
    }
}
