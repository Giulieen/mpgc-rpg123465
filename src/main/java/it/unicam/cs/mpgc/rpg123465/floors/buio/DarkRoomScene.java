package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;
import it.unicam.cs.mpgc.rpg123465.persistence.TrialRecord;
import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.DilemmaSequence;
import it.unicam.cs.mpgc.rpg123465.questions.QuestionRepository;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;
import it.unicam.cs.mpgc.rpg123465.ui.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.ui.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.ui.support.CloseupOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.CountdownClock;
import it.unicam.cs.mpgc.rpg123465.ui.support.DilemmaPrompt;
import it.unicam.cs.mpgc.rpg123465.ui.support.RoomLighting;
import it.unicam.cs.mpgc.rpg123465.ui.support.ResultOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.SceneFx;
import it.unicam.cs.mpgc.rpg123465.ui.support.TrialStats;
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
    private final QuestionRepository questions;
    private final TrialRecord record;
    private final HeaderBar header;
    private final Consumer<Runnable> onSave;
    private final Runnable onExit;

    private final StackPane root = new StackPane();

    private final RoomLighting lighting = new RoomLighting(root);

    private final CloseupOverlay closeup = new CloseupOverlay(root, () -> closeupOpen = false);

    private final CountdownClock clock = new CountdownClock();

    private final CombinationLockState lockState = new CombinationLockState(4);

    private Consumer<SceneOutcome> onFinished;

    private Region headerView;

    private final ResultOverlay overlay = new ResultOverlay(root, () -> headerView);

    private DarkRoomWalls walls;
    private Label countdown;

    private boolean resolved;
    private boolean closeupOpen;
    private boolean entered;

    private DilemmaSequence dilemmas = new DilemmaSequence(List.of());

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
            QuestionRepository questions,
            RecordStore records,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (room == null || game == null || questions == null || records == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.room = room;
        this.questions = questions;
        /* Sul Buio si misura quanto ci si mette: vince il tempo più basso. */
        this.record = TrialRecord.lowerIsBetter(records, RECORD_KEY);

        this.controller = new DarkRoomController(room, game);

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

        lighting.setToggleGuard(() -> !resolved && !closeupOpen && !dilemmaOpen);

        lighting.setOnChange(() -> {
            if (lighting.isLit() && walls != null) {

                walls.hideNumbers();
            }
        });
    }

    @Override
    public Parent createView(Consumer<SceneOutcome> onFinished) {
        if (onFinished == null) {
            throw new IllegalArgumentException("Il callback di fine scena non può essere null.");
        }

        this.onFinished = onFinished;

        root.getStyleClass().add("fear-root");

        if (!entered) {
            prepareDilemmas();
            entered = true;
        }

        headerView = header.createView();

        header.setProva(room.title());

        updateHeader();

        root.setOnMouseMoved(event -> onMouseMoved(event.getX(), event.getY()));

        showEntryDilemma();

        return root;
    }

    /**
     * Estrae tre domande casuali dal catalogo JSON del Piano II.
     */
    private void prepareDilemmas() {
        dilemmas =
                new DilemmaSequence(questions .randomQuestions("buio", 3));
    }

    private void showEntryDilemma() {
        root.getChildren().setAll(
                SceneFx.contain(root, room.backgroundResource(), SCENE_SCALE),
                headerView
        );

        StackPane.setAlignment(headerView, Pos.TOP_CENTER);

        showNextDilemma(() -> showBriefing(this::startAttempt), false);
    }

    private void showNextDilemma(Runnable afterChoice, boolean pauseClock) {
        if (dilemmaOpen || !dilemmas.hasNext()) {

            if (afterChoice != null) {
                afterChoice.run();
            }

            return;
        }

        if (pauseClock) {
            clock.pause();
        }

        dilemmaOpen = true;

        Dilemma dilemma = dilemmas.current();

        DilemmaPrompt.show(
                root,
                dilemma.question(),
                DilemmaPrompt.optionsOf(dilemma),
                option -> {
                    if (dilemmas.resolve(dilemma)) {
                        controller.registerChoice(option.trait());
                    }

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

    private void showBriefing(Runnable onStart) {
        overlay.show(
                null,
                room.intro(),
                null,
                "Entra nel buio",
                () -> {
                    overlay.hide();
                    onStart.run();
                }
        );
    }

    private void startAttempt() {
        resetAttemptState();

        ImageView background = buildBackground();

        walls = new DarkRoomWalls(root, room.combination());

        Pane hotspots = buildHotspots();

        root.getChildren().setAll(
                background,
                lighting.lampGlow(),
                lighting.darkness(),
                hotspots,
                headerView
        );

        StackPane.setAlignment(headerView, Pos.TOP_CENTER);

        updateHeader();
        startClock();
    }

    /** Riporta la stanza a com'era all'ingresso: buio, serratura vuota. */
    private void resetAttemptState() {
        interlude = null;

        resolved = false;
        closeupOpen = false;
        successWaitingForDilemmas = false;

        lockState.reset();
        lighting.setLit(false);
    }

    /** Lo sfondo, affidato all'illuminazione perché possa oscurarlo. */
    private ImageView buildBackground() {
        ImageView background =
                SceneFx.contain(root, room.backgroundResource(), SCENE_SCALE);

        lighting.attach(background);

        return background;
    }

    /**
     * I punti vivi della stanza: le pareti con i numeri, la tastiera,
     * l'interruttore e il conto alla rovescia.
     *
     * Il pannello non raccoglie i clic sul proprio rettangolo pieno, così il
     * movimento della torcia continua ad arrivare allo sfondo.
     */
    private Pane buildHotspots() {
        Rectangle keypadHotspot = new Rectangle(96, 128, Color.TRANSPARENT);

        keypadHotspot.setCursor(Cursor.HAND);

        keypadHotspot.setOnMouseClicked(event -> openCloseup());

        SceneFx.place(root, keypadHotspot, 96, 128, KEYPAD_X, KEYPAD_Y);

        FontIcon lightSwitch = lighting.switchIcon();

        SceneFx.place(root, lightSwitch, 44, 44, 0.07, 0.90);

        countdown = new Label();

        countdown.getStyleClass().add("dark-timer");

        SceneFx.place(root, countdown, 120, 40, 0.90, 0.15);

        Pane hotspots =
                new Pane(walls.createView(), keypadHotspot, lightSwitch, countdown);

        hotspots.setPickOnBounds(false);

        return hotspots;
    }

    private void onMouseMoved(double x, double y) {
        if (walls == null || lighting.isLit() || resolved || closeupOpen || dilemmaOpen) {

            return;
        }

        walls.updateTorch(lighting.darkness(), x, y, lockState.getActiveSlot());
    }

    private void openCloseup() {
        if (resolved || closeupOpen || dilemmaOpen) {

            return;
        }

        closeupOpen = true;

        CombinationLockView lock = new CombinationLockView(lockState, this::submit);

        closeup.open(lock.createView());

        /*
         * Il close-up copre l'intera scena: senza questo, i tentativi, Salva
         * ed Esci sparirebbero proprio dove il giocatore passa più tempo.
         * Il primo piano resta comunque utilizzabile, perché la barra occupa
         * solo la fascia superiore.
         */
        headerView.toFront();
    }

    private void submit(String code) {
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

        if (dilemmas.hasNext()) {

            successWaitingForDilemmas = true;

            showRemainingDilemmasBeforeSuccess();
            return;
        }

        succeed();
    }

    private void showRemainingDilemmasBeforeSuccess() {
        if (!dilemmas.hasNext()) {

            successWaitingForDilemmas = false;
            succeed();
            return;
        }

        showNextDilemma(this::showRemainingDilemmasBeforeSuccess, false);
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

        Sound.play("/audio/gate-open.mp3", 0.7);

        updateHeader();
        showSuccessResult(elapsed, record.submit(elapsed));
    }

    private void showSuccessResult(int elapsed, int best) {
        overlay.show(
                null,
                room.outro(),
                TrialStats.line(
                        "Tempo: "
                                + TrialStats.time(elapsed),
                        "Record: "
                                + TrialStats.time(best)
                ),
                "Continua",
                () -> {
                    cleanup();

                    onFinished.accept(SceneOutcome.AVANTI);
                }
        );
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

        Sound.play("/audio/padlock-unlock.mp3", 0.35);

        failAttempt(room.wrongText());
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

        failAttempt(room.timeoutText());
    }

    /**
     * Registra un errore grave e decide come prosegue la prova.
     *
     * Finché restano tentativi si torna alla serratura dopo un breve
     * interludio; esauriti, la prova è fallita e va ricominciata da capo.
     */
    private void failAttempt(String message) {
        controller.registerFailedAttempt();
        updateHeader();

        if (controller.canRetry()) {
            showInterlude(message);
        } else {
            showTrialFailed(message);
        }
    }

    private void showInterlude(String message) {
        stopInterlude();

        interlude = SceneFx.interlude(root, message, attemptsNote(), 3.4, this::startAttempt);

        headerView.toFront();
    }

    private String attemptsNote() {
        int left = controller.remainingAttempts();

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
    private void showTrialFailed(String message) {
        overlay.show(
                "Prova fallita",
                message,
                null,
                "Ricomincia la prova",
                () -> {
                    overlay.hide();

                    controller.restartTrial();
                    updateHeader();
                    startAttempt();
                }
        );
    }

    private void startClock() {
        Sound.stopAll();

        Sound.loop("/audio/ambience-dark.mp3", 0.25);

        clock.start(room.seconds(), countdown, this::timeOut, this::onClockSecondChanged);
    }

    /**
     * Mostra la seconda domanda a 40 secondi e la terza a 20.
     * Se il close-up della serratura è aperto, aspetta il tick
     * successivo invece di coprire l'interazione in corso.
     */
    private void onClockSecondChanged(int remaining) {
        if (resolved || dilemmaOpen || closeupOpen) {

            return;
        }

        if (dilemmas.resolvedCount() == 1 && remaining <= SECOND_DILEMMA_AT) {

            showNextDilemma(null, true);

            return;
        }

        if (dilemmas.resolvedCount() == 2 && remaining <= THIRD_DILEMMA_AT) {

            showNextDilemma(null, true);
        }
    }

    private void updateHeader() {
        header.setTentativi(controller.remainingAttempts(), controller.maxAttempts());
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

        onSave.accept(() -> { if (running) { clock.resume(); } });
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
}

