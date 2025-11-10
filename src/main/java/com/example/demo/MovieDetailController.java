// File: MovieDetailController.java
package com.example.demo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox; // <-- BARU
import javafx.scene.layout.TilePane; // <-- BARU
import javafx.scene.paint.Color;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MovieDetailController {

    @FXML private AnchorPane detailPane;
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label directorLabel;
    @FXML private Label actorsLabel; // <-- Label ini tidak kita pakai lagi, tapi biarkan saja
    @FXML private Label descriptionLabel;

    // --- KONTROL GALERI BARU ---
    @FXML private HBox actorGalleryPane;
    @FXML private TilePane backdropGalleryPane;
    @FXML private Label userScoreLabel;
    @FXML private Label ratingLabel;

    private TMDBService tmdbService = new TMDBService(); // <-- Service API

    /**
     * Dipanggil oleh HelloApplication untuk mengirim data
     */
    public void setData(Movie movie, TMDBService.MovieDetails details, TMDBService.MovieCredits credits) {

        // 1. Isi data teks
        titleLabel.setText(details.title != null ? details.title : "Judul Tidak Ditemukan");
        directorLabel.setText("Sutradara: " + credits.getDirector());

        if (details.vote_average > 0) {
            DecimalFormat df = new DecimalFormat("#.#"); // Format 1 angka desimal
            userScoreLabel.setText(df.format(details.vote_average) + " / 10 User Score");
        } else {
            userScoreLabel.setText("N/A");
        }

        String certification = details.getCertification();
        if (certification != null && !certification.isEmpty()) {
            ratingLabel.setText(certification);
            ratingLabel.setVisible(true);
        } else {
            ratingLabel.setVisible(false); // Sembunyikan jika tidak ada rating
        }

        String overview = details.overview;
        if (overview == null || overview.trim().isEmpty()) {
            overview = "Deskripsi tidak tersedia.";
        }
        descriptionLabel.setText(overview);

        // 2. Muat poster & Ekstrak Warna (Logika ini tetap sama)
        if (details.fullPosterPath != null) {
            Image posterImage = new Image(details.fullPosterPath, true);
            posterImageView.setImage(posterImage);
            posterImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    extractDominantColor(posterImage);
                }
            });
        }

        // --- 3. LOGIKA BARU: Muat Kartu Aktor ---
        // (Kita muat ini langsung, karena datanya sudah ada di 'credits')
        loadActorCards(credits);

        // --- 4. LOGIKA BARU: Muat Galeri Backdrop (di Background) ---
        // (Kita perlu memanggil API baru, jadi harus pakai Task)
        loadBackdropGallery(movie.getTmdbMovieId());
    }

    /**
     * Dipanggil oleh Tombol 'Kembali'
     */
    @FXML
    private void onBackClick() {
        HelloApplication.showMainView();
    }

    /**
     * BARU: Mengisi galeri HBox dengan kartu aktor
     */
    private void loadActorCards(TMDBService.MovieCredits credits) {
        if (credits == null || credits.cast == null) return;

        actorGalleryPane.getChildren().clear(); // Kosongkan galeri
        int count = 0;
        for (TMDBService.CastMember member : credits.cast) {
            if (count >= 10) break; // Batasi 10 aktor teratas

            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ActorCard.fxml"));
                Node actorCardNode = loader.load();

                ActorCardController controller = loader.getController();
                controller.setData(member); // Kirim data aktor ke kartu

                actorGalleryPane.getChildren().add(actorCardNode); // Tambahkan kartu ke HBox
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    /**
     * BARU: Memanggil API gambar dan mengisi galeri backdrop
     */
    private void loadBackdropGallery(int tmdbId) {
        Task<TMDBService.MovieImages> imageTask = new Task<>() {
            @Override
            protected TMDBService.MovieImages call() throws Exception {
                // Panggil API di background thread
                return tmdbService.getMovieImages(tmdbId);
            }
        };

        imageTask.setOnSucceeded(e -> {
            TMDBService.MovieImages images = imageTask.getValue();
            if (images == null || images.backdrops == null) return;

            backdropGalleryPane.getChildren().clear(); // Kosongkan galeri
            int count = 0;
            for (TMDBService.ImageItem backdrop : images.backdrops) {
                if (count >= 6) break; // Batasi 6 gambar
                if (backdrop.fullBackdropPath != null) {
                    // Buat ImageView secara dinamis
                    ImageView backdropView = new ImageView(new Image(backdrop.fullBackdropPath, true));
                    backdropView.setFitWidth(250); // Ukuran gambar galeri
                    backdropView.setPreserveRatio(true);
                    backdropView.getStyleClass().add("backdrop-image"); // Tambahkan style
                    backdropGalleryPane.getChildren().add(backdropView);
                }
                count++;
            }
        });

        imageTask.setOnFailed(e -> imageTask.getException().printStackTrace());

        new Thread(imageTask).start();
    }

    /**
     * Menganalisis gambar poster untuk menemukan warna dominan
     */
    private void extractDominantColor(Image image) {
        // ... (Fungsi ini tidak berubah) ...
        Task<Color> colorTask = new Task<>() {
            @Override
            protected Color call() throws Exception {
                PixelReader pixelReader = image.getPixelReader();
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();
                Map<Integer, Integer> hueHistogram = new HashMap<>();
                int maxCount = 0; int dominantHue = 0; int sampleStep = 10;
                for (int y = 0; y < height; y += sampleStep) {
                    for (int x = 0; x < width; x += sampleStep) {
                        Color pixel = pixelReader.getColor(x, y);
                        if (pixel.getSaturation() > 0.2 && pixel.getBrightness() > 0.2 && pixel.getBrightness() < 0.9) {
                            int hue = (int) pixel.getHue();
                            int hueGroup = (hue / 10) * 10;
                            int count = hueHistogram.getOrDefault(hueGroup, 0) + 1;
                            hueHistogram.put(hueGroup, count);
                            if (count > maxCount) { maxCount = count; dominantHue = hueGroup; }
                        }
                    }
                }
                if (maxCount == 0) return null;
                return Color.hsb(dominantHue, 0.4, 0.3);
            }
        };
        colorTask.setOnSucceeded(e -> {
            Color dominantColor = colorTask.getValue();
            if (dominantColor != null) {
                String hexColor = String.format("#%02X%02X%02X",
                        (int) (dominantColor.getRed() * 255),
                        (int) (dominantColor.getGreen() * 255),
                        (int) (dominantColor.getBlue() * 255));
                String gradientStyle = String.format(
                        "-fx-background-color: linear-gradient(to bottom, %s 0%%, -background-dark 100%%);",
                        hexColor
                );
                detailPane.setStyle(gradientStyle);
            }
        });
        new Thread(colorTask).start();
    }
}