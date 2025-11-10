// File: HelloController.java
package com.example.demo; // Ganti dengan nama package Anda

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloController {

    // --- 1. Hubungan FXML ---
    @FXML private TextField searchField;
    @FXML private Button recommendButton;
    @FXML private TilePane resultTilePane; // <-- Berubah dari ListView
    @FXML private HBox genreFilterBox;
    @FXML private Label loadingLabel;

    // --- 2. Backend Service ---
    private DataService dataService;
    private RecommendationService recService;
    private TMDBService tmdbService; // <-- Service BARU

    private List<Artist> allArtists;
    private List<Movie> allMovies;

    // --- 3. Inisialisasi ---
    @FXML
    public void initialize() {
        System.out.println("Controller diinisialisasi, memuat data...");
        this.dataService = new DataService();
        this.recService = new RecommendationService();
        this.tmdbService = new TMDBService(); // <-- Inisialisasi service BARU

        String PATH_ARTIS = "artist_db.csv";
        String PATH_FILM = "movies_db.csv";

        // Muat data HANYA SEKALI saat startup
        this.allArtists = dataService.loadArtists(PATH_ARTIS);
        this.allMovies = dataService.loadMovies(PATH_FILM);

        if (this.allArtists.isEmpty() || this.allMovies.isEmpty()) {
            System.err.println("GAGAL MEMUAT DATA CSV.");
            loadingLabel.setText("ERROR: Gagal memuat database CSV.");
            loadingLabel.setVisible(true);
        } else {
            System.out.println("Berhasil memuat " + allArtists.size() + " artis dan " + allMovies.size() + " film.");
        }
    }

    // --- 4. Aksi Tombol (LOGIKA BARU) ---
    @FXML
    private void onRecommendClick() {
        // 1. Ambil input dari UI
        String artistName = searchField.getText();
        if (artistName.isEmpty()) return;

        // 2. Cari artis
        Artist selectedArtist = allArtists.stream()
                .filter(a -> a.getArtistName().equalsIgnoreCase(artistName.trim()))
                .findFirst()
                .orElse(null);

        if (selectedArtist == null) {
            resultTilePane.getChildren().clear();
            loadingLabel.setText("Artis '" + artistName + "' tidak ditemukan.");
            loadingLabel.setVisible(true);
            return;
        }

        // 3. Ambil Filter Genre yang dicentang
        List<String> activeGenreFilters = new ArrayList<>();
        for (Node node : genreFilterBox.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    // "cb_action" -> "genre_action"
                    activeGenreFilters.add("genre_" + cb.getId().substring(3));
                }
            }
        }

        // 4. Panggil "Otak" ML (Logika Vibe - Cepat)
        List<RecommendationService.ScoredMovie> recommendations = recService.recommendMovies(
                List.of(selectedArtist),
                allMovies
        );

        // 5. Bersihkan UI dan siapkan untuk memuat
        resultTilePane.getChildren().clear();
        loadingLabel.setText("Mencari data film untuk " + selectedArtist.getArtistName() + "...");
        loadingLabel.setVisible(true);
        recommendButton.setDisable(true);

        // --- 6. JALANKAN TUGAS BERAT DI BACKGROUND (Multi-threading) ---
        // Ini SANGAT PENTING agar UI tidak "freeze" saat mengambil data API
        Task<Void> loadMovieCardsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                // Loop hasil Vibe
                for (var sm : recommendations) {
                    // Maksimal 20 kartu
                    if (count >= 20) break;

                    // --- Logika Filter Genre ---
                    if (!activeGenreFilters.isEmpty()) {
                        boolean match = false;
                        for (String genreFilter : activeGenreFilters) {
                            if (sm.movie.getGenreValue(genreFilter) == 1) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) continue; // Skip film ini jika tidak cocok filter
                    }

                    // --- Ambil Data API (Lambat) ---
                    var details = tmdbService.getMovieDetails(sm.movie.getTmdbMovieId());
                    var credits = tmdbService.getMovieCredits(sm.movie.getTmdbMovieId());

                    // --- Buat Kartu (Harus di dalam Platform.runLater) ---
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("MovieCard.fxml"));
                            Node movieCardNode = loader.load(); // Muat FXML kartu

                            // Ambil controller kartu itu
                            MovieCardController cardController = loader.getController();
                            // Isi datanya
                            cardController.setData(details, credits);

                            // Tambahkan kartu ke grid
                            resultTilePane.getChildren().add(movieCardNode);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    count++;
                } // Akhir loop

                return null;
            }
        };

        // Atur apa yang terjadi setelah Task selesai
        loadMovieCardsTask.setOnSucceeded(e -> {
            loadingLabel.setVisible(false);
            recommendButton.setDisable(false);
            if (resultTilePane.getChildren().isEmpty()) {
                loadingLabel.setText("Tidak ada film yang cocok dengan filter Anda.");
                loadingLabel.setVisible(true);
            }
        });

        // Atur jika Task gagal
        loadMovieCardsTask.setOnFailed(e -> {
            loadingLabel.setText("Gagal mengambil data dari TMDB. Cek koneksi internet/API Key.");
            loadingLabel.setVisible(true);
            recommendButton.setDisable(false);
            loadMovieCardsTask.getException().printStackTrace();
        });

        // Jalankan Task!
        new Thread(loadMovieCardsTask).start();
    }
}