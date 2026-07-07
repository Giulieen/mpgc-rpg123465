package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Finestra principale dell'applicazione.
 */
public class MainWindow {

    private final GameEngine gameEngine;

    private final Label floorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label eventLabel = new Label();

    private final Button eventButton = new Button("Esegui evento");
    private final Button nextFloorButton = new Button("Prossimo piano");

    public MainWindow(GameEngine gameEngine) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }

        this.gameEngine = gameEngine;
    }

    public void show(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Tower of Self");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        eventButton.setOnAction(event -> executeCurrentEvent());
        nextFloorButton.setOnAction(event -> moveToNextFloor());

        root.getChildren().addAll(
                title,
                floorLabel,
                descriptionLabel,
                eventLabel,
                eventButton,
                nextFloorButton
        );

        updateView();

        Scene scene = new Scene(root, 700, 500);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.show();
    }

    private void executeCurrentEvent() {
        gameEngine.executeCurrentFloorEvent();
        updateView();
    }

    private void moveToNextFloor() {
        if (!gameEngine.isGameCompleted()) {
            gameEngine.advanceFloor();
        }

        updateView();
    }

    private void updateView() {
        Floor floor = gameEngine.getCurrentFloor();

        floorLabel.setText("Piano: " + floor);
        descriptionLabel.setText(floor.getDescription());
        eventLabel.setText("Evento: " + floor.getEvent().getTitle());

        nextFloorButton.setDisable(gameEngine.isOnLastFloor() || !gameEngine.getPlayer().isAlive());
        eventButton.setDisable(gameEngine.isGameCompleted() || !gameEngine.getPlayer().isAlive());
    }
}