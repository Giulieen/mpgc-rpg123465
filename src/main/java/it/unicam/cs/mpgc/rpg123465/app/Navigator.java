package it.unicam.cs.mpgc.rpg123465.app;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.controller.OperationResult;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.FilePlayerRegistry;
import it.unicam.cs.mpgc.rpg123465.persistence.FileRecordStore;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.PlayerRegistry;
import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import it.unicam.cs.mpgc.rpg123465.questions.QuestionCatalogException;
import it.unicam.cs.mpgc.rpg123465.ui.ProfileResultScreen;
import it.unicam.cs.mpgc.rpg123465.ui.IntroScreen;
import it.unicam.cs.mpgc.rpg123465.ui.SceneFlow;
import it.unicam.cs.mpgc.rpg123465.ui.StartMenu;
import it.unicam.cs.mpgc.rpg123465.ui.WindowFrame;

/**
 * Governa la navigazione fra menu, introduzione, piani e finale della demo.
 */
public final class Navigator {

    private static final String SAVE_PATH =
            "saves/save.dat";

    private static final String RECORDS_PATH =
            "saves/records.properties";

    private static final String PLAYERS_PATH =
            "saves/players.txt";

    /**
     * I record sopravvivono alle partite: uno solo per tutta l'applicazione,
     * creato all'avvio e non insieme al salvataggio.
     */
    private final RecordStore records =
            new FileRecordStore(
                    RECORDS_PATH
            );

    /** I nomi già usati, per non farli riprendere da una partita nuova. */
    private final PlayerRegistry players =
            new FilePlayerRegistry(
                    PLAYERS_PATH
            );

    private final WindowFrame frame;

    public Navigator(
            WindowFrame frame
    ) {
        if (frame == null) {
            throw new IllegalArgumentException(
                    "La cornice non può essere null."
            );
        }

        this.frame = frame;
    }

    public void showStartMenu() {
        Sound.stopAll();

        StartMenu menu =
                new StartMenu(
                        this::startNewGame,
                        this::loadGame,
                        players::isTaken
                );

        frame.setContent(
                menu.createView()
        );
    }

    private void startNewGame(
            String playerName
    ) {
        GameController controller;

        try {
            controller =
                    createController(
                            playerName
                    );

        } catch (QuestionCatalogException exception) {
            showCatalogError(
                    exception
            );
            return;
        }

        /*
         * Il nome si riserva solo ora: se il catalogo fosse rotto la partita
         * non partirebbe, e sarebbe bruciato per niente.
         */
        players.register(playerName);

        IntroScreen intro =
                new IntroScreen(
                        () -> showCurrentFloor(
                                controller
                        )
                );

        frame.setContent(
                intro.createView()
        );
    }

    private void loadGame() {
        GameController controller;

        try {
            controller =
                    createController(
                            null
                    );

        } catch (QuestionCatalogException exception) {
            showCatalogError(
                    exception
            );
            return;
        }

        if (!controller.hasSavedGame()) {
            showInfo(
                    "Nessun salvataggio",
                    "Non è stata trovata alcuna partita salvata.",
                    "Torna al menu"
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
                    ),
                    "Torna al menu"
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
     * Crea il checkpoint prima della scena e lo salva da solo.
     */
    private void showCurrentFloor(
            GameController controller
    ) {
        controller.beginFloorCheckpoint();
        autoSave(controller);

        FloorContent content =
                controller
                        .getCurrentFloor()
                        .getContent();

        FloorSceneFactory scenes =
                new FloorSceneFactory(
                        controller,
                        records,
                        onDismissed -> showSaveResult(
                                controller,
                                onDismissed
                        ),
                        () -> exitCurrentFloor(
                                controller
                        )
                );

        new SceneFlow(
                scenes.scenesFor(
                        content
                ),
                frame::setContent,
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
                    ),
                    "Riprendi"
            );
            return;
        }

        showStartMenu();
    }

    /**
     * Salva senza dire niente.
     *
     * <p>
     * Scatta all'ingresso di ogni piano e alla fine della salita, cioè
     * esattamente dove il checkpoint è già quello giusto: il giocatore ritrova
     * la partita dov'era anche se chiude la finestra senza pensarci. Il pulsante
     * Salva resta, per chi vuole fermare il punto di propria iniziativa.
     *
     * <p>
     * Un errore non viene mostrato: interrompere la partita con un messaggio
     * per un salvataggio che il giocatore non ha chiesto darebbe più fastidio
     * del problema. Se ne accorgerà semmai salvando a mano, dove l'esito si
     * vede.
     */
    private void autoSave(
            GameController controller
    ) {
        controller.saveGame();
    }

    /**
     * Mostra direttamente il risultato della demo.
     */
    private void showDemoResult(
            GameController controller
    ) {
        autoSave(controller);

        ProfileResultScreen finale =
                new ProfileResultScreen(
                        controller.getMind(),
                        this::showStartMenu
                );

        frame.setContent(
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

    /**
     * Salva e mostra l'esito.
     *
     * Il messaggio non blocca più il thread grafico, quindi la prova che si
     * era fermata per mostrarlo va ripresa quando il giocatore lo chiude: è
     * questo il compito di {@code onDismissed}.
     *
     * @param onDismissed avvisa il piano che il messaggio è stato chiuso
     */
    private void showSaveResult(
            GameController controller,
            Runnable onDismissed
    ) {
        OperationResult result =
                controller.saveGame();

        if (result.success()) {
            showInfo(
                    "Salvataggio",
                    "Punto di ripresa salvato. "
                            + "Caricando ripartirai dall'inizio di questo piano.",
                    "Riprendi",
                    onDismissed
            );
        } else {
            showInfo(
                    "Salvataggio non riuscito",
                    errorMessage(
                            "Si è verificato un errore durante il salvataggio.",
                            result
                    ),
                    "Riprendi",
                    onDismissed
            );
        }
    }

    /**
     * Spiega che la partita non può iniziare perché il catalogo è guasto.
     *
     * Il dettaglio arriva dal repository e indica il file e il punto del
     * problema: senza, resterebbe soltanto uno stack trace nella console.
     */
    private void showCatalogError(
            QuestionCatalogException exception
    ) {
        showInfo(
                "Domande non disponibili",
                "Non è stato possibile avviare la partita perché il catalogo "
                        + "delle domande non è leggibile."
                        + "\n\nDettaglio: "
                        + exception.getMessage(),
                "Torna al menu"
        );
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

    /**
     * Mostra un messaggio del gioco.
     *
     * L'etichetta del pulsante dice dove si va chiudendo, invece di un generico
     * assenso: da un messaggio di sistema ci si aspetta un "va bene", da un
     * gioco di sapere cosa succede dopo.
     *
     * @param buttonLabel cosa succede chiudendo
     */
    private void showInfo(
            String title,
            String content,
            String buttonLabel
    ) {
        showInfo(title, content, buttonLabel, null);
    }

    private void showInfo(
            String title,
            String content,
            String buttonLabel,
            Runnable onDismissed
    ) {
        frame.showMessage(
                title,
                content,
                buttonLabel,
                onDismissed
        );
    }
}
