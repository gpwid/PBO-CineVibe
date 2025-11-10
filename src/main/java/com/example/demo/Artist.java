package com.example.demo;

// File: Artist.java
public class Artist {
    // Sesuaikan tipe data jika perlu (String, int, double)
    private String artistName;
    private int artistId; // Opsional

    // 9 Kolom Vibe (WAJIB ADA)
    private double vibe_energy;
    private double vibe_positivity;
    private double vibe_romance;
    private double vibe_melancholy;
    private double vibe_darkness;
    private double vibe_epic;
    private double vibe_complexity;
    private double vibe_acoustic;
    private double vibe_experimental;

    // Buat Constructor
    public Artist(String artistName, double vibe_energy, double vibe_positivity, double vibe_romance,
                  double vibe_melancholy, double vibe_darkness, double vibe_epic,
                  double vibe_complexity, double vibe_acoustic, double vibe_experimental) {
        this.artistName = artistName;
        this.vibe_energy = vibe_energy;
        this.vibe_positivity = vibe_positivity;
        this.vibe_romance = vibe_romance;
        this.vibe_melancholy = vibe_melancholy;
        this.vibe_darkness = vibe_darkness;
        this.vibe_epic = vibe_epic;
        this.vibe_complexity = vibe_complexity;
        this.vibe_acoustic = vibe_acoustic;
        this.vibe_experimental = vibe_experimental;
    }

    // Buat "Getter" untuk semua field di atas
    // (Klik kanan di IDE -> Generate -> Getters)

    public String getArtistName() { return artistName; }
    public double getVibe_energy() { return vibe_energy; }
    public double getVibe_positivity() { return vibe_positivity; }
    public double getVibe_romance() { return vibe_romance; }
    public double getVibe_melancholy() { return vibe_melancholy; }
    public double getVibe_darkness() { return vibe_darkness; }
    public double getVibe_epic() { return vibe_epic; }
    public double getVibe_complexity() { return vibe_complexity; }
    public double getVibe_acoustic() { return vibe_acoustic; }
    public double getVibe_experimental() { return vibe_experimental; }
}