package it.unicam.cs.mpgc.rpg123465.app;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.controller.OperationResult;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg123465.ui.ProfileResultScreen;
import it.unicam.cs.mpgc.rpg123465.ui.IntroScreen;
import it.unicam.cs.mpgc.rpg123465.ui.SceneFlow;
import it.unicam.cs.mpgc.rpg123465.ui.StartMenu;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

/**
 * Governa la navigazione fra menu, introduzione, piani e finale della demo.
 */
public final class Navigator {

    private static final String SAVE_PATH =
            "saves/save.dat";

    private final Scene scene;

    public Navigator(
            Scene scene
    ) {
        if (scene == null) {
            throw new IllegalArgumentException(
                    "La scena non può essere null."
            );
        }

        this.scene = scene;
    }

    public void showStartMenu() {
        Sound.stopAll();

        StartMenu menu =
                new StartMenu(
                        this::startNewGame,
                        this::loadGame
                );

        scene.setRoot(
                menu.createView()
        );
    }

    private void startNewGame(
            String playerName
    ) {
        GameController controller =
                createController(
                        playerName
                );

        IntroScreen intro =
                new IntroScreen(
                        () -> showCurrentFloor(
                                controller
                        )
                );

        scene.setRoot(
                intro.createView()
        );
    }

    private void loadGame() {
        GameController controller =
                createController(
                        null
                );

        if (!controller.hasSavedGame()) {
            showInfo(
                    "Nessun salvataggio",
                    "Non è stata trovata alcuna partita salvata."
            );
            return;
        }

        OperationResult result =
                controller.loadGame();

        if (!result.success()) {
            showInfo(
                    "Caricamento non riuscito",
                    errorMessage(
                            "Si è verificato un errore durante il caricamento.",
                            result
                    )
            );
            return;
        }

        if (controller.isGameCompleted()) {
            showDemoResult(
                    controller
            );
        } else {
            showCurrentFloor(
                    controller
            );
        }
    }

    /**
     * Crea il checkpoint prima della scena.
     */
    private void showCurrentFloor(
            GameController controller
    ) {
        controller.beginFloorCheckpoint();

        FloorContent content =
                controller
                        .getCurrentFloor()
                        .getContent();

        FloorSceneFactory scenes =
                new FloorSceneFactory(
                        controller,
                        () -> showSaveResult(
                                controller
                        ),
                        () -> exitCurrentFloor(
                                controller
                        )
                );

        new SceneFlow(
                scenes.scenesFor(
                        content
                ),
                scene::setRoot,
                () -> onFloorCompleted(
                        controller
                )
        ).start();
    }

    private void onFloorCompleted(
            GameController controller
    ) {
        controller.climbToNextFloor();

        if (controller.isGameCompleted()) {
            showDemoResult(
                    controller
            );
        } else {
            showCurrentFloor(
                    controller
            );
        }
    }

    /**
     * Salva il checkpoint del piano e torna al menu.
     */
    private void exitCurrentFloor(
            GameController controller
    ) {
        OperationResult result =
                controller.saveGame();

        if (!result.success()) {
            showInfo(
                    "Uscita non riuscita",
                    errorMessage(
                            "Non è stato possibile salvare il punto di ripresa.",
                            result
                    )
            );
            return;
        }

        showStartMenu();
    }

    /**
     * Mostra direttamente il risultato della demo.
     */
    private void showDemoResult(
            GameController controller
    ) {
        ProfileResultScreen finale =
                new ProfileResultScreen(
                        controller.getMind(),
                        this::showStartMenu
                );

        scene.setRoot(
                finale.createView()
        );
    }

    private GameController createController(
            String playerName
    ) {
        GameEngine engine =
                GameFactory.createNewGame(
                        playerName
                );

        SaveManager saveManager =
                new FileSaveManager(
                        SAVE_PATH
                );

        return new GameController(
                engine,
                saveManager
        );
    }

    private void showSaveResult(
            GameController controller
    ) {
        OperationResult result =
                controller.saveGame();

        if (result.success()) {
            showInfo(
                    "Salvataggio",
                    "Punto di ripresa salvato. "
                            + "Caricando ripartirai dall'inizio di questo piano."
            );
        } else {
            showInfo(
                    "Salvataggio non riuscito",
                    errorMessage(
                            "Si è verificato un errore durante il salvataggio.",
                            result
                    )
            );
        }
    }

    private String errorMessage(
            String fallback,
            OperationResult result
    ) {
        if (result.detail() == null
                || result.detail().isBlank()) {

            return fallback;
        }

        return fallback
                + "\n\nDettaglio: "
                + result.detail();
    }

    private void showInfo(
            String title,
            String content
    ) {
        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                title
        );

        alert.setContentText(
                content
        );

        alert.showAndWait();
    }
}
