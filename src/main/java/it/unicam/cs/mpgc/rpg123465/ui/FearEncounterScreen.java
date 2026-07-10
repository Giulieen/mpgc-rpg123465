package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import it.unicam.cs.mpgc.rpg123465.fear.Attitude;
import it.unicam.cs.mpgc.rpg123465.fear.FearChoice;
import it.unicam.cs.mpgc.rpg123465.fear.FearEncounter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Schermata di un incontro con una paura.
 * <p>
 * Mostra la scena (sfondo + nebbia), la situazione e le reazioni possibili.
 * Alla scelta applica gli effetti su Lucidità e Stress, racconta la
 * conseguenza e ricorda che Chimeris ha memorizzato l'atteggiamento, poi
 * lascia proseguire.
 */
public class FearEncounterScreen {

    private static final int MAX_LUCIDITA = 100;
    private static final int MAX_STRESS = 100;

    private final FearEncounter encounter;
    private final GameController controller;
    private final Runnable onComplete;
    private final StackPane root = new StackPane();
    private final HeaderBar header;

    private Region headerView;
    private ImageView background;
    private Rectangle dark;
    private Region fog;
    private PauseTransition tremorTimer;

    private int lucidita = MAX_LUCIDITA;
    private int stress;

    public FearEncounterScreen(FearEncounter encounter,
                               GameController controller,
                               Runnable onComplete) {
        if (encounter == null || controller == null || onComplete == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }
        this.encounter = encounter;
        this.controller = controller;
        this.onComplete = onComplete;
        this.header = new HeaderBar(controller.getPlayerName());
        this.stress = encounter.initialStress();
    }

    public Parent createView() {
        root.getStyleClass().add("fear-root");

        startAmbience();

        background = coverImage(encounter.backgroundResource());
        dark = darkOverlay();
        fog = new FogOverlay(0.5).createView();

        headerView = header.createView();
        header.setVita(controller.getPlayerCurrentHealth(), controller.getPlayerMaxHealth());
        header.setProva(encounter.title());
        updateHeader();

        startTremors();

        showCenter(choicesView());
        return root;
    }

    /** Sottofondo della stanza: l'ambiente cupo, le candele, i topi che sciamano. */
    private void startAmbience() {
        Sound.stopAll();
        Sound.loop("/audio/ambience-topi.mp3", 0.35);
        Sound.loop("/audio/fire-crackle.mp3", 0.22);
        Sound.play("/audio/scurrying.mp3", 0.5);

        // Ogni tanto un topo squittisce nel buio: la stanza è viva.
        Sound.occasional("/audio/squeak.mp3", 0.4, 5, 14);
    }

    // --- Tremore della stanza ----------------------------------------------

    /**
     * Ogni tanto la Torre trema. Si muove soltanto lo sfondo: testo, pulsanti
     * e barra di stato restano fermi.
     */
    private void startTremors() {
        // Un filo di ingrandimento, così la scossa non scopre i bordi neri.
        background.setScaleX(1.04);
        background.setScaleY(1.04);
        scheduleTremor();
    }

    private void scheduleTremor() {
        double delay = 10 + Math.random() * 14;   // fra 10 e 24 secondi

        tremorTimer = new PauseTransition(Duration.seconds(delay));
        tremorTimer.setOnFinished(event -> {
            tremor(1.0);
            scheduleTremor();
        });
        tremorTimer.play();
    }

    /**
     * Una sequenza di scosse che accompagna tutto il rumble: cresce fino al
     * suo picco (~3s) e poi si spegne con lui. La Torre trema a lungo, non
     * una volta sola.
     */
    private void tremorBurst() {
        double[][] beats = {
                {0.6, 0.7},
                {1.4, 1.1},
                {2.2, 1.5},
                {3.0, 1.9},   // il picco del rumble
                {3.7, 1.3},
                {4.4, 0.8}
        };

        for (double[] beat : beats) {
            tremorAfter(beat[0], beat[1]);
        }
    }

    /** Una scossa ritardata, per sincronizzarla con un suono. */
    private void tremorAfter(double seconds, double strength) {
        PauseTransition wait = new PauseTransition(Duration.seconds(seconds));
        wait.setOnFinished(event -> tremor(strength));
        wait.play();
    }

