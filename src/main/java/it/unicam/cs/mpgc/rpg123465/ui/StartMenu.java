package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Schermata iniziale del gioco: presenta il titolo, una breve introduzione
 * narrativa, il campo per il nome del giocatore e i comandi per iniziare o
 * caricare una partita. Non contiene logica di gioco: delega le azioni ai
 * callback ricevuti dal navigatore delle schermate.
 */
public class StartMenu {

    private static final String DESCRIPTION =
            "Una torre si erge dove finiscono i pensieri. "
                    + "Ogni piano è una parte di te che attende di essere ascoltata. "
                    + "Sali, se ne hai il coraggio.";

    private final Consumer<String> onNewGame;
    private final Runnable onLoadGame;
    private final TextField nameField = new TextField();

    /**
     * Crea la schermata iniziale.
     *
     * @param onNewGame azione da eseguire con il nome inserito alla nuova partita
     * @param onLoadGame azione da eseguire per caricare una partita
     */
    public StartMenu(Consumer<String> onNewGame, Runnable onLoadGame) {
        if (onNewGame == null || onLoadGame == null) {
            throw new IllegalArgumentException("I callback non possono essere null.");
        }

        this.onNewGame = onNewGame;
        this.onLoadGame = onLoadGame;
    }

    /**
     * @return radice della schermata iniziale
     */
    public Parent createView() {
        Label title = new Label("Tower of Self");
        title.getStyleClass().add("title");

        Label description = new Label(DESCRIPTION);
        description.setWrapText(true);
        description.getStyleClass().add("narrative");

        nameField.setPromptText("Il tuo nome (predefinito: Viaggiatore)");

        Button newGameButton = new Button("Nuova partita");
        newGameButton.setOnAction(event -> onNewGame.accept(nameField.getText()));

        Button loadButton = new Button("Carica partita");
        loadButton.setOnAction(event -> onLoadGame.run());

        VBox root = new VBox(20,
                title,
                description,
                nameField,
                new HBox(10, newGameButton, loadButton)
        );
        root.setPadding(new Insets(40));
        root.getStyleClass().add("content");
        return root;
    }
}
