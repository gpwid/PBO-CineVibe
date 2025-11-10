// File: HelloController.java
package com.example.demo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    // --- 1. Hubungan FXML ---
    @FXML private TextField searchField;
    @FXML private Button recommendButton;
    @FXML private TilePane resultTilePane;
    @FXML private Label loadingLabel;
    @FXML private Label recommendationLabel;
    @FXML private ComboBox<String> genreFilterDropdown;

    // --- 2. Backend Service ---
    private DataService dataService;
    private RecommendationService recService;
    private TMDBService tmdbService;

    private List<Artist> allArtists;
    private List<Movie> allMovies;

    // --- Cache untuk Filter Instan ---
    private class CachedMovieCard {
        Node cardNode; // VBox (kartu)
        Movie movie;   // Data movie (untuk genre)

        CachedMovieCard(Node cardNode, Movie movie) {
            this.cardNode = cardNode;
            this.movie = movie;
        }
    }
    private List<CachedMovieCard> recommendationCache = new ArrayList<>();

    // --- 3. Inisialisasi ---
    @FXML
    public void initialize() {
        System.out.println("Controller diinisialisasi, memuat data...");
        this.dataService = new DataService();
        this.recService = new RecommendationService();
        this.tmdbService = new TMDBService();

        // Path ke file CSV Anda (pastikan ada di root proyek)
        String PATH_ARTIS = "artist_db.csv";
        String PATH_FILM = "movies_db.csv";

        this.allArtists = dataService.loadArtists(PATH_ARTIS);
        this.allMovies = dataService.loadMovies(PATH_FILM);

        if (this.allArtists.isEmpty() || this.allMovies.isEmpty()) {
            System.err.println("GAGAL MEMUAT DATA CSV. Pastikan file ada di root proyek.");
            loadingLabel.setText("ERROR: Gagal memuat database CSV.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
        } else {
            System.out.println("Berhasil memuat " + allArtists.size() + " artis dan " + allMovies.size() + " film.");
            loadingLabel.setText("Database berhasil dimuat. Siap mencari...");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
        }

        // Mengisi 19 Genre ke Dropdown
        List<String> allGenres = List.of(
                "Semua Genre", "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
                "Drama", "Family", "Fantasy", "History", "Horror", "Music",
                "Mystery", "Romance", "Science Fiction", "TV Movie", "Thriller",
                "War", "Western"
        );
        genreFilterDropdown.setItems(FXCollections.observableArrayList(allGenres));
        genreFilterDropdown.setValue("Semua Genre");

        // Event Listener untuk Filter Instan
        genreFilterDropdown.setOnAction(event -> onGenreFilterChanged());
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

        List<RecommendationService.ScoredMovie> recommendations = recService.recommendMovies(
                List.of(selectedArtist),
                allMovies
        );

        // Persiapan UI
        resultTilePane.getChildren().clear();
        recommendationCache.clear();
        genreFilterDropdown.setValue("Semua Genre");
        loadingLabel.setText("Mencari data film untuk " + selectedArtist.getArtistName() + "...");
        loadingLabel.setVisible(true);
        recommendationLabel.setVisible(false);
        recommendButton.setDisable(true);
        genreFilterDropdown.setDisable(true);

        // Tugas Background
        Task<Void> loadMovieCardsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                for (var sm : recommendations) {
                    if (count >= 20) break;
                    if (sm.movie.getTmdbMovieId() == 0) continue;

                    var details = tmdbService.getMovieDetails(sm.movie.getTmdbMovieId());
                    var credits = tmdbService.getMovieCredits(sm.movie.getTmdbMovieId());

                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("MovieCard.fxml"));
                            Node movieCardNode = loader.load();
                            MovieCardController cardController = loader.getController();

                            // Kirim data ke kartu
                            cardController.setData(sm.movie, details, credits);

                            recommendationCache.add(new CachedMovieCard(movieCardNode, sm.movie));
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

        loadMovieCardsTask.setOnFailed(e -> {
            loadingLabel.setText("Gagal mengambil data dari TMDB. Cek koneksi internet/API Key.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
            recommendButton.setDisable(false);
            genreFilterDropdown.setDisable(false);
            loadMovieCardsTask.getException().printStackTrace();
        });

        loadMovieCardsTask.setOnSucceeded(e -> {
            loadingLabel.setVisible(false);
            recommendButton.setDisable(false);
            genreFilterDropdown.setDisable(false);

            if (recommendationCache.isEmpty()) {
                loadingLabel.setText("Tidak ada film yang cocok dengan vibe artis.");
                loadingLabel.setVisible(true);
                recommendationLabel.setVisible(false);
            } else {
                recommendationLabel.setVisible(true);
            }
        });

        new Thread(loadMovieCardsTask).start();
    }

    // --- 5. FUNGSI FILTER INSTAN ---
    private void onGenreFilterChanged() {
        if (recommendationCache.isEmpty()) return;

        String selectedGenreName = genreFilterDropdown.getValue();
        resultTilePane.getChildren().clear();

        if (selectedGenreName == null || selectedGenreName.equals("Semua Genre")) {
            for (CachedMovieCard cachedCard : recommendationCache) {
                resultTilePane.getChildren().add(cachedCard.cardNode);
            }
        } else {
            String genreKey = "genre_" + selectedGenreName.toLowerCase()
                    .replace(" ", "_")
                    .replace("-", "_");

            for (CachedMovieCard cachedCard : recommendationCache) {
                if (cachedCard.movie.getGenreValue(genreKey) == 1) {
                    resultTilePane.getChildren().add(cachedCard.cardNode);
                }
            }
        }

        if (resultTilePane.getChildren().isEmpty() && !selectedGenreName.equals("Semua Genre")) {
            loadingLabel.setText("Tidak ada film di hasil Vibe yang cocok dengan genre '" + selectedGenreName + "'.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
        } else {
            loadingLabel.setVisible(false);
            recommendationLabel.setVisible(true);
        }
    }
}