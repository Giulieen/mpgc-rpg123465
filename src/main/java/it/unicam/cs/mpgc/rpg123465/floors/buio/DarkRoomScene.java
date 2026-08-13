package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;
import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.DilemmaOption;
import it.unicam.cs.mpgc.rpg123465.questions.Questions;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;
import it.unicam.cs.mpgc.rpg123465.ui.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.ui.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.ui.support.CloseupOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.CountdownClock;
import it.unicam.cs.mpgc.rpg123465.ui.support.DilemmaPrompt;
import it.unicam.cs.mpgc.rpg123465.ui.support.RoomLighting;
import it.unicam.cs.mpgc.rpg123465.ui.support.SceneFx;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Piano II — Il Buio.
 *
 * Il piano contiene tre dilemmi "Preferiresti":
 * uno prima del puzzle e due durante il conto alla rovescia.
 * Durante i dilemmi il timer viene messo in pausa.
 */
public class DarkRoomScene implements FloorScene {

    private static final double SCENE_SCALE = 0.88;

    private static final double KEYPAD_X = 0.425;
    private static final double KEYPAD_Y = 0.520;

    private static final int SECOND_DILEMMA_AT = 40;
    private static final int THIRD_DILEMMA_AT = 20;

    /** Identifica il record del piano nell'archivio condiviso. */
    private static final String RECORD_KEY = "buio.tempo";

    private final DarkRoom room;
    private final DarkRoomController controller;
    private final RecordStore records;
    private final HeaderBar header;
    private final Runnable onSave;
    private final Runnable onExit;

    private final StackPane root =
            new StackPane();

    private final RoomLighting lighting =
            new RoomLighting(
                    root
            );

    private final CloseupOverlay closeup =
            new CloseupOverlay(
                    root,
                    () -> closeupOpen = false
            );

    private final CountdownClock clock =
            new CountdownClock();

    private final CombinationLockState lockState =
            new CombinationLockState(4);

    private Consumer<SceneOutcome> onFinished;

    private Region headerView;

    private DarkRoomWalls walls;
    private Label countdown;

    private boolean resolved;
    private boolean closeupOpen;
    private boolean entered;

    private List<Dilemma> dilemmas =
            List.of();

    private int nextDilemmaIndex;

    private boolean dilemmaOpen;
    private boolean successWaitingForDilemmas;

    /**
     * Attesa dell'interludio fra un tentativo e il successivo.
     *
     * Va conservata perché il giocatore può uscire mentre è a schermo: senza
     * fermarla, allo scadere ripartirebbe un tentativo — con il suo audio e il
     * suo countdown — su una scena che non è più visibile.
     */
    private PauseTransition interlude;

