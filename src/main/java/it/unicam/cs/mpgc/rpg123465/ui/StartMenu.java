package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.function.Consumer;

public class StartMenu {

    private final Consumer<String> onNewGame;
    private final Runnable onLoadGame;
    private final TextField nameField = new TextField();

    public StartMenu(Consumer<String> onNewGame, Runnable onLoadGame) {
        if (onNewGame == null || onLoadGame == null) {
            throw new IllegalArgumentException("I callback non possono essere null.");
        }

        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
    }

    public Parent createView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("menu-root");


        Rectangle darkOverlay = new Rectangle();
        darkOverlay.getStyleClass().add("menu-dark-overlay");
        darkOverlay.widthProperty().bind(root.widthProperty());
        darkOverlay.heightProperty().bind(root.heightProperty());

        Region fog = new FogOverlay().createView();

        VBox card = createMenuCard();
        StackPane.setAlignment(card, Pos.CENTER);

        root.getChildren().addAll(darkOverlay, fog, card);

        return root;
    }

    private VBox createMenuCard() {
        Label title = new Label("TOWER OF SELF");
        title.getStyleClass().add("menu-title");

        Label subtitle = new Label("""
                Ogni paura ha un volto.
                Solo affrontandola potrai salire.
                """);
        subtitle.getStyleClass().add("menu-subtitle");

        Label description = new Label("Come ti chiami?");
        description.getStyleClass().add("narrative");

        nameField.setPromptText("Nome del giocatore");
        
        nameField.setMaxWidth(280);
        nameField.getStyleClass().add("name-field");

        Button newGameButton = new Button("Nuova partita");
        Button loadButton = new Button("Carica partita");

        newGameButton.getStyleClass().add("menu-button");
        loadButton.getStyleClass().add("menu-button");

        addButtonAnimation(newGameButton);
        addButtonAnimation(loadButton);

        newGameButton.setOnAction(event -> onNewGame.accept(nameField.getText()));
        loadButton.setOnAction(event -> onLoadGame.run());

        HBox buttons = new HBox(22, newGameButton, loadButton);
        buttons.setAlignment(Pos.CENTER);

        VBox menu = new VBox(
                26,
                title,
                subtitle,
                description,
                nameField,
                buttons
        );

        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));
        menu.setMaxWidth(820);
        menu.setMaxHeight(620);
        menu.getStyleClass().add("menu-card");

        return menu;
    }

    private void addButtonAnimation(Button button) {
        DropShadow normalShadow = new DropShadow();
        normalShadow.setColor(Color.rgb(0, 0, 0, 0.75));
        normalShadow.setRadius(12);
        normalShadow.setOffsetY(4);

        DropShadow glowShadow = new DropShadow();
        glowShadow.setColor(Color.rgb(255, 190, 70, 0.85));
        glowShadow.setRadius(22);
        glowShadow.setSpread(0.35);

        button.setEffect(normalShadow);

        button.setOnMouseEntered(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(140), button);
            scale.setToX(1.06);
            scale.setToY(1.06);
            scale.play();

            button.setEffect(glowShadow);
        });

        button.setOnMouseExited(event -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(140), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            button.setEffect(normalShadow);
        });
    }
}