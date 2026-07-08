package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.challenge.ChallengeQuestion;
import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.engine.GameState;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.DialogueChoice;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import it.unicam.cs.mpgc.rpg123465.events.ItemEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainWindow {

    private final GameController controller;

    private final Label floorLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label eventLabel = new Label();
    private final Label resultLabel = new Label();
    private final Label playerHealthLabel = new Label();
    private final Label enemyHealthLabel = new Label();

    private final ProgressBar playerHealthBar = new ProgressBar(1);
    private final ProgressBar enemyHealthBar = new ProgressBar(0);
    private final ListView<String> inventoryView = new ListView<>();

    private final Button eventButton = new Button();
    private final Button attackButton = new Button("Attacca");
    private final Button confrontButton = new Button("Confrontati");
    private final Button useItemButton = new Button("Usa oggetto");
    private final Button escapeButton = new Button("Fuggi");
    private final Button saveButton = new Button("Salva");
    private final Button loadButton = new Button("Carica");

    private VBox enemyPanel;
    private VBox mainActionsPanel;
    private VBox combatPanel;
    private boolean gameEndShown = false;

    public MainWindow(GameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Il controller non può essere null.");
        }
        this.controller = controller;
    }

    public Parent createView() {
        registerActions();

        Label title = new Label("Tower of Self");
        title.getStyleClass().add("title");

        descriptionLabel.setWrapText(true);
        resultLabel.setWrapText(true);

        playerHealthBar.setMaxWidth(Double.MAX_VALUE);
        enemyHealthBar.setMaxWidth(Double.MAX_VALUE);
        playerHealthBar.getStyleClass().add("player-health");
        enemyHealthBar.getStyleClass().add("enemy-health");

        inventoryView.setPrefHeight(130);

        VBox floorPanel = panel("Piano", floorLabel, eventLabel, descriptionLabel);
        VBox playerPanel = panel("Giocatore", playerHealthLabel, playerHealthBar);
        enemyPanel = panel("Nemico", enemyHealthLabel, enemyHealthBar);

        HBox statusRow = statusRow(playerPanel, enemyPanel);

        mainActionsPanel = panel("Azioni principali", new HBox(10, eventButton));
        combatPanel = panel("Azioni di combattimento",
                new HBox(10, attackButton, confrontButton, useItemButton, escapeButton));

        VBox content = new VBox(
                15,
                title,
                floorPanel,
                statusRow,
                panel("Inventario", inventoryView),
                panel("Messaggi", resultLabel),
                mainActionsPanel,
                combatPanel,
                panel("Salvataggio", new HBox(10, saveButton, loadButton))
        );

        content.setPadding(new Insets(20));
        content.getStyleClass().add("content");

        updateView();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("root-scroll");

        return scroll;
    }

    private void registerActions() {
        eventButton.setOnAction(event -> onExecuteEvent());
        attackButton.setOnAction(event -> onCombatAction(CombatAction.ATTACK));
        confrontButton.setOnAction(event -> onConfront());
        useItemButton.setOnAction(event -> onCombatAction(CombatAction.USE_ITEM));
        escapeButton.setOnAction(event -> onCombatAction(CombatAction.ESCAPE));
        saveButton.setOnAction(event -> onSave());
        loadButton.setOnAction(event -> onLoad());
    }

    private VBox panel(String header, Node... content) {
        Label sectionHeader = new Label(header);
        sectionHeader.getStyleClass().add("section-header");

        VBox box = new VBox(8, sectionHeader);
        box.getStyleClass().add("panel");
        box.getChildren().addAll(content);

        return box;
    }

    private HBox statusRow(VBox playerPanel, VBox enemyPanel) {
        playerPanel.setMaxWidth(Double.MAX_VALUE);
        enemyPanel.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(playerPanel, Priority.ALWAYS);
        HBox.setHgrow(enemyPanel, Priority.ALWAYS);

        return new HBox(15, playerPanel, enemyPanel);
    }

    private void onExecuteEvent() {
        if (controller.isCurrentEventExecuted()) {
            resultLabel.setText("Hai già affrontato l'evento di questo piano.");
            return;
        }

        Floor floor = controller.getCurrentFloor();

        if (floor.getEvent() instanceof DialogueEvent dialogueEvent) {
            handleDialogue(dialogueEvent);
            return;
        }

        if (floor.getEvent() instanceof CombatEvent && floor.getNumber() == 1) {
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "La Paura del Fallimento",
                    """
                    Una figura emerge lentamente dall'ombra.

                    Ha il tuo volto.

                    "E se non fossi abbastanza?"

                    Il confronto ha inizio.
                    """
            );
        }

        resultLabel.setText(controller.executeStandardEvent());
        refresh();
    }

    private void handleDialogue(DialogueEvent dialogueEvent) {
        controller.beginDialogue();

        Optional<DialogueChoice> choice = askDialogueChoice(dialogueEvent);

        if (choice.isEmpty()) {
            controller.abortDialogue();
            resultLabel.setText("Hai esitato davanti alla scelta.");
        } else {
            resultLabel.setText(controller.resolveDialogueChoice(choice.get()));
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

    private void onConfront() {
        ChallengeQuestion question = controller.startConfrontation();
        Optional<Integer> answer = askConfrontation(question);

        if (answer.isEmpty()) {
            resultLabel.setText("Eviti lo sguardo della Paura. Il combattimento continua.");
        } else {
            resultLabel.setText(controller.resolveConfrontation(answer.get()));
        }

        refresh();
    }

    private Optional<Integer> askConfrontation(ChallengeQuestion question) {
        List<String> answers = question.answers();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(answers.get(0), answers);
        dialog.setTitle("Confronto");
        dialog.setHeaderText(question.question());
        dialog.setContentText("Cosa rispondi?");

        return dialog.showAndWait().map(answers::indexOf);
    }

    private void onSave() {
        resultLabel.setText(controller.saveGame());
    }

    private void onLoad() {
        String message = controller.loadGame();
        gameEndShown = false;
        updateView();
        resultLabel.setText(message + "\n\n" + getInitialInstruction());
    }

    private void updateView() {
        Floor floor = controller.getCurrentFloor();

        floorLabel.setText("Piano: " + floor);
        eventLabel.setText(getNarrativeEventLabel());
        descriptionLabel.setText(floor.getDescription());

        eventButton.setText(getEventButtonText());
        resultLabel.setText(getInitialInstruction());

        refresh();
    }

    private String getNarrativeEventLabel() {
        Floor floor = controller.getCurrentFloor();

        if (floor.getEvent() instanceof CombatEvent) {
            return "Prova: confronto interiore";
        }
        if (floor.getEvent() instanceof DialogueEvent) {
            return "Prova: scelta interiore";
        }
        if (floor.getEvent() instanceof ItemEvent) {
            return "Prova: scoperta";
        }

        return "Prova della Torre";
    }

    private String getEventButtonText() {
        return "Affronta la Paura";
    }

    private String getInitialInstruction() {
        return """
                La Torre è silenziosa.

                Davanti a te senti una presenza.
                Qualcosa ti osserva dall'ombra.

                Premi "Affronta la Paura" per iniziare la prova.
                """;
    }

    private void refresh() {
        updateStatus();
        updateInventory();
        updateActions();
        checkGameEnd();
    }

    private void updateStatus() {
        int current = controller.getPlayerCurrentHealth();
        int max = controller.getPlayerMaxHealth();

        playerHealthLabel.setText("Lucidità: " + current + "/" + max);
        playerHealthBar.setProgress(healthRatio(current, max));

        Enemy enemy = controller.getCurrentEnemy();
        boolean hasEnemy = enemy != null;

        enemyPanel.setVisible(hasEnemy);
        enemyPanel.setManaged(hasEnemy);

        if (hasEnemy) {
            int enemyCurrent = enemy.getStats().getCurrentHealth();
            int enemyMax = enemy.getStats().getMaxHealth();

            enemyHealthLabel.setText(enemy.getName() + " — Intensità: " + enemyCurrent + "/" + enemyMax);
            enemyHealthBar.setProgress(healthRatio(enemyCurrent, enemyMax));
        } else {
            enemyHealthBar.setProgress(0);
        }
    }

    private double healthRatio(int current, int max) {
        return max <= 0 ? 0 : (double) current / max;
    }

    private void updateInventory() {
        List<String> entries = new ArrayList<>();

        for (Item item : controller.getInventoryItems()) {
            entries.add(item.getName() + " — " + item.getType());
        }

        inventoryView.getItems().setAll(entries);
    }

    private void updateActions() {
        GameState state = controller.getState();

        boolean exploring = state == GameState.EXPLORING;
        boolean combat = state == GameState.COMBAT;
        boolean terminal = state == GameState.VICTORY || state == GameState.GAME_OVER;

        boolean canRunEvent = exploring && !controller.isCurrentEventExecuted();

        setManagedVisible(eventButton, canRunEvent);
        setManagedVisible(mainActionsPanel, canRunEvent);

        setManagedVisible(attackButton, combat);
        setManagedVisible(confrontButton, combat && controller.isConfrontationAvailable());
        setManagedVisible(useItemButton, combat);
        setManagedVisible(escapeButton, combat);
        setManagedVisible(combatPanel, combat);

        saveButton.setDisable(terminal);
    }

    private void setManagedVisible(Node node, boolean value) {
        node.setVisible(value);
        node.setManaged(value);
    }

    private void checkGameEnd() {
        if (gameEndShown) {
            return;
        }

        if (controller.getState() == GameState.GAME_OVER) {
            gameEndShown = true;
            showAlert(
                    Alert.AlertType.ERROR,
                    "Game Over",
                    "La Torre non ti ha sconfitto per sempre. Ogni caduta può diventare un nuovo inizio."
            );
        } else if (controller.getState() == GameState.VICTORY) {
            gameEndShown = true;
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "La Paura si dissolve",
                    "La Paura si dissolve. Non hai cancellato il fallimento: "
                            + "hai imparato a non farti definire da esso."
            );
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}