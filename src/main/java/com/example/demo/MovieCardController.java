// File: MovieCardController.java
package com.example.demo; // Ganti dengan nama package Anda

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Kita akan mengimpor kelas internal dari TMDBService
import com.example.demo.TMDBService.MovieDetails;
import com.example.demo.TMDBService.MovieCredits;

public class MovieCardController {

    @FXML
    private ImageView posterImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label directorLabel;
    @FXML
    private Label actorsLabel;
    @FXML
    private Label descriptionLabel;

    // Fungsi ini akan dipanggil oleh HelloController
    // untuk mengisi data ke kartu ini
    public void setData(MovieDetails details, MovieCredits credits) {
        if (details != null) {
            titleLabel.setText(details.title != null ? details.title : "Judul Tidak Ditemukan");

            // Potong deskripsi jika terlalu panjang
            String overview = details.overview;
            if (overview == null || overview.isEmpty()) {
                overview = "Deskripsi tidak tersedia.";
            } else if (overview.length() > 150) {
                overview = overview.substring(0, 147) + "...";
            }
            descriptionLabel.setText(overview);

            // Muat gambar poster
            if (details.fullPosterPath != null) {
                // 'true' di akhir berarti memuat gambar di background (non-blocking)
                Image poster = new Image(details.fullPosterPath, true);
                posterImageView.setImage(poster);
            }
        }

        if (credits != null) {
            directorLabel.setText("Sutradara: " + credits.getDirector());
            actorsLabel.setText("Aktor: " + credits.getActors());
        }
    }
}