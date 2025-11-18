package com.siata.client.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PlaceholderPageView extends VBox {

    public PlaceholderPageView(String message) {
        setSpacing(24);
        build(message);
    }

    private void build(String message) {
        StackPane placeholderCard = new StackPane();
        placeholderCard.getStyleClass().add("placeholder-card");

        Label title = new Label("Belum ada data yang ditampilkan");
        title.getStyleClass().add("placeholder-title");

        Label description = new Label(message);
        description.getStyleClass().add("placeholder-description");
        description.setWrapText(true);
        description.setMaxWidth(420);

        VBox contentBox = new VBox(8, title, description);
        contentBox.setAlignment(Pos.CENTER);

        placeholderCard.getChildren().add(contentBox);
        StackPane.setAlignment(contentBox, Pos.CENTER);

        getChildren().add(placeholderCard);
    }
}

