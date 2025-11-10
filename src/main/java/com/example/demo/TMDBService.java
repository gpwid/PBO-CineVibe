// File: TMDBService.java
package com.example.demo; // Ganti dengan nama package Anda

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TMDBService {

    // !!! GANTI INI DENGAN API KEY ANDA !!!
    private final String API_KEY = "350260b539662daa7f6e04e316e69728";

    private final String API_BASE_URL = "https://api.themoviedb.org/3";
    private final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w300"; // w300 = lebar poster 300px

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Fungsi untuk mengambil detail dasar (judul, deskripsi, poster)
    public MovieDetails getMovieDetails(int tmdbId) {
        String url = API_BASE_URL + "/movie/" + tmdbId +
                "?api_key=" + API_KEY +
                "&language=id-ID" +
                "&append_to_response=translations";
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Ubah JSON menjadi objek Java
            MovieDetails details = gson.fromJson(response.body(), MovieDetails.class);

            if (details != null) {
                // 1. Cek jika deskripsi utama (Indonesia) kosong
                if (details.overview == null || details.overview.trim().isEmpty()) {

                    // 2. Coba cari terjemahan Bahasa Inggris (en)
                    if (details.translations != null && details.translations.translations != null) {
                        for (TranslationItem item : details.translations.translations) {
                            if ("en".equals(item.iso_639_1)) {
                                details.overview = item.data.overview; // Timpa dengan overview Bhs. Inggris
                                break; // Berhenti mencari
                            }
                        }
                    }
                }

                // 3. Set poster (logika ini tetap sama)
                if (details.posterPath != null) {
                    details.fullPosterPath = IMAGE_BASE_URL + details.posterPath;
                }
            }
            return details;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fungsi untuk mengambil kredit (aktor, sutradara)
    public MovieCredits getMovieCredits(int tmdbId) {
        String url = API_BASE_URL + "/movie/" + tmdbId + "/credits?api_key=" + API_KEY;
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Ubah JSON menjadi objek Java
            return gson.fromJson(response.body(), MovieCredits.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Kelas Internal untuk menampung data JSON ---
    // Ini adalah cetakan untuk data detail film
    public static class MovieDetails {
        @SerializedName("title")
        public String title;
        @SerializedName("overview")
        public String overview;
        @SerializedName("poster_path")
        public String posterPath;
        @SerializedName("translations")
        public TranslationsData translations;

        public String fullPosterPath; // Kita isi manual
    }

    // Ini adalah cetakan untuk data kredit
    public static class MovieCredits {
        @SerializedName("cast")
        public List<CastMember> cast;
        @SerializedName("crew")
        public List<CrewMember> crew;

        // Helper untuk mencari Sutradara
        public String getDirector() {
            if (crew == null) return "N/A";
            for (CrewMember member : crew) {
                if (member.job != null && member.job.equals("Director")) {
                    return member.name;
                }
            }
            return "N/A";
        }

        // Helper untuk mengambil 3 aktor utama
        public String getActors() {
            if (cast == null || cast.isEmpty()) return "N/A";
            StringBuilder actors = new StringBuilder();
            int count = 0;
            for (CastMember member : cast) {
                if (count >= 3) break;
                actors.append(member.name).append(", ");
                count++;
            }
            if (actors.length() > 2) {
                actors.setLength(actors.length() - 2); // Hapus koma terakhir
            }
            return actors.toString();
        }
    }

    public static class CastMember {
        @SerializedName("name")
        public String name;
    }

    public static class CrewMember {
        @SerializedName("name")
        public String name;
        @SerializedName("job")
        public String job;
    }

    public static class TranslationsData {
        @SerializedName("translations")
        public List<TranslationItem> translations;
    }

    public static class TranslationItem {
        @SerializedName("iso_639_1")
        public String iso_639_1; // Ini adalah kode bahasa, e.g., "en", "de", "fr"

        @SerializedName("data")
        public TranslationDetails data;
    }

    public static class TranslationDetails {
        @SerializedName("overview")
        public String overview; // Deskripsi dalam bahasa tersebut
    }
}