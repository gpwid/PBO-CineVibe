// File: MovieCardController.java
package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox; // <-- IMPORT

public class MovieCardController {

    @FXML private VBox cardRoot; // <-- Untuk mendeteksi klik
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label directorLabel;
    @FXML private Label actorsLabel;
    @FXML private Label descriptionLabel;

    // Simpan data film
    private Movie movie;
    private LocalMovieData localData;

    // Fungsi initialize untuk menambahkan event klik
    @FXML
    public void initialize() {
        cardRoot.setOnMouseClicked(event -> {
            if (this.movie != null && this.localData != null) {
                // --- DI-UPDATE: Kirim LocalMovieData ---
                Application.showDetailView(this.movie, this.localData);
            }
        });
    }

    // Fungsi untuk mengisi data dari HelloController
    public void setData(Movie movie, LocalMovieData localData) {
        // Simpan data
        this.movie = movie;
        this.localData = localData;

        // Isi UI
        if (localData != null) {
            titleLabel.setText(localData.title != null ? localData.title : "Judul Tidak Ditemukan");
            directorLabel.setText("Sutradara: " + localData.director);

            // Tampilkan 3 aktor pertama
            if (localData.actors != null && !localData.actors.isEmpty()) {
                StringBuilder actorString = new StringBuilder();
                int count = 0;
                for (LocalActorData actor : localData.actors) {
                    if (count >= 3) break;
                    actorString.append(actor.name).append(", ");
                    count++;
                }
                if (actorString.length() > 2) {
                    actorString.setLength(actorString.length() - 2);
                }
                actorsLabel.setText("Aktor: " + actorString.toString());
            } else {
                actorsLabel.setText("Aktor: N/A");
            }

            // Deskripsi
            String overview = localData.overview;
            if (overview == null || overview.trim().isEmpty()) {
                overview = "Deskripsi tidak tersedia.";
            } else if (overview.length() > 150) {
                overview = overview.substring(0, 147) + "...";
            }
            descriptionLabel.setText(overview);

            // --- BARU: Muat Poster LOKAL ---
            if (localData.poster_path != null) {
                String imagePath = LocalDataService.getInstance().getLocalImagePath(localData.poster_path);
                if (imagePath != null) {
                    posterImageView.setImage(new Image(imagePath));
                }
            }
        }
    }
}