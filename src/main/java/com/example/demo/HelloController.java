// File: HelloController.java
package com.example.demo;

import javafx.collections.FXCollections;
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

    // --- FXML (Tidak Berubah) ---
    @FXML private TextField searchField;
    @FXML private Button recommendButton;
    @FXML private TilePane resultTilePane;
    @FXML private Label loadingLabel;
    @FXML private Label recommendationLabel;
    @FXML private ComboBox<String> genreFilterDropdown;

    // --- Backend Service (DI-UPDATE) ---
    private DataService dataService;
    private RecommendationService recService;
    private LocalDataService localDataService; // <-- PENGGANTI TMDBService

    private List<Artist> allArtists;
    private List<Movie> allMovies;

    // Cache (Tidak Berubah)
    private class CachedMovieCard {
        Node cardNode;
        Movie movie;
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
        this.localDataService = LocalDataService.getInstance(); // <-- BARU

        // Path CSV (Tidak Berubah)
        String PATH_ARTIS = "artist_db.csv";
        String PATH_FILM = "movies_db.csv";

        this.allArtists = dataService.loadArtists(PATH_ARTIS);
        this.allMovies = dataService.loadMovies(PATH_FILM);

        if (this.allArtists.isEmpty() || this.allMovies.isEmpty()) {
            loadingLabel.setText("ERROR: Gagal memuat database CSV.");
            loadingLabel.setVisible(true);
        } else {
            System.out.println("Berhasil memuat " + allArtists.size() + " artis dan " + allMovies.size() + " film.");
            loadingLabel.setText("Database Vibe & JSON berhasil dimuat. Siap mencari...");
            loadingLabel.setVisible(true);
        }

        // Dropdown Genre (Tidak Berubah)
        List<String> allGenres = List.of(
                "Semua Genre", "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
                "Drama", "Family", "Fantasy", "History", "Horror", "Music",
                "Mystery", "Romance", "Science Fiction", "TV Movie", "Thriller",
                "War", "Western"
        );
        genreFilterDropdown.setItems(FXCollections.observableArrayList(allGenres));
        genreFilterDropdown.setValue("Semua Genre");
        genreFilterDropdown.setOnAction(event -> onGenreFilterChanged());
    }

    // --- 4. Aksi Tombol (LOGIKA BARU - TIDAK ADA TASK) ---
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

        // Panggil "Otak" ML (Logika Vibe - Cepat)
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
        // Tidak perlu disable tombol, prosesnya instan

        // --- TIDAK ADA TASK LAGI (PROSES SINKRON) ---
        int count = 0;
        for (var sm : recommendations) { // 'sm' adalah ScoredMovie
            if (count >= 20) break;
            if (sm.movie.getTmdbMovieId() == 0) continue;

            // --- BARU: Ambil data dari JSON LOKAL ---
            LocalMovieData localData = localDataService.getMovieData(String.valueOf(sm.movie.getTmdbMovieId()));

            // Jika film tidak ada di JSON lokal, lewati
            if (localData == null) {
                System.err.println("Data lokal tidak ditemukan untuk ID: " + sm.movie.getTmdbMovieId());
                continue;
            }

            // Buat kartu
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("MovieCard.fxml"));
                Node movieCardNode = loader.load();
                MovieCardController cardController = loader.getController();

                // --- DI-UPDATE: Kirim LocalMovieData ---
                cardController.setData(sm.movie, localData);

                recommendationCache.add(new CachedMovieCard(movieCardNode, sm.movie));
                resultTilePane.getChildren().add(movieCardNode);

            } catch (IOException e) {
                e.printStackTrace();
            }

            count++;
        }
        // --- AKHIR LOOP ---

        // Update UI setelah selesai
        loadingLabel.setVisible(false);
        if (recommendationCache.isEmpty()) {
            loadingLabel.setText("Tidak ada film yang cocok dengan vibe artis.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
        } else {
            recommendationLabel.setVisible(true);
        }
    }

    // --- 5. FUNGSI FILTER INSTAN (Tidak Berubah) ---
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
                    .replace("-", "_"); // untuk "sci-fi"

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