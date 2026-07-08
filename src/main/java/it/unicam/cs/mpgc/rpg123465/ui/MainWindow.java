package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Vista principale dell'applicazione.
 * <p>
 * Si occupa esclusivamente della presentazione: costruisce i componenti,
 * inoltra le azioni dell'utente al {@link GameController} e aggiorna le
 * etichette leggendo lo stato esposto dal controller.
 */
public class MainWindow {

    private final GameController controller;

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
                saveActions
        );

        updateView();

        Scene scene = new Scene(root, 760, 520);

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
            Optional<String> choice = askDialogueChoice(dialogueEvent);

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

    private Optional<String> askDialogueChoice(DialogueEvent dialogueEvent) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                dialogueEvent.getChoices().get(0),
                dialogueEvent.getChoices()
        );

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
        updateButtons();
    }

    private void updateHealthLabels() {
        playerHealthLabel.setText(
                "Vita giocatore: " +
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
                            " - Vita: " +
                            enemy.getStats().getCurrentHealth() +
                            "/" +
                            enemy.getStats().getMaxHealth()
            );
        }
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
}
