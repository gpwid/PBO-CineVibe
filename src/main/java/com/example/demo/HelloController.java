package com.example.demo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox; // <-- IMPORT BARU
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.collections.FXCollections; // <-- IMPORT BARU

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloController {

    // --- 1. Hubungan FXML ---
    @FXML private TextField searchField;
    @FXML private Button recommendButton;
    @FXML private TilePane resultTilePane;
    @FXML private Label loadingLabel;
    @FXML private Label recommendationLabel;

    // --- GANTI HBox DENGAN ComboBox ---
    @FXML private ComboBox<String> genreFilterDropdown;

    // --- 2. Backend Service ---
    private DataService dataService;
    private RecommendationService recService;
    private TMDBService tmdbService;

    private List<Artist> allArtists;
    private List<Movie> allMovies;

    // --- BARU: CACHE UNTUK FILTER INSTAN ---
    // Class kecil untuk menyimpan kartu dan data genrenya
    private class CachedMovieCard {
        Node cardNode; // VBox (kartu)
        Movie movie;   // Data movie (untuk genre)

        CachedMovieCard(Node cardNode, Movie movie) {
            this.cardNode = cardNode;
            this.movie = movie;
        }
    }
    // Daftar cache
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

        // --- BARU: Mengisi 19 Genre ke Dropdown ---
        List<String> allGenres = List.of(
                "Semua Genre", // Pilihan default
                "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
                "Drama", "Family", "Fantasy", "History", "Horror", "Music",
                "Mystery", "Romance", "Science Fiction", "TV Movie", "Thriller",
                "War", "Western"
        );
        genreFilterDropdown.setItems(FXCollections.observableArrayList(allGenres));
        genreFilterDropdown.setValue("Semua Genre"); // Set default

        // --- BARU: Event Listener untuk Filter Instan ---
        // Ini akan memanggil fungsi onGenreFilterChanged() setiap kali
        // nilai dropdown berubah.
        genreFilterDropdown.setOnAction(event -> onGenreFilterChanged());
    }

    // --- 4. Aksi Tombol (LOGIKA BARU) ---
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

        // --- Panggil "Otak" ML (Logika Vibe - Cepat) ---
        List<RecommendationService.ScoredMovie> recommendations = recService.recommendMovies(
                List.of(selectedArtist),
                allMovies
        );

        // --- Persiapan UI ---
        resultTilePane.getChildren().clear();
        recommendationCache.clear(); // <-- BARU: Kosongkan cache
        genreFilterDropdown.setValue("Semua Genre"); // <-- BARU: Reset filter
        loadingLabel.setText("Mencari data film untuk " + selectedArtist.getArtistName() + "...");
        loadingLabel.setVisible(true);
        recommendationLabel.setVisible(false);
        recommendButton.setDisable(true);
        genreFilterDropdown.setDisable(true); // Nonaktifkan dropdown saat loading

        // --- JALANKAN TUGAS BERAT DI BACKGROUND ---
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

                    // --- PENTING: KITA TIDAK FILTER GENRE DI SINI LAGI ---
                    // Kita ambil semua 20 film vibe teratas

                    var details = tmdbService.getMovieDetails(sm.movie.getTmdbMovieId());
                    var credits = tmdbService.getMovieCredits(sm.movie.getTmdbMovieId());

                    // Buat kartu dan simpan di cache
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("MovieCard.fxml"));
                            Node movieCardNode = loader.load();
                            MovieCardController cardController = loader.getController();
                            cardController.setData(details, credits);

                            // --- LOGIKA BARU ---
                            // 1. Tambahkan ke cache
                            recommendationCache.add(new CachedMovieCard(movieCardNode, sm.movie));
                            // 2. Tambahkan ke tampilan (karena defaultnya "Semua Genre")
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

        // ... (setOnFailed tetap sama) ...
        loadMovieCardsTask.setOnFailed(e -> {
            loadingLabel.setText("Gagal mengambil data dari TMDB. Cek koneksi internet/API Key.");
            loadingLabel.setVisible(true);
            recommendationLabel.setVisible(false);
            recommendButton.setDisable(false);
            genreFilterDropdown.setDisable(false); // Aktifkan lagi jika gagal
            loadMovieCardsTask.getException().printStackTrace();
        });

        // --- BARU: setOnSucceeded di-update ---
        loadMovieCardsTask.setOnSucceeded(e -> {
            loadingLabel.setVisible(false);
            recommendButton.setDisable(false);
            genreFilterDropdown.setDisable(false); // Aktifkan dropdown

            if (recommendationCache.isEmpty()) { // Cek cache
                loadingLabel.setText("Tidak ada film yang cocok dengan vibe artis.");
                loadingLabel.setVisible(true);
                recommendationLabel.setVisible(false);
            } else {
                recommendationLabel.setVisible(true); // Tampilkan label hasil
            }
        });

        new Thread(loadMovieCardsTask).start();
    }

    // --- BARU: FUNGSI FILTER INSTAN ---
    private void onGenreFilterChanged() {
        // Jika cache kosong (belum ada pencarian), jangan lakukan apa-apa
        if (recommendationCache.isEmpty()) {
            return;
        }

        String selectedGenreName = genreFilterDropdown.getValue();

        // 1. Bersihkan tampilan
        resultTilePane.getChildren().clear();

        // 2. Cek apakah "Semua Genre"
        if (selectedGenreName == null || selectedGenreName.equals("Semua Genre")) {
            // Jika ya, tambahkan semua kartu dari cache
            for (CachedMovieCard cachedCard : recommendationCache) {
                resultTilePane.getChildren().add(cachedCard.cardNode);
            }
        } else {
            // 3. Jika genre spesifik dipilih
            // Ubah "Science Fiction" -> "genre_science_fiction"
            String genreKey = "genre_" + selectedGenreName.toLowerCase()
                    .replace(" ", "_")
                    .replace("-", "_"); // untuk "sci-fi"

            // Loop HANYA pada cache (super cepat)
            for (CachedMovieCard cachedCard : recommendationCache) {
                if (cachedCard.movie.getGenreValue(genreKey) == 1) {
                    // Hanya tambahkan kartu yang genrenya cocok
                    resultTilePane.getChildren().add(cachedCard.cardNode);
                }
            }
        }

        // 4. Update label jika hasil filter kosong
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