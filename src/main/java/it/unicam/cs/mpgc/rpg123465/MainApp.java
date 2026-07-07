package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point dell'applicazione Tower of Self.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameEngine gameEngine = GameFactory.createNewGame();

        MainWindow mainWindow = new MainWindow(gameEngine);
        mainWindow.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}