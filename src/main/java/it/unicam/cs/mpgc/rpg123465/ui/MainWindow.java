package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.events.DialogueChoice;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vista principale dell'applicazione.
 * <p>
 * Si occupa esclusivamente della presentazione: costruisce i componenti,
 * inoltra le azioni dell'utente al {@link GameController} e aggiorna le
 * etichette e l'inventario leggendo lo stato esposto dal controller.
 */
public class MainWindow {

    private final GameController controller;

    private final Label floorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label eventLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label playerHealthLabel = new Label();
    private final Label enemyHealthLabel = new Label();

    private final ListView<String> inventoryView = new ListView<>();

    private final Button eventButton = new Button("Esegui evento");
    private final Button nextFloorButton = new Button("Prossimo piano");
    private final Button attackButton = new Button("Attacca");
    private final Button useItemButton = new Button("Usa oggetto");
    private final Button escapeButton = new Button("Fuggi");
    private final Button saveButton = new Button("Salva");
    private final Button loadButton = new Button("Carica");

    private boolean gameEndShown = false;

    public MainWindow(GameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Il controller non può essere null.");
        }

        this.controller = controller;
    }

    public void show(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Tower of Self");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox mainActions = new HBox(10, eventButton, nextFloorButton);
        HBox combatActions = new HBox(10, attackButton, useItemButton, escapeButton);
        HBox saveActions = new HBox(10, saveButton, loadButton);

        Label inventoryTitle = new Label("Inventario");
        inventoryTitle.setStyle("-fx-font-weight: bold;");
        inventoryView.setPrefHeight(120);

        eventButton.setOnAction(event -> onExecuteEvent());
        nextFloorButton.setOnAction(event -> onNextFloor());
        attackButton.setOnAction(event -> onCombatAction(CombatAction.ATTACK));
        useItemButton.setOnAction(event -> onCombatAction(CombatAction.USE_ITEM));
        escapeButton.setOnAction(event -> onCombatAction(CombatAction.ESCAPE));
        saveButton.setOnAction(event -> onSave());
        loadButton.setOnAction(event -> onLoad());

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
                inventoryTitle,
                inventoryView,
                saveActions
        );

        updateView();

        Scene scene = new Scene(root, 760, 640);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.show();
    }

    private void onExecuteEvent() {
        if (controller.isCurrentEventExecuted()) {
            resultLabel.setText("Hai già affrontato l'evento di questo piano.");
            return;
        }

        Floor floor = controller.getCurrentFloor();

        if (floor.getEvent() instanceof DialogueEvent dialogueEvent) {
            Optional<DialogueChoice> choice = askDialogueChoice(dialogueEvent);

            if (choice.isEmpty()) {
                resultLabel.setText("Hai esitato davanti alla scelta.");
                return;
            }

            resultLabel.setText(controller.resolveDialogueChoice(choice.get()));
        } else {
            resultLabel.setText(controller.executeStandardEvent());
        }

        refresh();
    }

    private Optional<DialogueChoice> askDialogueChoice(DialogueEvent dialogueEvent) {
        List<DialogueChoice> choices = dialogueEvent.getChoices();

        ChoiceDialog<DialogueChoice> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(dialogueEvent.getTitle());
        dialog.setHeaderText(dialogueEvent.getDescription());
        dialog.setContentText("Scegli come reagire:");

        return dialog.showAndWait();
    }

    private void onCombatAction(CombatAction action) {
        resultLabel.setText(controller.executeCombatAction(action));
        refresh();
    }

    private void onNextFloor() {
        controller.advanceFloor();
        updateView();
    }

    private void onSave() {
        resultLabel.setText(controller.saveGame());
    }

    private void onLoad() {
        String message = controller.loadGame();
        gameEndShown = false;
        updateView();
        resultLabel.setText(message);
    }

    private void updateView() {
        Floor floor = controller.getCurrentFloor();

        floorLabel.setText("Piano: " + floor);
        descriptionLabel.setText(floor.getDescription());
        eventLabel.setText("Evento: " + floor.getEvent().getTitle());
        resultLabel.setText("");

        refresh();
    }

    private void refresh() {
        updateHealthLabels();
        updateInventory();
        updateButtons();
        checkGameEnd();
    }

    private void updateHealthLabels() {
        playerHealthLabel.setText(
                "Lucidità: " +
                        controller.getPlayerCurrentHealth() +
                        "/" +
                        controller.getPlayerMaxHealth()
        );

        Enemy enemy = controller.getCurrentEnemy();

        if (enemy == null) {
            enemyHealthLabel.setText("Nemico: nessuno");
        } else {
            enemyHealthLabel.setText(
                    "Nemico: " +
                            enemy.getName() +
                            " - Intensità: " +
                            enemy.getStats().getCurrentHealth() +
                            "/" +
                            enemy.getStats().getMaxHealth()
            );
        }
    }

    private void updateInventory() {
        List<String> entries = new ArrayList<>();

        for (Item item : controller.getInventoryItems()) {
            entries.add(item.getName() + " — " + item.getType());
        }

        inventoryView.getItems().setAll(entries);
    }

    private void updateButtons() {
        boolean playerDead = !controller.isPlayerAlive();
        boolean combatActive = controller.isCombatActive();
        boolean eventAlreadyExecuted = controller.isCurrentEventExecuted();

        nextFloorButton.setDisable(
                combatActive || controller.isOnLastFloor() || playerDead || !eventAlreadyExecuted
        );

        eventButton.setDisable(
                combatActive || controller.isGameCompleted() || playerDead || eventAlreadyExecuted
        );

        attackButton.setDisable(!combatActive || playerDead);
        useItemButton.setDisable(!combatActive || playerDead);
        escapeButton.setDisable(!combatActive || playerDead);
    }

    /**
     * Mostra un avviso di fine partita (sconfitta o vittoria) una sola volta e
     * disabilita i pulsanti di gioco. Il pulsante "Carica" resta attivo per
     * permettere di riprendere una partita salvata.
     */
    private void checkGameEnd() {
        if (gameEndShown) {
            return;
        }

        if (!controller.isPlayerAlive()) {
            gameEndShown = true;
            disableGameButtons();
            showAlert(Alert.AlertType.ERROR, "Game Over",
                    "La tua lucidità si è spenta: la torre ti ha sopraffatto.");
        } else if (controller.isGameCompleted() && !controller.isCombatActive()) {
            gameEndShown = true;
            disableGameButtons();
            showAlert(Alert.AlertType.INFORMATION, "Vittoria",
                    "Hai raggiunto la cima e affrontato il tuo Alter Ego: sei diventato integro.");
        }
    }

    private void disableGameButtons() {
        eventButton.setDisable(true);
        nextFloorButton.setDisable(true);
        attackButton.setDisable(true);
        useItemButton.setDisable(true);
        escapeButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
