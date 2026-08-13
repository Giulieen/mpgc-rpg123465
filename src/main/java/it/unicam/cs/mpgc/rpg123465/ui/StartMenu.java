package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.ui.support.FogOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StartMenu {

    private static final int MIN_NAME_LENGTH = 4;
    private static final int MAX_NAME_LENGTH = 20;

    private final Consumer<String> onNewGame;
    private final Runnable onLoadGame;

    /**
     * Dice se un nome è già stato usato su questa installazione.
     *
     * Il menu non sa dove sia scritto l'elenco: gli basta poter chiedere.
     */
    private final Predicate<String> nameTaken;

    private final TextField nameField =
            new TextField();

    private final Label nameError =
            new Label();

    public StartMenu(
            Consumer<String> onNewGame,
            Runnable onLoadGame,
            Predicate<String> nameTaken
    ) {
        if (onNewGame == null
                || onLoadGame == null
                || nameTaken == null) {

            throw new IllegalArgumentException(
                    "I callback non possono essere null."
            );
        }

        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
        this.nameTaken = nameTaken;
    }

    public Parent createView() {
        StackPane root =
                new StackPane();

        root.getStyleClass().add(
                "menu-root"
        );

        Rectangle darkOverlay =
                new Rectangle();

        darkOverlay.getStyleClass().add(
                "menu-dark-overlay"
        );

        darkOverlay.widthProperty().bind(
                root.widthProperty()
        );

        darkOverlay.heightProperty().bind(
                root.heightProperty()
        );

        Region fog =
                new FogOverlay()
                        .createView();

        VBox card =
                createMenuCard();

        StackPane.setAlignment(
                card,
                Pos.CENTER
        );

        root.getChildren().addAll(
                darkOverlay,
                fog,
                card
        );

        return root;
    }

    private VBox createMenuCard() {
        Label title =
                new Label(
                        "TOWER OF SELF"
                );

        title.getStyleClass().add(
                "menu-title"
        );

        Label subtitle =
                new Label("""
                        La Torre osserva le tue scelte.
                        Solo salendo scoprirai cosa raccontano di te.
                        """);

        subtitle.getStyleClass().add(
                "menu-subtitle"
        );

        Label description =
                new Label(
                        "Come ti chiami?"
                );

        description.getStyleClass().add(
                "narrative"
        );

        configureNameField();
        configureErrorLabel();

        Button newGameButton =
                new Button(
                        "Nuova partita"
                );

        Button loadButton =
                new Button(
                        "Carica partita"
                );

        newGameButton.getStyleClass().add(
                "menu-button"
        );

        loadButton.getStyleClass().add(
                "menu-button"
        );

        newGameButton.setOnAction(
                event -> startNewGame()
        );

        nameField.setOnAction(
                event -> startNewGame()
        );

        loadButton.setOnAction(
                event -> onLoadGame.run()
        );

        HBox buttons =
                new HBox(
                        22,
                        newGameButton,
                        loadButton
                );

        buttons.setAlignment(
                Pos.CENTER
        );

        VBox menu =
                new VBox(
                        18,
                        title,
                        subtitle,
                        description,
                        nameField,
                        nameError,
                        buttons
                );

        menu.setAlignment(
                Pos.CENTER
        );

        menu.setPadding(
                new Insets(40)
        );

        menu.setMaxWidth(
                820
        );

        menu.setMaxHeight(
                620
        );

        menu.getStyleClass().add(
                "menu-card"
        );

        return menu;
    }

    private void configureNameField() {
        nameField.setPromptText(
                "Nome del giocatore"
        );

        nameField.setMaxWidth(
                280
        );

        nameField.getStyleClass().add(
                "name-field"
        );

        /*
         * Impedisce di superare i 20 caratteri.
         */
        nameField.setTextFormatter(
                new TextFormatter<String>(
                        change ->
                                change
                                        .getControlNewText()
                                        .length()
                                        <= MAX_NAME_LENGTH
                                        ? change
                                        : null
                )
        );

        /*
         * Appena il giocatore modifica il testo,
         * rimuoviamo l'eventuale errore precedente.
         */
        nameField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideNameError()
        );
    }

    private void configureErrorLabel() {
        nameError.setWrapText(
                true
        );

        nameError.setMaxWidth(
                320
        );

        nameError.setAlignment(
                Pos.CENTER
        );

        nameError.setStyle(
                "-fx-text-fill: #d97979;"
        );

        nameError.setVisible(
                false
        );

        nameError.setManaged(
                false
        );
    }

    private void startNewGame() {
        String name =
                nameField
                        .getText()
                        .trim();

        String validationError =
                validateName(
                        name
                );

        if (validationError != null) {
            showNameError(
                    validationError
            );

            return;
        }

        hideNameError();

        /*
         * Utilizziamo il nome già ripulito dagli
         * spazi iniziali e finali.
         */
        onNewGame.accept(
                name
        );
    }

    private String validateName(
            String name
    ) {
        if (name.isBlank()) {
            return "Inserisci un nome.";
        }

        if (name.length()
                < MIN_NAME_LENGTH) {

            return "Il nome deve contenere almeno "
                    + MIN_NAME_LENGTH
                    + " caratteri.";
        }

        /*
         * Sono ammessi:
         * - lettere, comprese quelle accentate;
         * - spazi;
         * - apostrofo;
         * - trattino.
         */
        if (!name.matches(
                "[\\p{L}][\\p{L} '\\-]*"
        )) {
            return "Il nome può contenere solo lettere, spazi, apostrofi e trattini.";
        }

        /*
         * Il confronto ignora maiuscole e spazi ai bordi: lo decide il registro,
         * che è l'unico a sapere quali nomi sono già stati usati.
         */
        if (nameTaken.test(name)) {
            return "Questo nome è già stato usato. Scegline un altro.";
        }

        return null;
    }

    private void showNameError(
            String message
    ) {
        nameError.setText(
                message
        );

        nameError.setManaged(
                true
        );

        nameError.setVisible(
                true
        );
    }

    private void hideNameError() {
        nameError.setVisible(
                false
        );

        nameError.setManaged(
                false
        );
    }

}