    /**
     * Una scossa che si smorza, come un tremito vero.
     *
     * @param strength moltiplicatore dell'ampiezza (1 = scossa d'ambiente)
     */
    private void tremor(double strength) {
        Timeline shake = new Timeline();
        int steps = 10;

        for (int i = 1; i <= steps; i++) {
            double decay = 1.0 - (double) i / steps;
            double offsetX = (Math.random() - 0.5) * 16 * strength * decay;
            double offsetY = (Math.random() - 0.5) * 11 * strength * decay;

            shake.getKeyFrames().add(new KeyFrame(Duration.millis(i * 45),
                    new KeyValue(background.translateXProperty(), offsetX),
                    new KeyValue(background.translateYProperty(), offsetY)));
        }

        shake.getKeyFrames().add(new KeyFrame(Duration.millis((steps + 1) * 45),
                new KeyValue(background.translateXProperty(), 0),
                new KeyValue(background.translateYProperty(), 0)));

        shake.play();
    }

    private void stopTremors() {
        if (tremorTimer != null) {
            tremorTimer.stop();
            tremorTimer = null;
        }
    }

    // --- Livelli ------------------------------------------------------------

    private void showCenter(Node center) {
        root.getChildren().setAll(background, dark, fog, center, headerView);

        StackPane.setAlignment(center, Pos.CENTER);
        StackPane.setMargin(center, new Insets(90, 40, 60, 40));
        StackPane.setAlignment(headerView, Pos.TOP_CENTER);
    }

    private void updateHeader() {
        header.setLucidita(lucidita, MAX_LUCIDITA);
        header.setStress(stress, MAX_STRESS);
    }

    // --- Contenuto ----------------------------------------------------------

    private VBox choicesView() {
        Label title = new Label(encounter.title());
        title.getStyleClass().add("fear-title");

        Label situation = paragraph(encounter.situation());

        VBox buttons = new VBox(12);
        buttons.setAlignment(Pos.CENTER);
        for (FearChoice choice : encounter.choices()) {
            Button button = new Button(choice.label());
            button.getStyleClass().add("menu-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> choose(choice));
            buttons.getChildren().add(button);
        }

        return panel(title, situation, buttons);
    }

    private void choose(FearChoice choice) {
        for (SoundCue cue : choice.sounds()) {
            Sound.play(cue);
        }
        if (choice.shakesRoom()) {
            tremorBurst();
        }

        lucidita = clamp(lucidita + choice.lucidityDelta());
        stress = clamp(stress + choice.stressDelta());
        updateHeader();

        Label reaction = paragraph(choice.reaction());

        Label effects = new Label(describeEffects(choice));
        effects.getStyleClass().add("fear-effects");

        Label memory = new Label(chimerisMemory(choice.attitude()));
        memory.setWrapText(true);
        memory.setMaxWidth(680);
        memory.getStyleClass().add("fear-memory");

        Button next = new Button("Continua");
        next.getStyleClass().add("menu-button");
        next.setOnAction(event -> {
            stopTremors();
            onComplete.run();
        });

        showCenter(panel(reaction, effects, memory, next));
    }

    /** Rende leggibile l'effetto della scelta, es. "Lucidità ±0 · Stress −18". */
    private String describeEffects(FearChoice choice) {
        return "Lucidità " + signed(choice.lucidityDelta())
                + "     ·     Stress " + signed(choice.stressDelta());
    }

    private String signed(int delta) {
        if (delta > 0) {
            return "+" + delta;
        }
        if (delta < 0) {
            return "−" + Math.abs(delta);
        }
        return "±0";
    }

    private String chimerisMemory(Attitude attitude) {
        return switch (attitude) {
            case COLPISCI -> "Chimeris ha visto la tua violenza. Non la dimenticherà.";
            case CONTIENI -> "Chimeris ti ha visto scacciare ciò che temi. Tornerà a grattare.";
            case TOLLERI -> "Chimeris ti ha visto resistere. La paura perde un po' della sua presa.";
            case ACCOGLI -> "Chimeris ti ha visto accogliere. Questa paura non lo nutrirà più.";
        };
    }

    // --- Elementi di base ---------------------------------------------------

    private VBox panel(Node... children) {
        VBox box = new VBox(22, children);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(760);
        box.getStyleClass().add("fear-panel");
        return box;
    }

    private Label paragraph(String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(720);
        label.getStyleClass().add("fear-text");
        return label;
    }

    private ImageView coverImage(String resourcePath) {
        ImageView view = new ImageView(new Image(getClass().getResourceAsStream(resourcePath)));
        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(root.heightProperty());
        return view;
    }

    private Rectangle darkOverlay() {
        Rectangle rectangle = new Rectangle();
        rectangle.getStyleClass().add("fear-dark");
        rectangle.widthProperty().bind(root.widthProperty());
        rectangle.heightProperty().bind(root.heightProperty());
        return rectangle;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(MAX_LUCIDITA, value));
    }
}
