package com.example.sistempakarispa;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log; // Import Log untuk debugging
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sistempakarispa.R;
import com.example.sistempakarispa.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HasilDiagnosaActivity extends AppCompatActivity {

    private static final String TAG = "HasilDiagnosaActivity"; // TAG untuk Logcat

    SQLiteDatabase sqLiteDatabase;
    Toolbar toolbar;
    TextView tvGejala, tvNamaPenyakit;
    MaterialButton btnDiagnosaUlang, btnDaftarPenyakit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hasil_diagnosa);

        setStatusBar();

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        // Pastikan database terbuka, jika tidak, log pesan kesalahan.
        if (databaseHelper.openDatabase()) {
            sqLiteDatabase = databaseHelper.getReadableDatabase();
            Log.d(TAG, "Database opened successfully.");
        } else {
            Log.e(TAG, "Failed to open database! Application might not function correctly.");
            // Handle error: mungkin tampilkan pesan ke pengguna atau keluar dari activity
            // return; // Jangan langsung return, coba tetap lanjutkan untuk melihat error lain
        }


        toolbar = findViewById(R.id.toolbar);
        tvGejala = findViewById(R.id.tvGejala);
        tvNamaPenyakit = findViewById(R.id.tvNamaPenyakit);
        btnDiagnosaUlang = findViewById(R.id.btnDiagnosaUlang);
        btnDaftarPenyakit = findViewById(R.id.btnDaftarPenyakit);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        String str_hasil = getIntent().getStringExtra("HASIL");
        String[] gejala_terpilih = new String[0];
        if (str_hasil != null) {
            gejala_terpilih = str_hasil.split("#");
            Log.d(TAG, "Gejala terpilih string: " + str_hasil);
            for (String s : gejala_terpilih) {
                Log.d(TAG, "Gejala terpilih array item: '" + s + "'"); // Tambahkan tanda kutip untuk melihat spasi
            }
        } else {
            Log.w(TAG, "str_hasil (Gejala Terpilih) is null!");
        }

        // Langkah 1: Ubah array nama gejala yang dipilih menjadi List kode gejala
        ArrayList<String> kodeGejalaTerpilih = new ArrayList<>();
        if (gejala_terpilih.length > 0) {
            for (String namaGejala : gejala_terpilih) {
                // Hapus spasi di awal/akhir nama gejala jika ada
                String trimmedNamaGejala = namaGejala.trim();
                String queryGetKode = "SELECT kode_gejala FROM gejala WHERE nama_gejala = ?";
                Cursor cursorKode = null;
                try {
                    cursorKode = sqLiteDatabase.rawQuery(queryGetKode, new String[]{trimmedNamaGejala});
                    if (cursorKode.moveToFirst()) {
                        String kodeGejala = cursorKode.getString(0);
                        kodeGejalaTerpilih.add(kodeGejala);
                        Log.d(TAG, "Nama Gejala: '" + trimmedNamaGejala + "' -> Kode Gejala: " + kodeGejala);
                    } else {
                        Log.w(TAG, "Tidak ditemukan kode gejala untuk nama: '" + trimmedNamaGejala + "'");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error querying gejala table for '" + trimmedNamaGejala + "': " + e.getMessage());
                } finally {
                    if (cursorKode != null) {
                        cursorKode.close();
                    }
                }
            }
        }
        Log.d(TAG, "Jumlah kode gejala terpilih: " + kodeGejalaTerpilih.size());
        if (kodeGejalaTerpilih.isEmpty()) {
            Log.e(TAG, "List kodeGejalaTerpilih kosong. Perhitungan CF akan 0.");
        }


        // Langkah 2: Hitung CF untuk setiap penyakit berdasarkan gejala yang dipilih
        HashMap<String, Double> mapHasil = new HashMap<>();
        String query_penyakit = "SELECT kode_penyakit FROM penyakit ORDER BY kode_penyakit";
        Cursor cursor_penyakit = null;
        try {
            cursor_penyakit = sqLiteDatabase.rawQuery(query_penyakit, null);
            Log.d(TAG, "Total penyakit ditemukan: " + cursor_penyakit.getCount());

            // Loop untuk setiap penyakit (P01, P02, P03)
            while (cursor_penyakit.moveToNext()) {
                String kodePenyakit = cursor_penyakit.getString(0);
                double cf_gabungan = 0.0;
                Log.d(TAG, "Mulai perhitungan untuk penyakit: " + kodePenyakit);

                // Loop untuk setiap kode gejala yang dipilih oleh pengguna
                for (String kodeGejala : kodeGejalaTerpilih) {
                    // Cari aturan (rule) yang cocok untuk penyakit dan gejala saat ini
                    String query_rule = "SELECT nilai_cf FROM rule WHERE kode_penyakit = ? AND kode_gejala = ?";
                    Cursor cursor_rule = null;
                    try {
                        cursor_rule = sqLiteDatabase.rawQuery(query_rule, new String[]{kodePenyakit, kodeGejala});
                        Log.d(TAG, "  Query rule: P=" + kodePenyakit + ", G=" + kodeGejala + " -> Result Count: " + cursor_rule.getCount());

                        // Jika aturan ditemukan, gabungkan nilai CF-nya
                        if (cursor_rule.moveToFirst()) {
                            double cf_rule = cursor_rule.getDouble(0);
                            Log.d(TAG, "  Rule ditemukan: P=" + kodePenyakit + ", G=" + kodeGejala + ", CF_rule=" + cf_rule);

                            // Rumus penggabungan Certainty Factor (CF) yang benar
                            double prev_cf_gabungan = cf_gabungan; // Simpan nilai sebelumnya untuk log
                            cf_gabungan = cf_gabungan + cf_rule * (1 - cf_gabungan);
                            Log.d(TAG, "  CF Gabungan diperbarui: " + prev_cf_gabungan + " + " + cf_rule + " * (1 - " + prev_cf_gabungan + ") = " + cf_gabungan);
                        } else {
                            Log.d(TAG, "  Tidak ada rule ditemukan untuk P=" + kodePenyakit + ", G=" + kodeGejala);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error querying rule table for P=" + kodePenyakit + ", G=" + kodeGejala + ": " + e.getMessage());
                    } finally {
                        if (cursor_rule != null) {
                            cursor_rule.close();
                        }
                    }
                }
                // Simpan hasil akhir (sudah dalam bentuk persentase)
                mapHasil.put(kodePenyakit, cf_gabungan * 100);
                Log.d(TAG, "Hasil akhir CF untuk " + kodePenyakit + ": " + (cf_gabungan * 100) + "%");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying penyakit table: " + e.getMessage());
        } finally {
            if (cursor_penyakit != null) {
                cursor_penyakit.close();
            }
        }


        // Debugging: Lihat semua hasil perhitungan sebelum diurutkan
        Log.d(TAG, "Semua hasil perhitungan CF_akhir:");
        for (Map.Entry<String, Double> entry : mapHasil.entrySet()) {
            Log.d(TAG, "  Penyakit: " + entry.getKey() + ", CF: " + entry.getValue() + "%");
        }


        StringBuffer output_gejala_terpilih = new StringBuffer();
        int no = 1;
        for (String s_gejala_terpilih : gejala_terpilih) {
            output_gejala_terpilih.append(no++)
                    .append(". ")
                    .append(s_gejala_terpilih)
                    .append("\n");
        }

        tvGejala.setText(output_gejala_terpilih);

        Map<String, Double> sortedHasil = sortByValue(mapHasil);

        // Pastikan sortedHasil tidak kosong sebelum mencoba mendapatkan entry pertama
        if (sortedHasil.isEmpty()) {
            tvNamaPenyakit.setText("Tidak ada diagnosa yang cocok (0%)");
            Log.e(TAG, "sortedHasil is empty! No matching diagnosis found.");
        } else {
            Map.Entry<String, Double> entry = sortedHasil.entrySet().iterator().next();
            String kode_penyakit = entry.getKey();
            double hasil_cf = entry.getValue();
            int persentase = (int) hasil_cf;

            String query_penyakit_hasil = "SELECT nama_penyakit FROM penyakit where kode_penyakit='" + kode_penyakit + "'";
            Cursor cursor_hasil = null;
            try {
                cursor_hasil = sqLiteDatabase.rawQuery(query_penyakit_hasil, null);

                if (cursor_hasil.moveToFirst()) { // Pastikan kursor memiliki hasil
                    tvNamaPenyakit.setText(cursor_hasil.getString(0) + " " + persentase + "%");
                    Log.d(TAG, "Menampilkan hasil: " + cursor_hasil.getString(0) + " " + persentase + "%");
                } else {
                    tvNamaPenyakit.setText("Penyakit tidak ditemukan untuk kode: " + kode_penyakit + " (0%)");
                    Log.e(TAG, "Nama penyakit tidak ditemukan untuk kode: " + kode_penyakit);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying penyakit for result: " + e.getMessage());
            } finally {
                if (cursor_hasil != null) {
                    cursor_hasil.close();
                }
            }
        }


        btnDiagnosaUlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Kembali ke DiagnosaActivity
            }
        });

        btnDaftarPenyakit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HasilDiagnosaActivity.this,
                        DaftarPenyakitActivity.class);
                startActivity(intent);
            }
        });
    }

    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        // Implementasi sudah benar
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

        // Diurutkan dari nilai tertinggi ke terendah
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private void setStatusBar() {
        // Implementasi sudah benar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        // Implementasi sudah benar
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (on) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implementasi sudah benar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
