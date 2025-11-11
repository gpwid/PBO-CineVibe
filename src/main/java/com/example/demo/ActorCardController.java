// File: ActorCardController.java
package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ActorCardController {

    @FXML private ImageView actorImageView;
    @FXML private Label actorNameLabel;
    @FXML private Label characterNameLabel;

    public void setData(LocalActorData member) {
        if (member == null) return;

        actorNameLabel.setText(member.name != null ? member.name : "N/A");
        characterNameLabel.setText(member.character != null ? member.character : "N/A");

        // --- BARU: Muat Potret LOKAL ---
        if (member.profile_path != null) {
            String imagePath = LocalDataService.getInstance().getLocalImagePath(member.profile_path);
            if (imagePath != null) {
                actorImageView.setImage(new Image(imagePath));
            }
        }
    }
}