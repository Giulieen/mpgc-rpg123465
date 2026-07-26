package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.fear.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg123465.ui.AlterEgoScreen;
import it.unicam.cs.mpgc.rpg123465.ui.FearEncounterScreen;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;
import it.unicam.cs.mpgc.rpg123465.ui.IntroScreen;
import it.unicam.cs.mpgc.rpg123465.ui.SceneFlow;
import it.unicam.cs.mpgc.rpg123465.ui.StartMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;

/**
 * Entry point dell'applicazione Tower of Self.
 * <p>
 * Coordina la navigazione tra le schermate (menu, introduzione, piani della
 * Torre, rivelazione dell'alter ego) riutilizzando un'unica scena di cui
 * sostituisce la radice. Ogni schermata è una vista indipendente: la logica di
 * gioco resta nel controller e nel modello.
 */
public class MainApp extends Application {

    private static final String SAVE_PATH = "saves/save.dat";
    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 760;

    private Scene scene;

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
                "/audio/rats-many.mp3");

        scene = new Scene(new StackPane(), INITIAL_WIDTH, INITIAL_HEIGHT);
        applyStylesheet();

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);

        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        stage.centerOnScreen();

        showStartMenu();
        stage.show();
    }

    // --- Navigazione fra le schermate ---------------------------------------

    private void showStartMenu() {
        Sound.stopAll();
        StartMenu menu = new StartMenu(this::startNewGame, this::loadGame);
        scene.setRoot(menu.createView());
    }

    private void startNewGame(String playerName) {
        GameController controller = createController(playerName);
        IntroScreen intro = new IntroScreen(() -> showCurrentFloor(controller));
        scene.setRoot(intro.createView());
    }

    private void loadGame() {
        GameController controller = createController(null);

        if (!controller.hasSavedGame()) {
            showInfo("Nessun salvataggio",
                    "Non è stata trovata alcuna partita salvata.");
            return;
        }

        controller.loadGame();

        if (controller.isGameCompleted()) {
            showAlterEgo(controller);
        } else {
            showCurrentFloor(controller);
        }
    }

    /**
     * Avvia il piano su cui si trova il giocatore, attraversandone le scene una
     * dopo l'altra.
     */
    private void showCurrentFloor(GameController controller) {
        FloorContent content = controller.getCurrentFloor().getContent();

        new SceneFlow(
                scenesFor(content, controller),
                scene::setRoot,
                () -> onFloorCompleted(controller)
        ).start();
    }

    /**
     * Compone le scene che formano un piano.
     * <p>
     * Oggi la Torre conosce un solo tipo di contenuto, l'incontro con una
     * paura, che si risolve in un'unica scena. I piani successivi ne
     * alterneranno più d'una — l'arrivo, la scelta, la prova — e basterà
     * elencarle qui.
     */
    private List<FloorScene> scenesFor(FloorContent content, GameController controller) {
        if (content instanceof FearEncounter encounter) {
            return List.of(new FearEncounterScreen(encounter, controller));
        }

        throw new IllegalStateException(
                "Nessuna scena sa mostrare questo contenuto: " + content.getClass());
    }

    /**
     * Il piano è stato affrontato: si salgono le scale (recuperando il fiato) e
     * si prosegue, oppure la Torre è finita e Chimeris si rivela.
     */
    private void onFloorCompleted(GameController controller) {
        controller.climbToNextFloor();

        if (controller.isGameCompleted()) {
            showAlterEgo(controller);
        } else {
            showCurrentFloor(controller);
        }
    }

    private void showAlterEgo(GameController controller) {
        AlterEgoScreen finale = new AlterEgoScreen(controller.getMind(), this::showStartMenu);
        scene.setRoot(finale.createView());
    }

    // --- Supporto ------------------------------------------------------------

    private GameController createController(String playerName) {
        GameEngine engine = GameFactory.createNewGame(playerName);
        SaveManager saveManager = new FileSaveManager(SAVE_PATH);
        return new GameController(engine, saveManager);
    }

    private void applyStylesheet() {
        URL stylesheet = getClass().getResource("/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
