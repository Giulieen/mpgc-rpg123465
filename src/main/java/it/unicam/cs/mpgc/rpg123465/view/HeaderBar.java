package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Barra di stato sempre visibile in cima alle schermate di gioco: nome del
 * giocatore, tentativi rimasti e prova in corso, più i comandi per salvare e
 * uscire.
 */
public class HeaderBar {

    private static final Color ICON_COLOR = Color.web("#d8c07a");
    private static final Color SPENT_COLOR = Color.rgb(255, 255, 255, 0.18);
    private static final int ICON_SIZE = 20;
    private static final int ATTEMPT_ICON_SIZE = 18;

    private final Label nameValue = new Label();
    private final Label provaValue = new Label();

    /**
     * Le losanghe dei tentativi: accese quelle disponibili, spente le altre.
     * Mostrarli come simboli invece che come numero rende leggibile a colpo
     * d'occhio quanto manca alla fine della prova.
     */
    private final HBox attemptIcons = new HBox(6);

    private final Runnable onSave;
    private final Runnable onExit;

    /**
     * @param playerName nome del giocatore mostrato nella barra
     */
    public HeaderBar(String playerName) {
        this(playerName, null, null);
    }

    /**
     * @param playerName nome del giocatore mostrato nella barra
     * @param onSave azione da eseguire al salvataggio, oppure {@code null}
     */
    public HeaderBar(String playerName, Runnable onSave) {
        this(playerName, onSave, null);
    }

    /**
     * @param playerName nome del giocatore mostrato nella barra
     * @param onSave azione da eseguire al salvataggio, oppure {@code null}
     * @param onExit azione per uscire dal livello, oppure {@code null}
     */
    public HeaderBar(String playerName, Runnable onSave, Runnable onExit) {
        nameValue.setText(playerName);
        this.onSave = onSave;
        this.onExit = onExit;
    }

    /**
     * @return la barra, da allineare in cima alla schermata
     */
    public Region createView() {
        attemptIcons.setAlignment(Pos.CENTER_LEFT);

        HBox bar = new HBox(
                30,
                item("mdi2a-account", nameValue),
                attemptsGroup(),
                spacer(),
                item("mdi2b-book-open-variant", provaValue)
        );

        if (onSave != null) {
            Button saveButton = new Button("Salva");
            saveButton.getStyleClass().add("header-save");
            saveButton.setOnAction(event -> onSave.run());
            bar.getChildren().add(saveButton);
        }

        if (onExit != null) {
            Button exitButton = new Button("Esci");
            exitButton.getStyleClass().add("header-save");
            exitButton.setOnAction(event -> onExit.run());
            bar.getChildren().add(exitButton);
        }

        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("header-bar");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        return bar;
    }

    /**
     * Aggiorna i tentativi mostrati.
     *
     * @param remaining tentativi ancora disponibili
     * @param max tentativi concessi all'ingresso del piano
     */
    public void setTentativi(int remaining, int max) {
        attemptIcons.getChildren().clear();

        for (int i = 0; i < max; i++) {
            FontIcon icon = new FontIcon("mdi2r-rhombus");
            icon.setIconSize(ATTEMPT_ICON_SIZE);
            icon.setIconColor(i < remaining ? ICON_COLOR : SPENT_COLOR);
            attemptIcons.getChildren().add(icon);
        }
    }

    public void setProva(String text) {
        provaValue.setText(text);
    }

    private HBox attemptsGroup() {
        Label caption = new Label("Tentativi");
        caption.getStyleClass().add("header-value");

        HBox box = new HBox(8, caption, attemptIcons);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private HBox item(String iconLiteral, Label value) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(ICON_SIZE);
        icon.setIconColor(ICON_COLOR);

        value.getStyleClass().add("header-value");

        HBox box = new HBox(8, icon, value);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}
