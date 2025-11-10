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

    public void setData(TMDBService.CastMember member) {
        if (member == null) return;

        actorNameLabel.setText(member.name != null ? member.name : "N/A");
        characterNameLabel.setText(member.character != null ? member.character : "N/A");

        if (member.fullProfilePath != null) {
            actorImageView.setImage(new Image(member.fullProfilePath, true));
        } else {
            // Opsional: set gambar placeholder jika potret tidak ada
            // actorImageView.setImage(new Image(getClass().getResourceAsStream("placeholder_actor.png")));
        }
    }
}