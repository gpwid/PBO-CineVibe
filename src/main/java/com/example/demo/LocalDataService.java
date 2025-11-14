// File: LocalDataService.java
package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// Ini menggunakan Pola Desain "Singleton"
public class LocalDataService {

    private static LocalDataService instance;
    private Map<String, LocalMovieData> localDatabase;
    private Gson gson;

    // Constructor dibuat private
    private LocalDataService() {
        gson = new Gson();
        localDatabase = new HashMap<>();
    }

    // Cara publik untuk mendapatkan satu-satunya instance dari service ini
    public static LocalDataService getInstance() {
        if (instance == null) {
            instance = new LocalDataService();
        }
        return instance;
    }

    // Fungsi ini harus dipanggil SATU KALI saat aplikasi startup
    public void loadData() {
        System.out.println("Memuat database JSON lokal...");
        // Pastikan file JSON ada di src/main/resources/com/example/demo/
        try (InputStream stream = Application.class.getResourceAsStream("local_database.json");
             InputStreamReader reader = new InputStreamReader(stream)) {

            localDatabase = gson.fromJson(reader, new TypeToken<Map<String, LocalMovieData>>(){}.getType());
            System.out.println("Database JSON lokal berhasil dimuat. " + localDatabase.size() + " film ditemukan.");

        } catch (Exception e) {
            System.err.println("GAGAL TOTAL memuat local_database.json!");
            e.printStackTrace();
        }
    }

    // Fungsi untuk mengambil satu data film dari database yang sudah di-load
    public LocalMovieData getMovieData(String tmdbId) {
        return localDatabase.get(tmdbId);
    }

    // Helper untuk mengubah path relatif (misal "images/posters/123.jpg")
    // menjadi path absolut yang bisa dibaca JavaFX
    public String getLocalImagePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        try {
            // Pastikan path gambar Anda (misal "images/...") ada di resources/com/example/demo/
            return Application.class.getResource(relativePath).toExternalForm();
        } catch (Exception e) {
            System.err.println("Gambar lokal tidak ditemukan: " + relativePath);
            return null;
        }
    }
}