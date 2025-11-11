// File: MovieDetailController.java
package com.example.demo;

import javafx.concurrent.Task; // Tetap perlu Task untuk ekstraksi warna
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
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
    @FXML private Label descriptionLabel;
    @FXML private HBox actorGalleryPane;
    @FXML private TilePane backdropGalleryPane;
    @FXML private Label userScoreLabel;
    @FXML private Label ratingLabel;

    // HAPUS: private TMDBService tmdbService;

    /**
     * Dipanggil oleh HelloApplication (DI-UPDATE)
     */
    public void setData(Movie movie, LocalMovieData localData) {

        // --- LOGIKA OFFLINE BARU ---

        // 1. Isi data teks
        titleLabel.setText(localData.title != null ? localData.title : "Judul Tidak Ditemukan");
        directorLabel.setText("Sutradara: " + localData.director);

        DecimalFormat df = new DecimalFormat("#.#");
        userScoreLabel.setText(df.format(localData.vote_average) + " / 10 User Score");

        if (localData.certification != null && !localData.certification.isEmpty()) {
            ratingLabel.setText(localData.certification);
            ratingLabel.setVisible(true);
        } else {
            ratingLabel.setVisible(false);
        }

        String overview = localData.overview;
        if (overview == null || overview.trim().isEmpty()) {
            overview = "Deskripsi tidak tersedia.";
        }
        descriptionLabel.setText(overview);

        // 2. Muat poster LOKAL & Ekstrak Warna
        if (localData.poster_path != null) {
            String imagePath = LocalDataService.getInstance().getLocalImagePath(localData.poster_path);
            if (imagePath != null) {
                Image posterImage = new Image(imagePath);
                posterImageView.setImage(posterImage);
                // Ekstraksi warna tetap berjalan (karena ini dari gambar yang sudah di-load)
                extractDominantColor(posterImage);
            }
        }

        // 3. Muat Aktor LOKAL
        loadActorCards(localData);

        // 4. Muat Backdrop LOKAL
        loadBackdropGallery(localData);
    }

    /**
     * Dipanggil oleh Tombol 'Kembali' (Tidak Berubah)
     */
    @FXML
    private void onBackClick() {
        HelloApplication.showMainView();
    }

    /**
     * BARU: Mengisi galeri HBox dengan kartu aktor LOKAL
     */
    private void loadActorCards(LocalMovieData localData) {
        if (localData == null || localData.actors == null) return;

        actorGalleryPane.getChildren().clear();
        for (LocalActorData member : localData.actors) {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("ActorCard.fxml"));
                Node actorCardNode = loader.load();

                ActorCardController controller = loader.getController();
                controller.setData(member); // Kirim data LOKAL ke kartu

                actorGalleryPane.getChildren().add(actorCardNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * BARU: Mengisi galeri backdrop LOKAL
     */
    private void loadBackdropGallery(LocalMovieData localData) {
        if (localData == null || localData.backdrops == null) return;

        backdropGalleryPane.getChildren().clear();
        for (String backdropPath : localData.backdrops) {
            String imagePath = LocalDataService.getInstance().getLocalImagePath(backdropPath);
            if (imagePath != null) {
                ImageView backdropView = new ImageView(new Image(imagePath));
                backdropView.setFitWidth(250); // Ukuran gambar galeri
                backdropView.setPreserveRatio(true);
                backdropView.getStyleClass().add("backdrop-image");
                backdropGalleryPane.getChildren().add(backdropView);
            }
        }
    }

    /**
     * Menganalisis gambar poster untuk menemukan warna dominan (Tidak Berubah)
     */
    private void extractDominantColor(Image image) {
        // ... (Fungsi ini tidak berubah, karena bekerja pada Image object) ...
        Task<Color> colorTask = new Task<>() {
            @Override
            protected Color call() throws Exception {
                PixelReader pixelReader = image.getPixelReader();
                int width = (int) image.getWidth(); int height = (int) image.getHeight();
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
                return Color.hsb(dominantHue, 0.4, 0.3); // (Hue, Saturasi 40%, Kecerahan 30%)
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