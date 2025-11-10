package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Kita sekarang memuat font "Inter"
            Font.loadFont(getClass().getResourceAsStream("Inter-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("Inter-Bold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("Inter-Italic.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Gagal memuat font Inter. Menggunakan font default sistem.");
            // e.printStackTrace(); // Uncomment untuk debug jika font tidak termuat
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("CineVibe");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}