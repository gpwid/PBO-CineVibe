package com.example.demo;// File: DataService.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataService {

    // Helper untuk mengubah String ke Double (menangani error)
    private double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0; // Default ke 0 jika data kosong atau salah
        }
    }

    // Fungsi ini mengasumsikan artist_db.csv punya 9 kolom vibe
    public List<Artist> loadArtists(String csvFilePath) {
        List<Artist> artists = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            String[] headers = br.readLine().split(","); // Baca header

            // Buat pemetaan nama kolom ke indeks (agar lebih aman)
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < headers.length) continue;

                Artist artist = new Artist(
                        values[headerMap.get("artist_name")],
                        parseDouble(values[headerMap.get("vibe_energy")]),
                        parseDouble(values[headerMap.get("vibe_positivity")]),
                        parseDouble(values[headerMap.get("vibe_romance")]),
                        parseDouble(values[headerMap.get("vibe_melancholy")]),
                        parseDouble(values[headerMap.get("vibe_darkness")]),
                        parseDouble(values[headerMap.get("vibe_epic")]),
                        parseDouble(values[headerMap.get("vibe_complexity")]),
                        parseDouble(values[headerMap.get("vibe_acoustic")]),
                        parseDouble(values[headerMap.get("vibe_experimental")])
                );
                artists.add(artist);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("Error saat parsing artist CSV. Pastikan nama kolom Vibe di CSV Anda sudah benar dan ada 9.");
        }
        return artists;
    }

    // Fungsi ini memuat data film
    public List<Movie> loadMovies(String csvFilePath) {
        List<Movie> movies = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String[] headers = br.readLine().split(","); // Baca header (SANGAT PENTING)

            // Buat pemetaan nama kolom ke indeks
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < headers.length) continue;

                String movieName = values[headerMap.get("movie_name")];
                int tmdbId = (int) parseDouble(values[headerMap.get("tmdb_movie_id")]);

                Movie movie = new Movie(movieName, tmdbId);

                // Mengisi 9 Vibe
                movie.setVibe_energy(parseDouble(values[headerMap.get("vibe_energy")]));
                movie.setVibe_positivity(parseDouble(values[headerMap.get("vibe_positivity")]));
                movie.setVibe_romance(parseDouble(values[headerMap.get("vibe_romance")]));
                movie.setVibe_melancholy(parseDouble(values[headerMap.get("vibe_melancholy")]));
                movie.setVibe_darkness(parseDouble(values[headerMap.get("vibe_darkness")]));
                movie.setVibe_epic(parseDouble(values[headerMap.get("vibe_epic")]));
                movie.setVibe_complexity(parseDouble(values[headerMap.get("vibe_complexity")]));
                movie.setVibe_acoustic(parseDouble(values[headerMap.get("vibe_acoustic")]));
                movie.setVibe_experimental(parseDouble(values[headerMap.get("vibe_experimental")]));

                // Mengisi Genre (Meskipun tidak dipakai, tetap di-load)
                for (String header : headers) {
                    if (header.startsWith("genre_")) {
                        movie.setGenre(header, (int) parseDouble(values[headerMap.get(header)]));
                    }
                }
                movies.add(movie);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("Error saat parsing movie CSV. Pastikan nama kolom Vibe (9) dan Genre sudah benar.");
        }
        return movies;
    }
}