package it.unicam.cs.mpgc.rpg123465.view.floors.rats;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.floors.rats.FearChoice;
import it.unicam.cs.mpgc.rpg123465.model.floors.rats.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.view.FloorScene;
import it.unicam.cs.mpgc.rpg123465.view.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.view.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.view.components.FogOverlay;
import it.unicam.cs.mpgc.rpg123465.view.components.SceneFx;
import it.unicam.cs.mpgc.rpg123465.view.components.ScreenShake;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

/**
 * Prima fase del Piano I: il dilemma.
 *
 * <p>
 * Mostra una domanda "Preferiresti" con due sole risposte e registra il tratto
 * associato, che resta invisibile al giocatore. Data la risposta, introduce la
 * prova e cede il passo alla stanza dei Topi: il dilemma vale una sola volta,
 * mentre la prova si puo' ripetere.
 */
public class FearEncounterScreen implements FloorScene {

    private final FearEncounter encounter;
    private final GameController controller;
    private final MindState mind;
    private final StackPane root = new StackPane();
    private final HeaderBar header;
    private final Runnable onExit;

    private Consumer<SceneOutcome> onFinished;

    private Region headerView;
    private ImageView background;
    private Rectangle dark;
    private Region fog;
    private ScreenShake shake;

    private boolean answered;

    public FearEncounterScreen(
            FearEncounter encounter,
            GameController controller,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (encounter == null || controller == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.encounter = encounter;
        this.controller = controller;
        this.mind = controller.getMind();
        this.onExit = onExit;

        this.header = new HeaderBar(
                controller.getPlayerName(),
                onSave == null
                        ? null
                        // Il dilemma non ha nulla da fermare: niente da riprendere.
                        : () -> onSave.accept(null),
                onExit == null
                        ? null
                        : this::exitLevel
        );
    }

    @Override
    public Parent createView(Consumer<SceneOutcome> onFinished) {
        if (onFinished == null) {
            throw new IllegalArgumentException("Il callback di fine scena non può essere null.");
        }

        this.onFinished = onFinished;

        root.getStyleClass().add("fear-root");

        startAmbience();

        background = SceneFx.cover(root, encounter.backgroundResource());

        dark = darkOverlay();

        fog = new FogOverlay(0.5).createView();

        headerView = header.createView();

        header.setProva(encounter.title());

        updateHeader();

        shake = new ScreenShake(background);

        shake.startAmbient();

        showCenter(dilemmaView());

        return root;
    }

    private void startAmbience() {
        Sound.stopAll();

        Sound.loop("/audio/ambience-topi.mp3", 0.35);

        Sound.loop("/audio/fire-crackle.mp3", 0.22);

        Sound.play("/audio/scurrying.mp3", 0.5);

        Sound.occasional("/audio/squeak.mp3", 0.4, 5, 14);
    }

    private void showCenter(Node center) {
        root.getChildren().setAll(background, dark, fog, center, headerView);

        StackPane.setAlignment(center, Pos.CENTER);

        StackPane.setMargin(center, new Insets(90, 40, 60, 40));

        StackPane.setAlignment(headerView, Pos.TOP_CENTER);
    }

    private void updateHeader() {
        header.setTentativi(controller.getRemainingAttempts(), controller.getMaxAttempts());
    }

    private VBox dilemmaView() {
        Label title = new Label(encounter.title());

        title.getStyleClass().add("fear-title");

        Label question = SceneFx.paragraph(encounter.situation());

        VBox buttons = new VBox(12);

        buttons.setAlignment(Pos.CENTER);

        for (FearChoice choice : encounter.choices()) {
            Button button = new Button(choice.label());

            button.getStyleClass().add("menu-button");

            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(event -> choose(choice));

            buttons.getChildren().add(button);
        }

        return panel(title, question, buttons);
    }

    private void choose(FearChoice choice) {
        if (answered) {
            return;
        }

        answered = true;

        /*
         * Il giocatore non vede il tratto associato.
         * La risposta contribuisce soltanto al profilo finale.
         */
        mind.registerTrait(choice.trait());

        updateHeader();
        showBriefing();
    }

    /**
     * Annuncia la prova che segue.
     *
     * La scelta e' gia' stata registrata: da qui in poi il Piano I non tocca
     * piu' il profilo, qualunque cosa succeda nel labirinto.
     */
    private void showBriefing() {
        Label heading = new Label("La Torre ha raccolto la tua scelta.");

        heading.getStyleClass().add("fear-title");

        Button enter = new Button("Entra nel labirinto");

        enter.getStyleClass().add("menu-button");

        enter.setOnAction(event -> { cleanup(); onFinished.accept(SceneOutcome.NEXT); });

        showCenter(
                panel(
                        heading,
                        SceneFx.paragraph(
                                "Un rumore crescente sale dalle pareti: "
                                        + "le tane si stanno aprendo.\n\n"
                                        + "Non lasciare che i topi raggiungano le uscite."
                        ),
                        enter
                )
        );
    }

    private VBox panel(Node... children) {
        VBox box = new VBox(22, children);

        box.setAlignment(Pos.CENTER);

        box.setMaxWidth(760);

        box.getStyleClass().add("fear-panel");

        return box;
    }

    private Rectangle darkOverlay() {
        Rectangle rectangle = new Rectangle();

        rectangle.getStyleClass().add("fear-dark");

        rectangle.widthProperty().bind(root.widthProperty());

        rectangle.heightProperty().bind(root.heightProperty());

        return rectangle;
    }

    private void exitLevel() {
        cleanup();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void cleanup() {
        if (shake != null) {
            shake.stop();
        }

        Sound.stopAll();
    }
}
