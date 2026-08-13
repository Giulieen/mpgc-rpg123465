package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.ui.support.FogOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.ScrollingBackground;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Introduzione narrativa mostrata all'avvio di una nuova partita: il bosco che
 * scorre, poi il cancello che il giocatore apre dopo aver raccolto la chiave.
 * Al termine invoca il callback che porta dentro la Torre.
 * <p>
 * Le animazioni a ciclo indefinito e gli ambienti sonori avviati qui non si
 * fermano da soli: vengono conservati e chiusi in {@link #cleanup()} prima di
 * cedere il controllo.
 */
public class IntroScreen {

    private static final Image GATE =
            new Image(IntroScreen.class.getResourceAsStream("/images/scenes/gate.jpg"));

    private final Runnable onEnter;
    private final StackPane root = new StackPane();

    /**
     * Animazioni a ciclo indefinito avviate dalla schermata.
     * <p>
     * Le transizioni finite si esauriscono da sole; queste no, e continuerebbero
     * a girare sul nodo della chiave anche dopo la sua rimozione dalla scena.
     */
    private final List<Animation> endlessAnimations = new ArrayList<>();

    /**
     * @param onEnter azione da eseguire quando il giocatore entra nella Torre
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
        root.getStyleClass().add("intro-root");
        showForestStep();
        return root;
    }

    // --- Passi dell'introduzione -------------------------------------------

    private void showForestStep() {
        // I due ambienti si sovrappongono: vento fra gli alberi e notte con i
        // grilli. Hanno durate diverse, così il loop non si ripete mai uguale.
        Sound.stopAll();
        Sound.loop("/audio/ambience-forest.mp3", 0.35);
        Sound.loop("/audio/ambience-night.mp3", 0.28);
        Sound.occasional("/audio/owl-hoot.mp3", 0.45, 10, 26);

        Region forest = new ScrollingBackground("/images/bg/forest.jpg", 35).createView();

        Label text = introText(
                "Non ricordi come sei arrivato in questo bosco.\n"
              + "Gli alberi sussurrano: qualcosa ti osserva tra i tronchi.\n"
              + "Più avanti, una Torre si erge nel buio. E ti chiama.");

        Button next = menuButton("Avvicìnati");
        next.setOnAction(event -> showGateStep());

        render(forest, panel(text, next), new FogOverlay());
    }

    private void showGateStep() {
        ImageView leftDoor = gateHalf(true);
        ImageView rightDoor = gateHalf(false);

        Rotate leftHinge = hinge(leftDoor, true);
        Rotate rightHinge = hinge(rightDoor, false);

        HBox doors = new HBox(leftDoor, rightDoor);
        doors.setAlignment(Pos.CENTER);

        Label text = introText(
                "La Torre mette alla prova le tue abilità e le tue scelte.\n"
              + "Quale profilo emergerà dalle tue risposte?\n\n"
              + "Prendi la chiave e apri il cancello.");

        Button open = menuButton("Apri il cancello");
        open.setDisable(true);

        VBox overlay = panel(text, open);
        open.setOnAction(event -> openGate(leftHinge, rightHinge, overlay));

        /*
         * Il testo non cambia: raccolta la chiave restano lo scatto del
         * lucchetto, la chiave che sparisce e il pulsante che si accende.
         * Bastano a dire che ha funzionato, senza rimpiazzare una schermata
         * che il giocatore sta ancora leggendo.
         */
        Node key = keyGleam();
        key.setOnMouseClicked(event -> {
            Sound.play("/audio/padlock-unlock.mp3", 0.8);

            // La chiave lascia la scena: le sue animazioni non hanno più un
            // bersaglio visibile e vanno chiuse qui, non alla fine della scena.
            stopEndlessAnimations();

            root.getChildren().remove(key);
            open.setDisable(false);
        });

        render(doors, overlay, new FogOverlay(0.4));
        StackPane.setAlignment(key, Pos.BOTTOM_LEFT);
        StackPane.setMargin(key, new Insets(0, 0, 120, 160));
        root.getChildren().add(key);
    }

    /** Una metà dell'immagine del cancello: il battente sinistro o destro. */
    private ImageView gateHalf(boolean left) {
        ImageView door = new ImageView(GATE);
        double width = GATE.getWidth();
        double height = GATE.getHeight();

        door.setViewport(new Rectangle2D(left ? 0 : width / 2, 0, width / 2, height));
        door.fitWidthProperty().bind(root.widthProperty().divide(2));
        door.fitHeightProperty().bind(root.heightProperty());
        return door;
    }

    /** Il cardine: il battente ruota attorno al proprio bordo esterno. */
    private Rotate hinge(ImageView door, boolean left) {
        Rotate rotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);

        if (left) {
            rotate.setPivotX(0);
        } else {
            rotate.pivotXProperty().bind(door.fitWidthProperty());
        }
        rotate.pivotYProperty().bind(door.fitHeightProperty().divide(2));

        door.getTransforms().add(rotate);
        return rotate;
    }

    /** I due battenti si spalancano cigolando, poi si entra nella Torre. */
    private void openGate(Rotate leftHinge, Rotate rightHinge, Node overlay) {
        FadeTransition hideText = new FadeTransition(Duration.millis(600), overlay);
        hideText.setToValue(0);
        hideText.play();

        // 2.4s: la durata del corpo del suono, prima che resti solo la coda.
        Timeline swing = new Timeline(new KeyFrame(Duration.millis(2400),
                new KeyValue(leftHinge.angleProperty(), -78, Interpolator.EASE_BOTH),
                new KeyValue(rightHinge.angleProperty(), 78, Interpolator.EASE_BOTH)));
        swing.setOnFinished(event -> enterTower());
        swing.play();

        Sound.play("/audio/gate-open.mp3", 0.75);
    }

    private void enterTower() {
        FadeTransition fade = new FadeTransition(Duration.millis(1000), root);
        fade.setFromValue(1);
        fade.setToValue(0);

        // Si chiude a dissolvenza finita e non prima, così il cigolio del
        // cancello e gli ambienti accompagnano l'uscita invece di troncarla.
        fade.setOnFinished(event -> {
            cleanup();
            onEnter.run();
        });

        fade.play();
    }

    /**
     * Chiude ciò che la schermata ha avviato e che non termina da solo.
     * <p>
     * È idempotente: le animazioni già ferme ignorano lo stop e la lista viene
     * svuotata alla prima chiamata.
     */
    private void cleanup() {
        stopEndlessAnimations();
        Sound.stopAll();
    }

    private void stopEndlessAnimations() {
        endlessAnimations.forEach(Animation::stop);
        endlessAnimations.clear();
    }

    // --- Costruzione dei livelli e degli elementi --------------------------

    private void render(Node background, Node overlay, FogOverlay fogOverlay) {
        Rectangle dark = new Rectangle();
        dark.getStyleClass().add("intro-dark");
        dark.widthProperty().bind(root.widthProperty());
        dark.heightProperty().bind(root.heightProperty());

        Region fog = fogOverlay.createView();

        root.getChildren().setAll(background, dark, fog, overlay);
    }

    private VBox panel(Label text, Button button) {
        VBox box = new VBox(22, text, button);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("intro-panel");
        return box;
    }

    private Label introText(String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(700);
        label.getStyleClass().add("intro-text");

        FadeTransition fadeIn = new FadeTransition(Duration.millis(900), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        return label;
    }

    private Button menuButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        return button;
    }

    /** La chiave nascosta tra le radici: fluttua e ondeggia, illuminata d'oro. */
    private Node keyGleam() {
        FontIcon key = new FontIcon("mdi2k-key");
        key.setIconSize(64);
        key.setIconColor(Color.web("#d8c07a"));   // stessa tinta delle icone della barra
        key.setCursor(Cursor.HAND);

        // Solo un accenno di alone, per farla notare nel buio.
        DropShadow glow = new DropShadow(10, Color.rgb(216, 192, 122, 0.30));
        glow.setSpread(0.05);
        key.setEffect(glow);

        bob(key);
        sway(key);

        return key;
    }

    /** Fluttua piano su e giù. */
    private void bob(Node node) {
        TranslateTransition bob = new TranslateTransition(Duration.seconds(1.8), node);
        bob.setFromY(0);
        bob.setToY(-14);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.setInterpolator(Interpolator.EASE_BOTH);

        playEndless(bob);
    }

    /** Ondeggia appena, come sospesa a un filo. */
    private void sway(Node node) {
        RotateTransition sway = new RotateTransition(Duration.seconds(2.6), node);
        sway.setFromAngle(-9);
        sway.setToAngle(9);
        sway.setAutoReverse(true);
        sway.setCycleCount(Animation.INDEFINITE);
        sway.setInterpolator(Interpolator.EASE_BOTH);

        playEndless(sway);
    }

    /** Avvia un'animazione senza fine, registrandola perché sia richiudibile. */
    private void playEndless(Animation animation) {
        endlessAnimations.add(animation);
        animation.play();
    }
}