    public DarkRoomScene(
            DarkRoom room,
            GameController game,
            RecordStore records,
            Runnable onSave,
            Runnable onExit
    ) {
        if (room == null || game == null || records == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.room = room;
        this.records = records;

        this.controller =
                new DarkRoomController(
                        room,
                        game
                );

        this.onSave = onSave;
        this.onExit = onExit;

        this.header =
                new HeaderBar(
                        game.getPlayerName(),
                        onSave == null
                                ? null
                                : this::saveWithClockSuspended,
                        onExit == null
                                ? null
                                : this::exitLevel
                );

        lighting.setToggleGuard(
                () -> !resolved
                        && !closeupOpen
                        && !dilemmaOpen
        );

        lighting.setOnChange(() -> {
            if (lighting.isLit()
                    && walls != null) {

                walls.hideNumbers();
            }
        });
    }

    @Override
    public Parent createView(
            Consumer<SceneOutcome> onFinished
    ) {
        if (onFinished == null) {
            throw new IllegalArgumentException(
                    "Il callback di fine scena non può essere null."
            );
        }

        this.onFinished = onFinished;

        root.getStyleClass().add(
                "fear-root"
        );

        if (!entered) {
            prepareDilemmas();
            entered = true;
        }

        headerView =
                header.createView();

        header.setProva(
                room.title()
        );

        updateHeader();

        root.setOnMouseMoved(
                event -> onMouseMoved(
                        event.getX(),
                        event.getY()
                )
        );

        showEntryDilemma();

        return root;
    }

    /**
     * Estrae tre domande casuali dal catalogo JSON del Piano II.
     */
    private void prepareDilemmas() {
        dilemmas =
                Questions.repository()
                        .randomQuestions(
                                "buio",
                                3
                        );

        nextDilemmaIndex = 0;
    }

    private void showEntryDilemma() {
        root.getChildren().setAll(
                SceneFx.contain(
                        root,
                        room.backgroundResource(),
                        SCENE_SCALE
                ),
                headerView
        );

        StackPane.setAlignment(
                headerView,
                Pos.TOP_CENTER
        );

        showNextDilemma(
                () -> showBriefing(
                        this::startAttempt
                ),
                false
        );
    }

    private void showNextDilemma(
            Runnable afterChoice,
            boolean pauseClock
    ) {
        if (dilemmaOpen
                || nextDilemmaIndex
                >= dilemmas.size()) {

            if (afterChoice != null) {
                afterChoice.run();
            }

            return;
        }

        if (pauseClock) {
            clock.pause();
        }

        dilemmaOpen = true;

        Dilemma dilemma =
                dilemmas.get(
                        nextDilemmaIndex
                );

        DilemmaPrompt.show(
                root,
                dilemma.question(),
                promptOptions(
                        dilemma
                ),
                option -> {
                    controller.registerChoice(
                            option.trait()
                    );

                    nextDilemmaIndex++;
                    dilemmaOpen = false;

                    updateHeader();

                    if (afterChoice != null) {
                        afterChoice.run();
                    } else if (!successWaitingForDilemmas) {
                        clock.resume();
                    }
                },
                headerView
        );
    }

    private void showBriefing(
            Runnable onStart
    ) {
        StackPane overlay =
                new StackPane(
                        SceneFx.veil(
                                root,
                                0.6
                        )
                );

        Button enter =
                new Button(
                        "Entra nel buio"
                );

        enter.getStyleClass().add(
                "menu-button"
        );

        enter.setOnAction(event -> {
            root.getChildren().remove(
                    overlay
            );

            onStart.run();
        });

        VBox panel =
                new VBox(
                        26,
                        SceneFx.paragraph(
                                room.intro()
                        ),
                        enter
                );

        panel.setAlignment(
                Pos.CENTER
        );

        panel.setMaxWidth(
                760
        );

        panel.getStyleClass().add(
                "fear-panel"
        );

        overlay.getChildren().add(
                panel
        );

        StackPane.setAlignment(
                panel,
                Pos.CENTER
        );

        root.getChildren().add(
                overlay
        );

        headerView.toFront();
    }

    private void startAttempt() {
        interlude = null;

        resolved = false;
        closeupOpen = false;
        successWaitingForDilemmas = false;

        lockState.reset();
        lighting.setLit(false);

        ImageView background =
                SceneFx.contain(
                        root,
                        room.backgroundResource(),
                        SCENE_SCALE
                );

        lighting.attach(
                background
        );

        walls =
                new DarkRoomWalls(
                        root,
                        room.combination()
                );

        Rectangle keypadHotspot =
                new Rectangle(
                        96,
                        128,
                        Color.TRANSPARENT
                );

        keypadHotspot.setCursor(
                Cursor.HAND
        );

        keypadHotspot.setOnMouseClicked(
                event -> openCloseup()
        );

        SceneFx.place(
                root,
                keypadHotspot,
                96,
                128,
                KEYPAD_X,
                KEYPAD_Y
        );

        FontIcon lightSwitch =
                lighting.switchIcon();

        SceneFx.place(
                root,
                lightSwitch,
                44,
                44,
                0.07,
                0.90
        );

        countdown =
                new Label();

        countdown.getStyleClass().add(
                "dark-timer"
        );

        SceneFx.place(
                root,
                countdown,
                120,
                40,
                0.90,
                0.15
        );

        Pane hotspots =
                new Pane(
                        walls.createView(),
                        keypadHotspot,
                        lightSwitch,
                        countdown
                );

        hotspots.setPickOnBounds(
                false
        );

        root.getChildren().setAll(
                background,
                lighting.lampGlow(),
                lighting.darkness(),
                hotspots,
                headerView
        );

        StackPane.setAlignment(
                headerView,
                Pos.TOP_CENTER
        );

        updateHeader();
        startClock();
    }

    private void onMouseMoved(
            double x,
            double y
    ) {
        if (walls == null
                || lighting.isLit()
                || resolved
                || closeupOpen
                || dilemmaOpen) {

            return;
        }

        walls.updateTorch(
                lighting.darkness(),
                x,
                y,
                lockState.getActiveSlot()
        );
    }

    private void openCloseup() {
        if (resolved
                || closeupOpen
                || dilemmaOpen) {

            return;
        }

        closeupOpen = true;

        CombinationLockView lock =
                new CombinationLockView(
                        lockState,
                        this::submit
                );

        closeup.open(
                lock.createView()
        );

        /*
         * Il close-up copre l'intera scena: senza questo, i tentativi, Salva
         * ed Esci sparirebbero proprio dove il giocatore passa più tempo.
         * Il primo piano resta comunque utilizzabile, perché la barra occupa
         * solo la fascia superiore.
         */
        headerView.toFront();
    }

    private void submit(
            String code
    ) {
        if (controller.opens(code)) {
            beforeSuccess();
        } else {
            wrongCombination();
        }
    }

    /**
     * Se il giocatore trova il codice prima che siano apparse tutte
     * le domande, le domande mancanti vengono completate prima
     * dell'esito del piano.
     */
    private void beforeSuccess() {
        if (resolved) {
            return;
        }

        clock.pause();

        if (closeup.isOpen()) {
            closeup.close();
        }

        if (nextDilemmaIndex
                < dilemmas.size()) {

            successWaitingForDilemmas = true;

            showRemainingDilemmasBeforeSuccess();
            return;
        }

        succeed();
    }

    private void showRemainingDilemmasBeforeSuccess() {
        if (nextDilemmaIndex
                >= dilemmas.size()) {

            successWaitingForDilemmas = false;
            succeed();
            return;
        }

        showNextDilemma(
                this::showRemainingDilemmasBeforeSuccess,
                false
        );
    }

    private void succeed() {
        if (resolved) {
            return;
        }

        resolved = true;

        /*
         * Il tempo va letto prima di fermare il clock, che azzera il residuo.
         * Il conto alla rovescia resta la meccanica; il record misura invece
         * quanto ci si è messi, dove più basso è meglio.
         */
        int elapsed =
                room.seconds()
                        - clock.remainingSeconds();

        clock.stop();

        if (closeup.isOpen()) {
            closeup.close();
        }

        Sound.stopAll();

        Sound.play(
                "/audio/gate-open.mp3",
                0.7
        );

        updateHeader();
        showSuccessResult(
                elapsed,
                submitRecord(elapsed)
        );
    }

    /**
     * Conserva il tempo se ha battuto il record.
     *
     * @return il record da mostrare dopo questa prova
     */
    private int submitRecord(
            int elapsed
    ) {
        OptionalInt previous =
                records.best(
                        RECORD_KEY
                );

        if (previous.isEmpty()
                || elapsed < previous.getAsInt()) {

            records.save(
                    RECORD_KEY,
                    elapsed
            );

            return elapsed;
        }

        return previous.getAsInt();
    }

    private String formatTime(
            int seconds
    ) {
        return String.format(
                "%02d:%02d",
                seconds / 60,
                seconds % 60
        );
    }

    private void showSuccessResult(
            int elapsed,
            int record
    ) {
        StackPane overlay =
                new StackPane(
                        SceneFx.veil(
                                root,
                                0.78
                        )
                );

        Label result =
                SceneFx.paragraph(
                        room.outro()
                );

        Label times =
                new Label(
                        "Tempo: "
                                + formatTime(elapsed)
                                + "     ·     Record: "
                                + formatTime(record)
                );

        times.getStyleClass().add(
                "fear-effects"
        );

        Button next =
                new Button(
                        "Continua"
                );

        next.getStyleClass().add(
                "menu-button"
        );

        next.setOnAction(event -> {
            cleanup();

            onFinished.accept(
                    SceneOutcome.AVANTI
            );
        });

        VBox panel =
                new VBox(
                        24,
                        result,
                        times,
                        next
                );

        panel.setAlignment(
                Pos.CENTER
        );

        panel.setMaxWidth(
                780
        );

        panel.getStyleClass().add(
                "fear-panel"
        );

        overlay.getChildren().add(
                panel
        );

        StackPane.setAlignment(
                panel,
                Pos.CENTER
        );

        root.getChildren().add(
                overlay
        );

        headerView.toFront();
    }

    private void wrongCombination() {
        if (resolved) {
            return;
        }

        resolved = true;
        clock.stop();

        if (closeup.isOpen()) {
            closeup.close();
        }

        Sound.stopAll();

        Sound.play(
                "/audio/padlock-unlock.mp3",
                0.35
        );

        failAttempt(
                room.wrongText()
        );
    }

    private void timeOut() {
        if (resolved) {
            return;
        }

        resolved = true;
        clock.stop();

        if (closeup.isOpen()) {
            closeup.close();
        }

        failAttempt(
                room.timeoutText()
        );
    }

    /**
     * Registra un errore grave e decide come prosegue la prova.
     *
     * Finché restano tentativi si torna alla serratura dopo un breve
     * interludio; esauriti, la prova è fallita e va ricominciata da capo.
     */
    private void failAttempt(
            String message
    ) {
        controller.registerFailedAttempt();
        updateHeader();

        if (controller.canRetry()) {
            showInterlude(
                    message
            );
        } else {
            showTrialFailed(
                    message
            );
        }
    }

    private void showInterlude(
            String message
    ) {
        stopInterlude();

        interlude = SceneFx.interlude(
                root,
                message,
                attemptsNote(),
                3.4,
                this::startAttempt
        );

        headerView.toFront();
    }

    private String attemptsNote() {
        int left =
                controller.remainingAttempts();

        return left == 1
                ? "Ti resta un tentativo."
                : "Ti restano " + left + " tentativi.";
    }

    /**
     * Tentativi esauriti: la prova ricomincia con i tentativi al massimo.
     *
     * Le risposte gia' date restano registrate e le domande non vengono
     * riproposte, quindi ricominciare non altera il profilo.
     */
    private void showTrialFailed(
            String message
    ) {
        StackPane overlay =
                new StackPane(
                        SceneFx.veil(
                                root,
                                0.86
                        )
                );

        Label heading =
                new Label(
                        "Prova fallita"
                );

        heading.getStyleClass().add(
                "fear-title"
        );

        Button again =
                new Button(
                        "Ricomincia la prova"
                );

        again.getStyleClass().add(
                "menu-button"
        );

        again.setOnAction(event -> {
            root.getChildren().remove(
                    overlay
            );

            controller.restartTrial();
            updateHeader();
            startAttempt();
        });

        VBox panel =
                new VBox(
                        24,
                        heading,
                        SceneFx.paragraph(
                                message
                        ),
                        again
                );

        panel.setAlignment(
                Pos.CENTER
        );

        panel.setMaxWidth(
                780
        );

        panel.getStyleClass().add(
                "fear-panel"
        );

        overlay.getChildren().add(
                panel
        );

        StackPane.setAlignment(
                panel,
                Pos.CENTER
        );

        root.getChildren().add(
                overlay
        );

        headerView.toFront();
    }

    private void startClock() {
        Sound.stopAll();

        Sound.loop(
                "/audio/ambience-dark.mp3",
                0.25
        );

        clock.start(
                room.seconds(),
                countdown,
                this::timeOut,
                this::onClockSecondChanged
        );
    }

    /**
     * Mostra la seconda domanda a 40 secondi e la terza a 20.
     * Se il close-up della serratura è aperto, aspetta il tick
     * successivo invece di coprire l'interazione in corso.
     */
    private void onClockSecondChanged(
            int remaining
    ) {
        if (resolved
                || dilemmaOpen
                || closeupOpen) {

            return;
        }

        if (nextDilemmaIndex == 1
                && remaining <= SECOND_DILEMMA_AT) {

            showNextDilemma(
                    null,
                    true
            );

            return;
        }

        if (nextDilemmaIndex == 2
                && remaining <= THIRD_DILEMMA_AT) {

            showNextDilemma(
                    null,
                    true
            );
        }
    }

    private void updateHeader() {
        header.setTentativi(
                controller.remainingAttempts(),
                controller.maxAttempts()
        );
    }

    /**
     * Salva tenendo fermo il conto alla rovescia.
     *
     * La conferma del salvataggio è una finestra modale, e una modale JavaFX
     * gira in un event loop annidato: i pulse continuano, quindi senza questa
     * sospensione il tempo scorrerebbe mentre il giocatore legge il messaggio.
     * Il countdown riparte solo se stava davvero correndo: se era gia' fermo
     * per un dilemma, a riprenderlo ci pensa la risposta.
     */
    private void saveWithClockSuspended() {
        boolean running = !clock.isPaused();

        clock.pause();

        try {
            onSave.run();

        } finally {
            if (running) {
                clock.resume();
            }
        }
    }

    private void exitLevel() {
        cleanup();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void cleanup() {
        clock.stop();
        stopInterlude();

        if (closeup.isOpen()) {
            closeup.close();
        }

        Sound.stopAll();
    }

    /**
     * Annulla l'interludio ancora in attesa, se ce n'è uno.
     *
     * Fermare la transizione impedisce che il suo esito riavvii il piano
     * dopo che il giocatore ha già lasciato la scena.
     */
    private void stopInterlude() {
        if (interlude != null) {
            interlude.stop();
            interlude = null;
        }
    }

    private List<DilemmaPrompt.Option> promptOptions(
            Dilemma dilemma
    ) {
        return List.of(
                toPromptOption(
                        dilemma.first()
                ),
                toPromptOption(
                        dilemma.second()
                )
        );
    }

    private DilemmaPrompt.Option toPromptOption(
            DilemmaOption option
    ) {
        return new DilemmaPrompt.Option(
                option.text(),
                option.trait()
        );
    }
}

