// File: HelloApplication.java
package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {

    private static Stage primaryStage;
    private static Parent mainViewRoot; // Menyimpan panel utama (rekomendasi)

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage; // Simpan stage utama

        // --- Memuat Font (Inter) ---
        try {
            Font.loadFont(getClass().getResourceAsStream("Inter-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("Inter-Bold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("Inter-Italic.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Gagal memuat font Inter. Pastikan file .ttf ada di folder resources/com/example/demo/");
        }
        // --- Akhir Blok Font ---

        // Muat FXML utama (hello-view.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        mainViewRoot = fxmlLoader.load(); // Simpan panel utama

        Scene scene = new Scene(mainViewRoot, 1200, 800);
        stage.setTitle("CineVibe Recommender");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.show();
    }

    /**
     * Fungsi helper statis untuk beralih ke panel Detail
     */
    public static void showDetailView(Movie movie, TMDBService.MovieDetails details, TMDBService.MovieCredits credits) {
        try {
            // 1. Muat FXML panel detail yang baru
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("MovieDetailView.fxml"));
            Parent detailRoot = loader.load();

            // 2. Ambil controller-nya
            MovieDetailController controller = loader.getController();

            // 3. Kirim data (film, detail, kredit) ke controller itu
            controller.setData(movie, details, credits);

            // 4. Ganti panel di window utama
            primaryStage.getScene().setRoot(detailRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fungsi helper statis untuk kembali ke panel Rekomendasi
     */
    public static void showMainView() {
        // Kembalikan panel utama yang sudah kita simpan
        primaryStage.getScene().setRoot(mainViewRoot);
    }

    public static void main(String[] args) {
        launch(args);
    }
}