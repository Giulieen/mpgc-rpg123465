package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.app.Navigator;
import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Entry point dell'applicazione Tower of Self.
 * <p>
 * Si occupa solo dell'avvio JavaFX: prepara i suoni, la finestra e la scena,
 * poi affida la navigazione fra le schermate al {@link Navigator}.
 */
public class MainApp extends Application {

    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 760;

    @Override
    public void start(Stage stage) {
        // Decodifica gli effetti in anticipo: al primo clic devono partire subito.
        Sound.preload(
                "/audio/gate-open.mp3",
                "/audio/padlock-unlock.mp3",
                "/audio/owl-hoot.mp3",
                "/audio/scurrying.mp3",
                "/audio/squeak.mp3",
                "/audio/mousetrap-snap.mp3",
                "/audio/torch-whoosh.mp3",
                "/audio/rumble.mp3",
                "/audio/scratching.mp3",
                "/audio/chimeris-laugh.mp3",
                "/audio/rats-many.mp3",
                "/audio/scream.mp3",
                "/audio/fall.mp3",
                "/audio/arrow-tap.mp3",
                "/audio/arrow-alert.mp3",
                "/audio/arrow-wrong.mp3",
                "/audio/wood-creak.mp3",
                "/audio/wood-step.mp3",
                "/audio/wood-break.mp3");

        Scene scene = new Scene(new StackPane(), INITIAL_WIDTH, INITIAL_HEIGHT);
        applyStylesheet(scene);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();

        new Navigator(scene).showStartMenu();
        stage.show();
    }

    private void applyStylesheet(Scene scene) {
        URL stylesheet = getClass().getResource("/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
