// File: MovieCardController.java
package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox; // <-- IMPORT

import com.example.demo.TMDBService.MovieDetails;
import com.example.demo.TMDBService.MovieCredits;

public class MovieCardController {

    @FXML private VBox cardRoot; // <-- Untuk mendeteksi klik
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label directorLabel;
    @FXML private Label actorsLabel;
    @FXML private Label descriptionLabel;

    // Simpan data film
    private Movie movie;
    private TMDBService.MovieDetails details;
    private TMDBService.MovieCredits credits;

    // Fungsi initialize untuk menambahkan event klik
    @FXML
    public void initialize() {
        cardRoot.setOnMouseClicked(event -> {
            // Jika data sudah ter-load, pindah ke panel detail
            if (this.movie != null && this.details != null && this.credits != null) {
                HelloApplication.showDetailView(this.movie, this.details, this.credits);
            }
        });
    }

    // Fungsi untuk mengisi data dari HelloController
    public void setData(Movie movie, MovieDetails details, MovieCredits credits) {
        // Simpan data
        this.movie = movie;
        this.details = details;
        this.credits = credits;

        // Isi UI
        if (details != null) {
            titleLabel.setText(details.title != null ? details.title : "Judul Tidak Ditemukan");

            String overview = details.overview;
            if (overview == null || overview.trim().isEmpty()) {
                overview = "Deskripsi tidak tersedia.";
            } else if (overview.length() > 150) {
                overview = overview.substring(0, 147) + "...";
            }
            descriptionLabel.setText(overview);

            if (details.fullPosterPath != null) {
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