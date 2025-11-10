package com.example.demo;// File: RecommendationService.java

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendationService {

    // Kelas ini sekarang public agar bisa dibaca oleh MainTest
    // (Bisa juga tetap private, tapi class ScoredMovie harus public)
    // Mari kita buat ScoredMovie jadi public

    private class UserProfile {
        double energy, positivity, romance, melancholy, darkness,
                epic, complexity, acoustic, experimental;

        // Fungsi toString() BARU untuk debugging
        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat("#.##");
            return "--- Profil Vibe Artis (Rata-rata) ---" +
                    "\n  Energy: " + df.format(energy) +
                    "\n  Positivity: " + df.format(positivity) +
                    "\n  Romance: " + df.format(romance) +
                    "\n  Melancholy: " + df.format(melancholy) +
                    "\n  Darkness: " + df.format(darkness) +
                    "\n  Epic: " + df.format(epic) +
                    "\n  Complexity: " + df.format(complexity) +
                    "\n  Acoustic: " + df.format(acoustic) +
                    "\n  Experimental: " + df.format(experimental) +
                    "\n" + "-".repeat(40);
        }
    }

    // Kelas ini di-update untuk menyimpan 'distance'
    // Kita buat public agar MainTest bisa mengakses field-nya
    public class ScoredMovie implements Comparable<ScoredMovie> {
        public Movie movie; // Dibuat public
        public double score; // Dibuat public
        public double distance; // <-- FIELD BARU

        ScoredMovie(Movie movie, double score, double distance) { // <-- CONSTRUCTOR DI-UPDATE
            this.movie = movie;
            this.score = score;
            this.distance = distance; // <-- BARU
        }

        @Override
        public int compareTo(ScoredMovie other) {
            return Double.compare(other.score, this.score);
        }
    }

    // --- FUNGSI UTAMA DI-UPDATE ---
    // Sekarang mengembalikan List<ScoredMovie>
    public List<ScoredMovie> recommendMovies(List<Artist> selectedArtists,
                                             List<Movie> allMovies) {

        // 1. Hitung Profil DNA Pengguna
        UserProfile userProfile = calculateUserProfile(selectedArtists);

        // --- BARU: Cetak profil vibe rata-rata artis untuk debug ---
        System.out.println(userProfile);

        List<ScoredMovie> scoredMovies = new ArrayList<>();

        for (Movie movie : allMovies) {

            // 3. Hitung Jarak Vibe
            double distance = calculateManhattanDistance(movie, userProfile);

            // 4. Hitung VibeScore
            double vibeScore = 1.0 / (1.0 + distance);

            // 5. Hitung Skor Final
            double finalScore = vibeScore;

            // --- BARU: Simpan 'distance' juga ---
            scoredMovies.add(new ScoredMovie(movie, finalScore, distance));
        }

        // 6. Urutkan film
        Collections.sort(scoredMovies);

        // 7. Ambil 20 film teratas
        List<ScoredMovie> topMovies = new ArrayList<>();
        int count = 0;
        for (ScoredMovie sm : scoredMovies) {
            if (count >= 20) break; // <-- Dinaikkan jadi 20
            topMovies.add(sm);
            count++;
        }

        // --- BARU: Kembalikan List<ScoredMovie> ---
        return topMovies;
    }

    // --- Helper Functions (Tidak Berubah) ---

    private UserProfile calculateUserProfile(List<Artist> selectedArtists) {
        UserProfile profile = new UserProfile();
        if (selectedArtists.isEmpty()) return profile;

        for (Artist artist : selectedArtists) {
            profile.energy += artist.getVibe_energy();
            profile.positivity += artist.getVibe_positivity();
            profile.romance += artist.getVibe_romance();
            profile.melancholy += artist.getVibe_melancholy();
            profile.darkness += artist.getVibe_darkness();
            profile.epic += artist.getVibe_epic();
            profile.complexity += artist.getVibe_complexity();
            profile.acoustic += artist.getVibe_acoustic();
            profile.experimental += artist.getVibe_experimental();
        }

        int n = selectedArtists.size();
        profile.energy /= n;
        profile.positivity /= n;
        profile.romance /= n;
        profile.melancholy /= n;
        profile.darkness /= n;
        profile.epic /= n;
        profile.complexity /= n;
        profile.acoustic /= n;
        profile.experimental /= n;

        return profile;
    }

    private double calculateManhattanDistance(Movie movie, UserProfile profile) {
        double sumOfAbsoluteDifferences = 0;

        // --- Aturan Bobot Dinamis ---
        // Jika nilai vibe di profil artis "ekstrem" (1, 2, 9, atau 10),
        // kita anggap vibe itu "penting" dan beri bobot 3x lipat.
        final double BOBOT_NORMAL = 1.0;
        final double BOBOT_EKSTREM = 3.0; // <-- Tweak angka ini (misal 2.0 atau 4.0)

        // Cek Vibe Energy
        double weight_energy = (profile.energy <= 2 || profile.energy >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_energy() - profile.energy) * weight_energy;

        // Cek Vibe Positivity
        double weight_positivity = (profile.positivity <= 2 || profile.positivity >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_positivity() - profile.positivity) * weight_positivity;

        // Cek Vibe Romance
        double weight_romance = (profile.romance <= 2 || profile.romance >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_romance() - profile.romance) * weight_romance;

        // Cek Vibe Melancholy
        double weight_melancholy = (profile.melancholy <= 2 || profile.melancholy >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_melancholy() - profile.melancholy) * weight_melancholy;

        // Cek Vibe Darkness
        double weight_darkness = (profile.darkness <= 2 || profile.darkness >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_darkness() - profile.darkness) * weight_darkness;

        // Cek Vibe Epic
        double weight_epic = (profile.epic <= 2 || profile.epic >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_epic() - profile.epic) * weight_epic;

        // Cek Vibe Complexity
        double weight_complexity = (profile.complexity <= 2 || profile.complexity >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_complexity() - profile.complexity) * weight_complexity;

        // Cek Vibe Acoustic
        double weight_acoustic = (profile.acoustic <= 2 || profile.acoustic >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_acoustic() - profile.acoustic) * weight_acoustic;

        // Cek Vibe Experimental
        double weight_experimental = (profile.experimental <= 2 || profile.experimental >= 9) ? BOBOT_EKSTREM : BOBOT_NORMAL;
        sumOfAbsoluteDifferences += Math.abs(movie.getVibe_experimental() - profile.experimental) * weight_experimental;

        return sumOfAbsoluteDifferences;
    }
}