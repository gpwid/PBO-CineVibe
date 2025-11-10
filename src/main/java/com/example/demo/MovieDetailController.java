// File: MovieDetailController.java
package com.example.demo;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public class MovieDetailController {

    @FXML private BorderPane detailPane;
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label directorLabel;
    @FXML private Label actorsLabel;
    @FXML private Label descriptionLabel;

    /**
     * Dipanggil oleh HelloApplication untuk mengirim data
     */
    public void setData(Movie movie, TMDBService.MovieDetails details, TMDBService.MovieCredits credits) {
        // 1. Isi data teks
        titleLabel.setText(details.title != null ? details.title : "Judul Tidak Ditemukan");
        directorLabel.setText("Sutradara: " + credits.getDirector());
        actorsLabel.setText("Aktor Utama: " + credits.getActors());

        String overview = details.overview;
        if (overview == null || overview.trim().isEmpty()) {
            overview = "Deskripsi tidak tersedia.";
        }
        descriptionLabel.setText(overview);

        // 2. Muat poster
        if (details.fullPosterPath != null) {
            Image posterImage = new Image(details.fullPosterPath, true);
            posterImageView.setImage(posterImage);

            // 3. Jalankan Ekstraksi Warna setelah gambar selesai dimuat
            posterImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0) {
                    extractDominantColor(posterImage);
                }
            });
        }
    }

    /**
     * Dipanggil oleh Tombol 'Kembali'
     */
    @FXML
    private void onBackClick() {
        // Panggil router statis untuk kembali
        HelloApplication.showMainView();
    }

    /**
     * Menganalisis gambar poster untuk menemukan warna dominan
     */
    private void extractDominantColor(Image image) {
        Task<Color> colorTask = new Task<>() {
            @Override
            protected Color call() throws Exception {
                PixelReader pixelReader = image.getPixelReader();
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();

                Map<Integer, Integer> hueHistogram = new HashMap<>();
                int maxCount = 0;
                int dominantHue = 0;
                int sampleStep = 10; // Cek 1 dari 10 pixel

                for (int y = 0; y < height; y += sampleStep) {
                    for (int x = 0; x < width; x += sampleStep) {
                        Color pixel = pixelReader.getColor(x, y);

                        // Abaikan warna pudar
                        if (pixel.getSaturation() > 0.2 && pixel.getBrightness() > 0.2 && pixel.getBrightness() < 0.9) {
                            int hue = (int) pixel.getHue();
                            int hueGroup = (hue / 10) * 10;
                            int count = hueHistogram.getOrDefault(hueGroup, 0) + 1;
                            hueHistogram.put(hueGroup, count);

                            if (count > maxCount) {
                                maxCount = count;
                                dominantHue = hueGroup;
                            }
                        }
                    }
                }

                if (maxCount == 0) {
                    return null; // Tidak ada warna dominan
                }

                // Buat warna baru (gelap dan pudar)
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

                // Terapkan ke background panel detail
                detailPane.setStyle("-fx-background-color: " + hexColor);
            }
        });

        new Thread(colorTask).start();
    }
}