// File: HelloApplication.java
package com.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {

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
        LocalDataService.getInstance().loadData();

        // Muat FXML utama (hello-view.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("hello-view.fxml"));
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
    public static void showDetailView(Movie movie, LocalMovieData localData) { // <-- Tipe data diubah
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("MovieDetailView.fxml"));
            Parent detailRoot = loader.load();
            MovieDetailController controller = loader.getController();

            // --- DI-UPDATE: Kirim LocalMovieData ---
            controller.setData(movie, localData);

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