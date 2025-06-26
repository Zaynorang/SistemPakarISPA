package com.example.sistempakarispa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Durasi tampilan splash screen dalam milidetik (misalnya, 3 detik)
    private static final int SPLASH_DURATION = 3000; // 3000ms = 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pastikan splash screen mengambil seluruh layar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Mengatur layout untuk splash screen
        setContentView(R.layout.activity_splash);

        // Menggunakan Handler untuk menunda navigasi ke MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Buat Intent untuk memulai MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                // Tutup SplashActivity agar pengguna tidak bisa kembali ke sana dengan tombol back
                finish();
            }
        }, SPLASH_DURATION); // Durasi penundaan
    }
}