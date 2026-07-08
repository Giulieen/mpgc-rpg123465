package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Schermata di introduzione narrativa mostrata dopo l'avvio di una nuova
 * partita e prima dell'ingresso nella torre.
 */
public class IntroScreen {

    private static final String INTRO_TEXT =
            "La Torre appare davanti a te.\n"
                    + "Ogni piano custodisce una parte della tua mente.\n"
                    + "Per salire dovrai affrontare paura, rabbia, ansia, "
                    + "solitudine, speranza e il tuo Alter Ego.";

    private final Runnable onEnter;

    /**
     * Crea la schermata introduttiva.
     *
     * @param onEnter azione da eseguire quando il giocatore entra nella torre
     */
    public IntroScreen(Runnable onEnter) {
        if (onEnter == null) {
            throw new IllegalArgumentException("Il callback non può essere null.");
        }

        this.onEnter = onEnter;
    }

    /**
     * @return radice della schermata introduttiva
     */
    public Parent createView() {
        Label title = new Label("Tower of Self");
        title.getStyleClass().add("title");

        Label intro = new Label(INTRO_TEXT);
        intro.setWrapText(true);
        intro.getStyleClass().add("narrative");

        Button enterButton = new Button("Entra nella torre");
        enterButton.setOnAction(event -> onEnter.run());

        VBox root = new VBox(24, title, intro, enterButton);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("content");
        return root;
    }
}
