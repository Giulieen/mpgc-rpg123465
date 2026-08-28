package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.view.Navigator;
import it.unicam.cs.mpgc.rpg123465.view.WindowFrame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.net.URL;

/**
 * Entry point dell'applicazione Tower of Self.
 * <p>
 * Si occupa solo dell'avvio JavaFX: prepara i suoni, l'icona e la finestra,
 * poi affida la navigazione fra le schermate al {@link Navigator}.
 */
public class MainApp extends Application {

    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 760;

    /**
     * Le misure dell'icona. Windows ne sceglie una diversa per la barra delle
     * applicazioni, per Alt+Tab e per il collegamento: fornirle tutte evita
     * che ne ridimensioni una sbagliata.
     */
    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};

    @Override
    public void start(Stage stage) {
        // Decodifica gli effetti in anticipo: al primo clic devono partire subito.
        Sound.preload(
                "/audio/gate-open.mp3",
                "/audio/padlock-unlock.wav",
                "/audio/owl-hoot.mp3",
                "/audio/scurrying.mp3",
                "/audio/squeak.wav",
                "/audio/mousetrap-snap.wav",
                "/audio/torch-whoosh.wav",
                "/audio/rats-many.mp3",
                "/audio/scream.wav",
                "/audio/fall.wav",
                "/audio/arrow-tap.wav",
                "/audio/arrow-wrong.wav",
                "/audio/wood-step.wav",
                "/audio/wood-break.wav");

        loadIcons(stage);

        /*
         * Senza decorazioni: la barra di sistema è bianca e spezza l'atmosfera
         * prima ancora del menu. I comandi della finestra li disegna WindowFrame.
         */
        stage.initStyle(StageStyle.UNDECORATED);

        WindowFrame frame = new WindowFrame(stage);

        Scene scene = new Scene(frame.getRoot(), INITIAL_WIDTH, INITIAL_HEIGHT);
        applyStylesheet(scene);

        stage.setTitle("Tower of Self");
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        new Navigator(frame).showStartMenu();

        stage.show();
        frame.centreOnScreen(INITIAL_WIDTH, INITIAL_HEIGHT);

        /*
         * La finestra si prende anche il fuoco della tastiera, non solo lo
         * schermo. Avviando il gioco da terminale Windows tende a lasciare il
         * fuoco a chi ha lanciato il comando: la finestra compare, il mouse
         * funziona (il primo clic gliela da'), ma i tasti no. Nel Piano III si
         * gioca a frecce, e una prova a tempo che non risponde alla tastiera
         * finche' non ci si clicca sopra sembra guasta.
         */
        stage.toFront();
        stage.requestFocus();
    }

    private void loadIcons(Stage stage) {
        for (int size : ICON_SIZES) {
            try (InputStream stream = getClass()
                    .getResourceAsStream("/images/icon/tower-" + size + ".png")) {

                if (stream != null) {
                    stage.getIcons().add(new Image(stream));
                }

            } catch (Exception exception) {
                // Senza icona il gioco parte lo stesso.
            }
        }
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
