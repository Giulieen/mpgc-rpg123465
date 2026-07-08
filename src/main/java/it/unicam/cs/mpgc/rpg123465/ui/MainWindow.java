package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.events.EventResult;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Finestra principale dell'applicazione.
 */
public class MainWindow {

    private GameEngine gameEngine;

    private final SaveManager saveManager = new FileSaveManager("saves/save.dat");

    private final Label floorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label eventLabel = new Label();
    private final Label resultLabel = new Label();

    private final Button eventButton = new Button("Esegui evento");
    private final Button nextFloorButton = new Button("Prossimo piano");
    private final Button saveButton = new Button("Salva");
    private final Button loadButton = new Button("Carica");

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
        saveButton.setOnAction(event -> saveGame());
        loadButton.setOnAction(event -> loadGame());

        root.getChildren().addAll(
                title,
                floorLabel,
                descriptionLabel,
                eventLabel,
                resultLabel,
                eventButton,
                nextFloorButton,
                saveButton,
                loadButton
        );

        updateView();

        Scene scene = new Scene(root, 700, 500);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.show();
    }

    private void executeCurrentEvent() {
        EventResult result = gameEngine.executeCurrentFloorEvent();
        resultLabel.setText("Risultato: " + result.getMessage());
        updateButtons();
    }

    private void moveToNextFloor() {
        if (!gameEngine.isGameCompleted()) {
            gameEngine.advanceFloor();
        }

        updateView();
    }

    private void saveGame() {
        try {
            GameSave save = new GameSave(
                    gameEngine.getPlayer().getName(),
                    gameEngine.getCurrentFloorIndex(),
                    gameEngine.getPlayer().getStats().getCurrentHealth()
            );

            saveManager.save(save);
            resultLabel.setText("Partita salvata correttamente.");
        } catch (IOException e) {
            resultLabel.setText("Errore durante il salvataggio: " + e.getMessage());
        }
    }

    private void loadGame() {
        try {
            GameSave save = saveManager.load();

            gameEngine = GameFactory.createNewGame();

            while (gameEngine.getCurrentFloorIndex() < save.getCurrentFloor()) {
                gameEngine.advanceFloor();
            }

            int currentHealth = gameEngine.getPlayer().getStats().getCurrentHealth();
            int damageToApply = currentHealth - save.getCurrentHealth();

            if (damageToApply > 0) {
                gameEngine.getPlayer().takeDamage(damageToApply);
            }

            updateView();
            resultLabel.setText("Partita caricata correttamente.");
        } catch (IOException | ClassNotFoundException e) {
            resultLabel.setText("Errore durante il caricamento: " + e.getMessage());
        }
    }

    private void updateView() {
        Floor floor = gameEngine.getCurrentFloor();

        floorLabel.setText("Piano: " + floor);
        descriptionLabel.setText(floor.getDescription());
        eventLabel.setText("Evento: " + floor.getEvent().getTitle());
        resultLabel.setText("");

        updateButtons();
    }

    private void updateButtons() {
        nextFloorButton.setDisable(gameEngine.isOnLastFloor() || !gameEngine.getPlayer().isAlive());
        eventButton.setDisable(gameEngine.isGameCompleted() || !gameEngine.getPlayer().isAlive());
    }
}