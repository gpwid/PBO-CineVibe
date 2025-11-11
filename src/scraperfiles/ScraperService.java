// File: ScraperService.java (Versi Anti-Duplikat)
package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList; // Pastikan import ini ada
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScraperService {

    // --- KONFIGURASI ---
    private static final String JSON_OUTPUT_PATH = "src/main/resources/com/example/demo/local_database.json";
    private static final String IMAGE_OUTPUT_PATH = "src/main/resources/com/example/demo/";
    private static final String MOVIE_DB_INPUT_PATH = "movies_db.csv"; // CSV Anda di root proyek
    // ------------------

    private DataService dataService;
    private TMDBService tmdbService;
    private HttpClient httpClient;
    private Gson gson;

    // Database di-load di sini
    private Map<String, LocalMovieData> localDatabase = new HashMap<>();

    public ScraperService() {
        this.dataService = new DataService();
        this.tmdbService = new TMDBService(); // Pastikan API Key di TMDBService sudah benar
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // --- Kelas Internal untuk JSON (POJO) ---
    private static class LocalMovieData {
        String title;
        String overview;
        double vote_average;
        String certification;
        String director;
        List<LocalActorData> actors;
        List<String> backdrops;
        String poster_path;
    }
    private static class LocalActorData {
        String name;
        String character;
        String profile_path;
    }

    // --- BARU: Fungsi untuk memuat database yang sudah ada ---
    private void loadExistingDatabase() {
        try (InputStream stream = Files.newInputStream(Paths.get(JSON_OUTPUT_PATH)); // Baca dari path output
             InputStreamReader reader = new InputStreamReader(stream)) {

            // Muat data yang ada ke dalam Map
            localDatabase = gson.fromJson(reader, new TypeToken<Map<String, LocalMovieData>>(){}.getType());
            if (localDatabase == null) {
                localDatabase = new HashMap<>();
            }
            System.out.println("Berhasil memuat " + localDatabase.size() + " film dari database yang ada.");

        } catch (Exception e) {
            System.out.println("Database lokal (" + JSON_OUTPUT_PATH + ") tidak ditemukan. Membuat baru.");
            localDatabase = new HashMap<>(); // Mulai baru jika file tidak ada
        }
    }

    // --- FUNGSI UTAMA SKRIP ---
    public void runScraper() {
        System.out.println("--- MEMULAI SCRAPER ---");

        // 1. Buat folder jika belum ada
        try {
            Files.createDirectories(Paths.get(IMAGE_OUTPUT_PATH + "images/posters"));
            Files.createDirectories(Paths.get(IMAGE_OUTPUT_PATH + "images/actors"));
            Files.createDirectories(Paths.get(IMAGE_OUTPUT_PATH + "images/backdrops"));
        } catch (IOException e) {
            System.err.println("Gagal membuat folder images!");
            e.printStackTrace();
            return;
        }

        // 2. Muat database JSON yang sudah ada (jika ada)
        loadExistingDatabase();

        // 3. Baca daftar film dari CSV
        List<Movie> moviesToScrape = dataService.loadMovies(MOVIE_DB_INPUT_PATH);
        if (moviesToScrape.isEmpty()) {
            System.err.println("Gagal memuat " + MOVIE_DB_INPUT_PATH);
            return;
        }
        System.out.println("Total film di CSV: " + moviesToScrape.size() + ". Memeriksa pembaruan...");

        // 4. Mulai loop scraping
        int count = 1;
        boolean newDataScraped = false; // Flag untuk tahu jika ada data baru

        for (Movie movie : moviesToScrape) {
            int tmdbId = movie.getTmdbMovieId();
            if (tmdbId == 0) continue;

            String movieIdStr = String.valueOf(tmdbId);
            System.out.println("\n(" + count + "/" + moviesToScrape.size() + ") Memeriksa film: " + movie.getMovieName() + " (ID: " + tmdbId + ")");

            // --- INI LOGIKA ANTI-DUPLIKAT (JSON) ---
            if (localDatabase.containsKey(movieIdStr)) {
                System.out.println("  -> Data JSON sudah ada. Melewati panggilan API.");
                count++;
                continue; // Lewati film ini, lanjut ke berikutnya
            }
            // --- AKHIR LOGIKA ---

            // Jika lolos, berarti ini data baru
            newDataScraped = true;
            System.out.println("  -> Data baru, memanggil API...");

            // 5. Panggil 3 API
            TMDBService.MovieDetails details = tmdbService.getMovieDetails(tmdbId);
            TMDBService.MovieCredits credits = tmdbService.getMovieCredits(tmdbId);
            TMDBService.MovieImages images = tmdbService.getMovieImages(tmdbId);

            if (details == null || credits == null || images == null) {
                System.err.println("  -> Gagal mengambil data API. Melewatkan film ini.");
                count++;
                continue;
            }

            // 6. Buat entri database baru
            LocalMovieData localData = new LocalMovieData();
            localData.title = details.title;
            localData.overview = details.overview;
            localData.vote_average = details.vote_average;
            localData.certification = details.getCertification();
            localData.director = credits.getDirector();

            // 7. Download Poster
            if (details.fullPosterPath != null) {
                String localPosterPath = "images/posters/" + tmdbId + ".jpg";
                downloadImage(details.fullPosterPath, IMAGE_OUTPUT_PATH + localPosterPath);
                localData.poster_path = localPosterPath;
            }

            // 8. Download Aktor
            localData.actors = new ArrayList<>();
            if (credits.cast != null) {
                int actorCount = 0;
                for (TMDBService.CastMember member : credits.cast) {
                    if (actorCount >= 10) break;
                    if (member.fullProfilePath != null && member.profile_path != null) {
                        LocalActorData actor = new LocalActorData();
                        actor.name = member.name;
                        actor.character = member.character;

                        String[] pathParts = member.profile_path.split("/");
                        String filename = pathParts[pathParts.length - 1];
                        String localActorPath = "images/actors/" + filename;

                        downloadImage(member.fullProfilePath, IMAGE_OUTPUT_PATH + localActorPath);
                        actor.profile_path = localActorPath;
                        localData.actors.add(actor);
                        actorCount++;
                    }
                }
            }

            // 9. Download Backdrop
            localData.backdrops = new ArrayList<>();
            if (images.backdrops != null) {
                int backdropCount = 0;
                for (TMDBService.ImageItem backdrop : images.backdrops) {
                    if (backdropCount >= 6) break;
                    if (backdrop.fullBackdropPath != null && backdrop.file_path != null) {
                        String[] pathParts = backdrop.file_path.split("/");
                        String filename = pathParts[pathParts.length - 1];
                        String localBackdropPath = "images/backdrops/" + filename;

                        downloadImage(backdrop.fullBackdropPath, IMAGE_OUTPUT_PATH + localBackdropPath);
                        localData.backdrops.add(localBackdropPath);
                        backdropCount++;
                    }
                }
            }

            // 10. Simpan ke database Map
            localDatabase.put(movieIdStr, localData);
            count++;

            // Jeda 500ms agar tidak di-ban oleh API TMDB
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }

        // 11. Tulis Map ke file JSON (HANYA jika ada data baru)
        if (newDataScraped) {
            System.out.println("\nMenyimpan data baru ke " + JSON_OUTPUT_PATH + "...");
            try (FileWriter writer = new FileWriter(JSON_OUTPUT_PATH)) {
                gson.toJson(localDatabase, writer);
                System.out.println("Database JSON lokal berhasil disimpan/diperbarui.");
            } catch (IOException e) {
                System.err.println("Gagal menyimpan file JSON!");
                e.printStackTrace();
            }
        } else {
            System.out.println("\nTidak ada data baru yang di-scrape. File JSON tidak diubah.");
        }

        System.out.println("--- SELESAI! ---");
    }

    // --- Helper untuk Download Gambar (Versi Anti-Duplikat) ---
    private void downloadImage(String url, String localPath) {
        try {
            // --- INI LOGIKA ANTI-DUPLIKAT (GAMBAR) ---
            Path outputPath = Paths.get(localPath);
            if (Files.exists(outputPath)) {
                System.out.println("  -> Gambar sudah ada: " + localPath);
                return; // JANGAN DOWNLOAD LAGI
            }
            // --- AKHIR LOGIKA ---

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                Files.copy(response.body(), outputPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("  -> Download sukses: " + localPath);
            } else {
                System.err.println("  -> Gagal download (status " + response.statusCode() + "): " + url);
            }
        } catch (Exception e) {
            System.err.println("  -> Error download gambar: " + e.getMessage());
        }
    }

    // --- CARA MENJALANKAN SKRIP INI ---
    public static void main(String[] args) {
        ScraperService scraper = new ScraperService();
        scraper.runScraper();
    }
}