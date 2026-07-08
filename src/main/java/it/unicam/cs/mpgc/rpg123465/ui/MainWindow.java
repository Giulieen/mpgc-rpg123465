package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.combat.CombatEngine;
import it.unicam.cs.mpgc.rpg123465.combat.CombatResult;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import it.unicam.cs.mpgc.rpg123465.events.EventResult;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MainWindow {

    private GameEngine gameEngine;
    private Enemy currentEnemy;

    private final Set<Integer> executedFloors = new HashSet<>();

    private final CombatEngine combatEngine = new CombatEngine();
    private final SaveManager saveManager = new FileSaveManager("saves/save.dat");

    private final Label floorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label eventLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label playerHealthLabel = new Label();
    private final Label enemyHealthLabel = new Label();

    private final Button eventButton = new Button("Esegui evento");
    private final Button nextFloorButton = new Button("Prossimo piano");
    private final Button attackButton = new Button("Attacca");
    private final Button useItemButton = new Button("Usa oggetto");
    private final Button escapeButton = new Button("Fuggi");
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
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox mainActions = new HBox(10, eventButton, nextFloorButton);
        HBox combatActions = new HBox(10, attackButton, useItemButton, escapeButton);
        HBox saveActions = new HBox(10, saveButton, loadButton);

        eventButton.setOnAction(event -> executeCurrentEvent());
        nextFloorButton.setOnAction(event -> moveToNextFloor());
        attackButton.setOnAction(event -> executeCombatAction(CombatAction.ATTACK));
        useItemButton.setOnAction(event -> executeCombatAction(CombatAction.USE_ITEM));
        escapeButton.setOnAction(event -> executeCombatAction(CombatAction.ESCAPE));
        saveButton.setOnAction(event -> saveGame());
        loadButton.setOnAction(event -> loadGame());

        root.getChildren().addAll(
                title,
                floorLabel,
                descriptionLabel,
                eventLabel,
                playerHealthLabel,
                enemyHealthLabel,
                resultLabel,
                mainActions,
                combatActions,
                saveActions
        );

        updateView();

        Scene scene = new Scene(root, 760, 520);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.show();
    }

    private void executeCurrentEvent() {
        Floor floor = gameEngine.getCurrentFloor();

        if (executedFloors.contains(gameEngine.getCurrentFloorIndex())) {
            resultLabel.setText("Hai già affrontato l'evento di questo piano.");
            return;
        }

        if (floor.getEvent() instanceof DialogueEvent dialogueEvent) {
            executeDialogueEvent(dialogueEvent);
        } else {
            EventResult result = gameEngine.executeCurrentFloorEvent();
            resultLabel.setText("Risultato: " + result.getMessage());

            if (floor.getEvent() instanceof CombatEvent combatEvent) {
                currentEnemy = combatEvent.getEnemy();
            }
        }

        executedFloors.add(gameEngine.getCurrentFloorIndex());

        updateButtons();
        updateHealthLabels();
    }

    private void executeDialogueEvent(DialogueEvent dialogueEvent) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                dialogueEvent.getChoices().get(0),
                dialogueEvent.getChoices()
        );

        dialog.setTitle(dialogueEvent.getTitle());
        dialog.setHeaderText(dialogueEvent.getDescription());
        dialog.setContentText("Scegli come reagire:");

        Optional<String> choice = dialog.showAndWait();

        if (choice.isEmpty()) {
            resultLabel.setText("Hai esitato davanti alla scelta.");
            return;
        }

        applyDialogueChoice(choice.get());
    }

    private void applyDialogueChoice(String choice) {
        if (choice.toLowerCase().contains("forza")) {
            gameEngine.getPlayer().takeDamage(10);
            resultLabel.setText("Hai reagito con forza, ma la rabbia ti consuma. Perdi 10 punti vita.");
        } else {
            gameEngine.getPlayer().heal(10);
            resultLabel.setText("Respiri, osservi e ritrovi controllo. Recuperi 10 punti vita.");
        }
    }

    private void executeCombatAction(CombatAction action) {
        if (currentEnemy == null) {
            resultLabel.setText("Nessun combattimento attivo.");
            return;
        }

        CombatResult result = combatEngine.executeTurn(
                gameEngine.getPlayer(),
                currentEnemy,
                action
        );

        if (result == null) {
            resultLabel.setText("Il combattimento continua.");
        } else {
            handleCombatResult(result);
        }

        updateButtons();
        updateHealthLabels();
    }

    private void handleCombatResult(CombatResult result) {
        switch (result) {
            case VICTORY -> {
                resultLabel.setText("Hai sconfitto " + currentEnemy.getName() + "!");
                currentEnemy = null;
            }
            case DEFEAT -> {
                resultLabel.setText("Sei stato sconfitto...");
                currentEnemy = null;
            }
            case ESCAPE -> {
                resultLabel.setText("Sei riuscito a fuggire.");
                currentEnemy = null;
            }
        }
    }

    private void moveToNextFloor() {
        if (!gameEngine.isGameCompleted()) {
            gameEngine.advanceFloor();
        }

        currentEnemy = null;
        updateView();
    }

    private void saveGame() {
        try {
            GameSave save = new GameSave(
                    gameEngine.getPlayer().getName(),
                    gameEngine.getCurrentFloorIndex(),
                    gameEngine.getPlayer().getStats().getCurrentHealth(),
                    gameEngine.isGameCompleted()
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
            gameEngine.restoreState(save.getCurrentFloor(), save.isGameCompleted());
            gameEngine.getPlayer().getStats().setCurrentHealth(save.getCurrentHealth());

            currentEnemy = null;
            executedFloors.clear();

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

        updateHealthLabels();
        updateButtons();
    }

    private void updateHealthLabels() {
        playerHealthLabel.setText(
                "Vita giocatore: " +
                        gameEngine.getPlayer().getStats().getCurrentHealth() +
                        "/" +
                        gameEngine.getPlayer().getStats().getMaxHealth()
        );

        if (currentEnemy == null) {
            enemyHealthLabel.setText("Nemico: nessuno");
        } else {
            enemyHealthLabel.setText(
                    "Nemico: " +
                            currentEnemy.getName() +
                            " - Vita: " +
                            currentEnemy.getStats().getCurrentHealth() +
                            "/" +
                            currentEnemy.getStats().getMaxHealth()
            );
        }
    }

    private void updateButtons() {
        boolean playerDead = !gameEngine.getPlayer().isAlive();
        boolean combatActive = currentEnemy != null;
        boolean eventAlreadyExecuted = executedFloors.contains(gameEngine.getCurrentFloorIndex());

        nextFloorButton.setDisable(
                combatActive || gameEngine.isOnLastFloor() || playerDead || !eventAlreadyExecuted
        );

        eventButton.setDisable(
                combatActive || gameEngine.isGameCompleted() || playerDead || eventAlreadyExecuted
        );

        attackButton.setDisable(!combatActive || playerDead);
        useItemButton.setDisable(!combatActive || playerDead);
        escapeButton.setDisable(!combatActive || playerDead);
    }
}