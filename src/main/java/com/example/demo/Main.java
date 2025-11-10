package com.example.demo;// File: MainTest.java

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // --- 1. Tentukan Path File (PENTING! GANTI INI) ---
        String PATH_ARTIS = "src/artist_db.csv";
        String PATH_FILM = "src/movies_db.csv";
        // String PATH_CONNECTIONS = "path/ke/known_connections.csv"; // <-- DIHAPUS

        System.out.println("--- Memulai Tes Rekomendasi (Pure Vibe Model) ---");

        // --- 2. Muat Data dari CSV ---
        DataService dataService = new DataService();
        List<Artist> allArtists = dataService.loadArtists(PATH_ARTIS);
        List<Movie> allMovies = dataService.loadMovies(PATH_FILM);

        if (allArtists.isEmpty() || allMovies.isEmpty()) {
            System.out.println("ERROR: Gagal memuat data. Cek path file di atas!");
            return;
        }
        System.out.println("Berhasil memuat " + allArtists.size() + " artis dan " + allMovies.size() + " film.");


        // --- 3. Simulasi Input Pengguna ---
        String[] artistNamesToTest = {"The Marias", "Radiohead", "Deftones"};

        List<Artist> selectedArtists = new ArrayList<>();
        for (Artist artist : allArtists) {
            for (String name : artistNamesToTest) {
                if (artist.getArtistName().equals(name)) {
                    selectedArtists.add(artist);
                    System.out.println("\nMenambahkan Artis: " + name);
                }
            }
        }

        // --- 4. Jalankan "Otak" ML (Inti Tes) ---
        RecommendationService recService = new RecommendationService();
        System.out.println("Menghitung rekomendasi (HANYA VIBE)...");

        // --- BARU: Menerima List<ScoredMovie> ---
        List<RecommendationService.ScoredMovie> recommendations = recService.recommendMovies(
                selectedArtists,
                allMovies);


        // --- 5. Tampilkan Hasil (VERSI DEBUG BARU) ---
        if (recommendations.isEmpty()) {
            System.out.println("Tidak ada rekomendasi yang ditemukan.");
        } else {
            System.out.println("\n--- TOP 20 REKOMENDASI (PURE VIBE) ---");

            DecimalFormat dfScore = new DecimalFormat("#.####");
            DecimalFormat dfDist = new DecimalFormat("#.##");

            // Header Tabel
            System.out.println(String.format("%-4s | %-40s | %-10s | %-10s",
                    "No.", "Nama Film", "SKOR VIBE", "JARAK"));
            System.out.println("-".repeat(70));

            int count = 1;
            for (RecommendationService.ScoredMovie sm : recommendations) {

                String scoreStr = dfScore.format(sm.score);
                String distStr = dfDist.format(sm.distance);
                String movieName = sm.movie.getMovieName();

                // Potong nama film jika terlalu panjang
                if (movieName.length() > 38) {
                    movieName = movieName.substring(0, 37) + "...";
                }

                System.out.println(String.format("%-4d | %-40s | %-10s | %-10s",
                        count, movieName, scoreStr, distStr));
                count++;
            }
        }

        System.out.println("\n--- Tes Selesai ---");
    }
}