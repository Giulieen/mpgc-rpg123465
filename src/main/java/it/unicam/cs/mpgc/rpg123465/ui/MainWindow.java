package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Finestra principale dell'applicazione Tower of Self.
 */
public class MainWindow {

    /**
     * Mostra la finestra principale.
     *
     * @param stage stage principale JavaFX
     */
    public void show(Stage stage) {
        Label title = new Label("Tower of Self");
        Label subtitle = new Label("RPG psicologico a turni");

        VBox root = new VBox(10, title, subtitle);
        root.setPrefSize(600, 400);

        Scene scene = new Scene(root);

        stage.setTitle("Tower of Self RPG");
        stage.setScene(scene);
        stage.show();
    }
}