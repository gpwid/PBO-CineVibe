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
            Font.loadFont(getClass().getResourceAsStream("/com/example/demo/Roboto-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/com/example/demo/Roboto-Bold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/com/example/demo/Roboto-Italic.ttf"), 10);
        } catch (Exception e) {
            System.err.println("Gagal memuat font Roboto. Menggunakan font default.");
            // e.printStackTrace(); // Uncomment untuk debug jika font tidak termuat
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 440);
        stage.setTitle("CineVibe");
        stage.setScene(scene);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}