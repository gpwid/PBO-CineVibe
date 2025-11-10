// File: HelloController.java
package com.example.demo;

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
    @FXML private TilePane resultTilePane;
    @FXML private HBox genreFilterBox;
    @FXML private Label loadingLabel;
    @FXML private Label recommendationLabel; // <-- BARU

    // --- 2. Backend Service ---
    private DataService dataService;
    private RecommendationService recService;
    private TMDBService tmdbService;

    private List<Artist> allArtists;
    private List<Movie> allMovies;

    // --- 3. Inisialisasi ---
    @FXML
    public void initialize() {
        System.out.println("Controller diinisialisasi, memuat data...");
        this.dataService = new DataService();
        this.recService = new RecommendationService();
        this.tmdbService = new TMDBService();

        String PATH_ARTIS = "artist_db.csv";
        String PATH_FILM = "movies_db.csv";

        this.allArtists = dataService.loadArtists(PATH_ARTIS);
        this.allMovies = dataService.loadMovies(PATH_FILM);

        if (this.allArtists.isEmpty() || this.allMovies.isEmpty()) {
            System.err.println("GAGAL MEMUAT DATA CSV.");
            loadingLabel.setText("ERROR: Gagal memuat database CSV.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false); // Sembunyikan label ini jika error
        } else {
            System.out.println("Berhasil memuat " + allArtists.size() + " artis dan " + allMovies.size() + " film.");
            loadingLabel.setText("Database berhasil dimuat. Siap mencari...");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false); // Sembunyikan di awal
        }
    }

    // --- 4. Aksi Tombol ---
    @FXML
    private void onRecommendClick() {
        String artistName = searchField.getText();
        if (artistName.isEmpty()) return;

        Artist selectedArtist = allArtists.stream()
                .filter(a -> a.getArtistName().equalsIgnoreCase(artistName.trim()))
                .findFirst()
                .orElse(null);

        if (selectedArtist == null) {
            resultTilePane.getChildren().clear();
            loadingLabel.setText("Artis '" + artistName + "' tidak ditemukan.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
            return;
        }

        List<String> activeGenreFilters = new ArrayList<>();
        for (Node node : genreFilterBox.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    activeGenreFilters.add("genre_" + cb.getId().substring(3));
                }
            }
        }

        // --- Panggil "Otak" ML (Logika Vibe - Cepat) ---
        List<RecommendationService.ScoredMovie> recommendations = recService.recommendMovies(
                List.of(selectedArtist),
                allMovies
        );

        resultTilePane.getChildren().clear();
        loadingLabel.setText("Mencari data film untuk " + selectedArtist.getArtistName() + "...");
        loadingLabel.setVisible(true);
        recommendationLabel.setVisible(false); // Sembunyikan saat loading
        recommendButton.setDisable(true);

        Task<Void> loadMovieCardsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                for (var sm : recommendations) {
                    if (count >= 20) break;

                    if (sm.movie.getTmdbMovieId() == 0) {
                        System.err.println("Skipping film: " + sm.movie.getMovieName() + " (TMDB ID is 0)");
                        continue;
                    }

                    if (!activeGenreFilters.isEmpty()) {
                        boolean match = false;
                        for (String genreFilter : activeGenreFilters) {
                            if (sm.movie.getGenreValue(genreFilter) == 1) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) continue;
                    }

                    var details = tmdbService.getMovieDetails(sm.movie.getTmdbMovieId());
                    var credits = tmdbService.getMovieCredits(sm.movie.getTmdbMovieId());

                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("MovieCard.fxml"));
                            Node movieCardNode = loader.load();
                            MovieCardController cardController = loader.getController();
                            cardController.setData(details, credits);
                            resultTilePane.getChildren().add(movieCardNode);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    count++;
                }
                return null;
            }
        };

        loadMovieCardsTask.setOnSucceeded(e -> {
            loadingLabel.setVisible(false);
            recommendButton.setDisable(false);
            if (resultTilePane.getChildren().isEmpty()) {
                loadingLabel.setText("Tidak ada film yang cocok dengan filter Anda.");
                loadingLabel.setVisible(true);
                recommendationLabel.setVisible(false);
            } else {
                recommendationLabel.setVisible(true); // Tampilkan label hasil
            }
        });

        loadMovieCardsTask.setOnFailed(e -> {
            loadingLabel.setText("Gagal mengambil data dari TMDB. Cek koneksi internet/API Key.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
            recommendButton.setDisable(false);
            loadMovieCardsTask.getException().printStackTrace();
        });

        new Thread(loadMovieCardsTask).start();
    }
}