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
    private final String ACTOR_IMAGE_URL = "https://image.tmdb.org/t/p/w185"; // Untuk potret aktor
    private final String BACKDROP_IMAGE_URL = "https://image.tmdb.org/t/p/w500"; // Untuk galeri

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    // Fungsi untuk mengambil detail dasar (judul, deskripsi, poster)
    public MovieDetails getMovieDetails(int tmdbId) {
        String url = API_BASE_URL + "/movie/" + tmdbId +
                "?api_key=" + API_KEY +
                "&language=id-ID" +
                "&append_to_response=translations,release_dates";

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            MovieDetails details = gson.fromJson(response.body(), MovieDetails.class);

            if (details != null) {
                if (details.overview == null || details.overview.trim().isEmpty()) {
                    if (details.translations != null && details.translations.translations != null) {
                        for (TranslationItem item : details.translations.translations) {
                            if ("en".equals(item.iso_639_1)) {
                                details.overview = item.data.overview;
                                break;
                            }
                        }
                    }
                }
                if (details.posterPath != null) {
                    details.fullPosterPath = IMAGE_BASE_URL + details.posterPath;
                }
            }
            return details;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public MovieCredits getMovieCredits(int tmdbId) {
        String url = API_BASE_URL + "/movie/" + tmdbId + "/credits?api_key=" + API_KEY;
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            MovieCredits credits = gson.fromJson(response.body(), MovieCredits.class);

            // --- BARU: Bangun URL potret aktor ---
            if (credits != null && credits.cast != null) {
                for (CastMember member : credits.cast) {
                    if (member.profile_path != null) {
                        member.fullProfilePath = ACTOR_IMAGE_URL + member.profile_path;
                    }
                }
            }
            return credits;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // --- FUNGSI BARU: Untuk mengambil galeri backdrop ---
    public MovieImages getMovieImages(int tmdbId) {
        String url = API_BASE_URL + "/movie/" + tmdbId + "/images?api_key=" + API_KEY;
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            MovieImages images = gson.fromJson(response.body(), MovieImages.class);

            // --- BARU: Bangun URL backdrop ---
            if (images != null && images.backdrops != null) {
                for (ImageItem item : images.backdrops) {
                    if (item.file_path != null) {
                        item.fullBackdropPath = BACKDROP_IMAGE_URL + item.file_path;
                    }
                }
            }
            return images;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // --- Kelas Internal (Menampung Data JSON) ---

    // 1. MovieDetails (DI-UPDATE)
    public static class MovieDetails {
        @SerializedName("title") public String title;
        @SerializedName("overview") public String overview;
        @SerializedName("poster_path") public String posterPath;
        @SerializedName("translations") public TranslationsData translations;
        @SerializedName("vote_average") public double vote_average;
        @SerializedName("release_dates") public ReleaseDatesResponse release_dates;
        public String fullPosterPath;

        public String getCertification() {
            if (release_dates == null || release_dates.results == null) return "";

            // Cari sertifikasi untuk "US" (Amerika) karena paling standar
            for (ReleaseDateResult result : release_dates.results) {
                if ("US".equals(result.iso_3166_1)) {
                    if (result.release_dates != null) {
                        for (ReleaseDateInfo info : result.release_dates) {
                            // Ambil sertifikasi pertama yang tidak kosong
                            if (info.certification != null && !info.certification.isEmpty()) {
                                return info.certification; // Misal: "PG-13"
                            }
                        }
                    }
                }
            }
            return ""; // Tidak ditemukan
        }

    }

    // 2. MovieCredits (DI-UPDATE)
    public static class MovieCredits {
        @SerializedName("cast") public List<CastMember> cast;
        @SerializedName("crew") public List<CrewMember> crew;

        public String getDirector() {
            if (crew == null) return "N/A";
            for (CrewMember member : crew) {
                if (member.job != null && member.job.equals("Director")) {
                    return member.name;
                }
            }
            return "N/A";
        }
        // Helper ini tidak kita pakai lagi di panel detail, tapi biarkan saja
        public String getActors() {
            if (cast == null || cast.isEmpty()) return "N/A";
            StringBuilder actors = new StringBuilder();
            int count = 0;
            for (CastMember member : cast) {
                if (count >= 3) break;
                actors.append(member.name).append(", ");
                count++;
            }
            if (actors.length() > 2) { actors.setLength(actors.length() - 2); }
            return actors.toString();
        }
    }

    // 3. CastMember (DI-UPDATE)
    public static class CastMember {
        @SerializedName("name") public String name;
        @SerializedName("character") public String character; // <-- BARU
        @SerializedName("profile_path") public String profile_path; // <-- BARU
        public String fullProfilePath; // <-- BARU (Kita isi manual)
    }

    // 4. CrewMember (TETAP SAMA)
    public static class CrewMember {
        @SerializedName("name") public String name;
        @SerializedName("job") public String job;
    }

    // 5. Translation Classes (TETAP SAMA)
    public static class TranslationsData { @SerializedName("translations") public List<TranslationItem> translations; }
    public static class TranslationItem {
        @SerializedName("iso_639_1") public String iso_639_1;
        @SerializedName("data") public TranslationDetails data;
    }
    public static class TranslationDetails { @SerializedName("overview") public String overview; }

    // --- KELAS INTERNAL BARU UNTUK GALERI GAMBAR ---
    public static class MovieImages {
        @SerializedName("backdrops")
        public List<ImageItem> backdrops;
    }

    public static class ImageItem {
        @SerializedName("file_path")
        public String file_path;
        public String fullBackdropPath; // <-- BARU (Kita isi manual)
    }

    public static class ReleaseDatesResponse {
        @SerializedName("results")
        public List<ReleaseDateResult> results;
    }

    public static class ReleaseDateResult {
        @SerializedName("iso_3166_1")
        public String iso_3166_1; // Kode negara, misal "US", "ID"

        @SerializedName("release_dates")
        public List<ReleaseDateInfo> release_dates;
    }

    public static class ReleaseDateInfo {
        @SerializedName("certification")
        public String certification; // Ini yang kita cari! (misal "PG-13")
    }
}