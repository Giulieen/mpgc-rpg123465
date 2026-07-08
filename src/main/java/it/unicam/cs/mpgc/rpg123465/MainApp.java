package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg123465.ui.IntroScreen;
import it.unicam.cs.mpgc.rpg123465.ui.MainWindow;
import it.unicam.cs.mpgc.rpg123465.ui.StartMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Entry point dell'applicazione Tower of Self.
 * <p>
 * Coordina la navigazione tra le schermate (menu iniziale, introduzione,
 * schermata di gioco) riutilizzando un'unica scena di cui sostituisce la
 * radice. Ogni schermata è una vista indipendente: la logica di gioco resta
 * nel controller e nel modello.
 */
public class MainApp extends Application {

    private static final String SAVE_PATH = "saves/save.dat";
    private static final double WIDTH = 820;
    private static final double HEIGHT = 720;

    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(new StackPane(), WIDTH, HEIGHT);
        applyStylesheet();

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        showStartMenu();
        stage.show();
    }

    private void showStartMenu() {
        StartMenu menu = new StartMenu(this::startNewGame, this::loadGame);
        scene.setRoot(menu.createView());
    }

    private void startNewGame(String playerName) {
        GameController controller = createController(playerName);
        IntroScreen intro = new IntroScreen(() -> showMainGame(controller));
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
        showMainGame(controller);
    }

    private void showMainGame(GameController controller) {
        scene.setRoot(new MainWindow(controller).createView());
    }

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
