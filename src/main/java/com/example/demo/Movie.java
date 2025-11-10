package com.example.demo;// File: Movie.java

import java.util.HashMap;
import java.util.Map;

public class Movie {

    private String movieName;
    private int tmdbMovieId;

    // 9 Kolom Vibe (WAJIB ADA dan SAMA DENGAN ARTIST)
    private double vibe_energy;
    private double vibe_positivity;
    private double vibe_romance;
    private double vibe_melancholy;
    private double vibe_darkness;
    private double vibe_epic;
    private double vibe_complexity;
    private double vibe_acoustic;
    private double vibe_experimental;

    // 19 Kolom Genre
    // Kita simpan di Map agar lebih mudah diakses
    private Map<String, Integer> genres = new HashMap<>();

    // Constructor (dibuat simpel, Anda bisa buat lebih lengkap)
    public Movie(String movieName, int tmdbMovieId) {
        this.movieName = movieName;
        this.tmdbMovieId = tmdbMovieId;
    }

    // --- Getters (Sama seperti Artist.java) ---
    public String getMovieName() { return movieName; }
    public int getTmdbMovieId() { return tmdbMovieId; }
    public double getVibe_energy() { return vibe_energy; }
    public double getVibe_positivity() { return vibe_positivity; }
    public double getVibe_romance() { return vibe_romance; }
    public double getVibe_melancholy() { return vibe_melancholy; }
    public double getVibe_darkness() { return vibe_darkness; }
    public double getVibe_epic() { return vibe_epic; }
    public double getVibe_complexity() { return vibe_complexity; }
    public double getVibe_acoustic() { return vibe_acoustic; }
    public double getVibe_experimental() { return vibe_experimental; }

    // --- Setters (Untuk mengisi data dari CSV) ---
    public void setVibe_energy(double v) { this.vibe_energy = v; }
    public void setVibe_positivity(double v) { this.vibe_positivity = v; }
    public void setVibe_romance(double v) { this.vibe_romance = v; }
    public void setVibe_melancholy(double v) { this.vibe_melancholy = v; }
    public void setVibe_complexity(double v) { this.vibe_complexity = v; }
    public void setVibe_darkness(double v) { this.vibe_darkness = v; }
    public void setVibe_epic(double v) { this.vibe_epic = v; }
    public void setVibe_experimental(double v) { this.vibe_experimental = v; }
    public void setVibe_acoustic(double v) { this.vibe_acoustic = v; }
    // ... (buat setter untuk 9 vibe) ...

    // --- Helper untuk Genre ---
    public void setGenre(String genreName, int value) {
        // genreName harus persis seperti nama kolom, misal "genre_action"
        this.genres.put(genreName, value);
    }

    public int getGenreValue(String genreName) {
        return this.genres.getOrDefault(genreName, 0);
    }
}