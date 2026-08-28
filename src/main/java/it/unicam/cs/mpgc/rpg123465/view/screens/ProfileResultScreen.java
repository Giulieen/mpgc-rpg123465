package it.unicam.cs.mpgc.rpg123465.view.screens;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.PlayerProfile;
import it.unicam.cs.mpgc.rpg123465.view.ProfilePortraits;
import it.unicam.cs.mpgc.rpg123465.view.components.FogOverlay;
import it.unicam.cs.mpgc.rpg123465.view.components.ScrollingBackground;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Schermata conclusiva della demo.
 *
 * Mostra il profilo emerso dalle scelte compiute durante la salita
 * senza rendere visibili i punteggi interni dei singoli tratti.
 */
public final class ProfileResultScreen {

    private final PlayerProfile profile;
    private final Runnable onBackToMenu;

    private final StackPane root = new StackPane();

    public ProfileResultScreen(MindState mind, Runnable onBackToMenu) {
        if (mind == null || onBackToMenu == null) {

            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.profile = mind.profile();

        this.onBackToMenu = onBackToMenu;
    }

    /**
     * @return radice della schermata finale
     */
    public Parent createView() {
        root.getStyleClass().add("fear-root");

        Sound.stopAll();

        Sound.loop("/audio/ambience-night.mp3", 0.25);

        Region background =
                new ScrollingBackground("/images/bg/forest.jpg").createView();

        Region fog = new FogOverlay(0.4).createView();

        root.getChildren().setAll(background, darkOverlay(), fog, content());

        return root;
    }

    private VBox content() {
        Label direction = new Label("IL TUO PROFILO STA ANDANDO VERSO");

        direction.getStyleClass().add("fear-memory");

        Label name = new Label(profile.getName());

        name.getStyleClass().add("fear-title");

        Label description = paragraph(profile.getDescription());

        VBox words = new VBox(14, direction, name, description);

        words.setAlignment(Pos.CENTER);

        words.setMaxWidth(460);

        /*
         * Ritratto a sinistra e parole a destra: il profilo si legge come una
         * carta, non come un elenco. La figura è alta quanto il testo che
         * accompagna, così la card resta equilibrata anche con le descrizioni
         * più lunghe.
         */
        HBox card = new HBox(34, portrait(), words);

        card.setAlignment(Pos.CENTER);

        Label demo =
                paragraph(
                        "Grazie per aver giocato la demo di Tower of Self.\n"
                                + "Completa la Torre per scoprire il profilo "
                                + "che ti rispecchia davvero."
                );

        demo.getStyleClass().add("fear-memory");

        Button menu = new Button("Torna al menu");

        menu.getStyleClass().add("menu-button");

        menu.setOnAction(event -> { Sound.stopAll(); onBackToMenu.run(); });

        VBox box = new VBox(26, card, demo, menu);

        box.setAlignment(Pos.CENTER);

        box.setMaxWidth(900);

        box.getStyleClass().add("fear-panel");

        return box;
    }

    private ImageView portrait() {
        ImageView view =
                new ImageView(new Image(
                        getClass().getResourceAsStream(ProfilePortraits.resourceOf(profile))));

        /*
         * Riquadro, non altezza fissa: le figure hanno proporzioni molto
         * diverse — c'è chi sta in piedi e chi cammina a quattro zampe — e
         * imporre l'altezza renderebbe le seconde larghe il doppio.
         */
        view.setPreserveRatio(true);
        view.setFitWidth(300);
        view.setFitHeight(340);
        view.setSmooth(true);

        return view;
    }

    private Label paragraph(String content) {
        Label label = new Label(content);

        label.setWrapText(true);

        label.setMaxWidth(720);

        label.getStyleClass().add("fear-text");

        return label;
    }

    private Node darkOverlay() {
        Rectangle rectangle = new Rectangle();

        rectangle.getStyleClass().add("fear-dark");

        rectangle.widthProperty().bind(root.widthProperty());

        rectangle.heightProperty().bind(root.heightProperty());

        return rectangle;
    }
}
