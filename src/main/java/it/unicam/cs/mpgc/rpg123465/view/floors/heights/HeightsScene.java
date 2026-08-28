package it.unicam.cs.mpgc.rpg123465.view.floors.heights;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;
import it.unicam.cs.mpgc.rpg123465.controller.HeightsController;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.DilemmaSequence;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.HeightsConfig;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.ArrowChallenge;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.ArrowDirection;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.BridgeRoute;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.LevelState;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;
import it.unicam.cs.mpgc.rpg123465.persistence.record.RecordStore;
import it.unicam.cs.mpgc.rpg123465.persistence.record.TrialRecord;
import it.unicam.cs.mpgc.rpg123465.view.FloorScene;
import it.unicam.cs.mpgc.rpg123465.view.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.view.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.view.components.KeyboardBinding;
import it.unicam.cs.mpgc.rpg123465.view.components.CountdownClock;
import it.unicam.cs.mpgc.rpg123465.view.components.DilemmaPrompt;
import it.unicam.cs.mpgc.rpg123465.view.components.ResultOverlay;
import it.unicam.cs.mpgc.rpg123465.view.components.SceneFx;
import it.unicam.cs.mpgc.rpg123465.view.components.TrialStats;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.scene.CacheHint;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Piano III — Le Altezze.
 *
 * Il giocatore attraversa l'abisso mantenendo l'equilibrio:
 * quando compare una freccia deve premere quella opposta.
 *
 * A ogni cambio ponte viene proposto un dilemma "Preferiresti".
 * Durante la domanda il timer della traversata viene messo in pausa.
 */
public class HeightsScene implements FloorScene {

    private static final double STAGE_W = 1600;
    private static final double STAGE_H = 900;

    private static final double MOM_STEP = 0.05;
    private static final double MOM_CAP = 6.0;
    private static final double ADV_FACTOR = 0.15;
    private static final double MIN_INTERVAL = 230;
    private static final double MIN_RESPONSE = 350;

    /** Identifica il record del piano nell'archivio condiviso. */
    private static final String RECORD_KEY = "altezze.punteggio";

    private final AltitudeCrossing crossing;
    private final HeightsConfig config;
    private final HeightsController controller;
    private final QuestionRepository questions;
    private final TrialRecord record;
    private final HeaderBar header;
    private final Consumer<Runnable> onSave;
    private final Runnable onExit;

    private final StackPane root = new StackPane();

    private final CountdownClock clock = new CountdownClock();

    private final Random rng = new Random();

    private Consumer<SceneOutcome> onFinished;

    private Region headerView;
    private HeightsHud hud;


    private final KeyboardBinding keyboard = new KeyboardBinding(this::onKeyPressed);

    private LevelState state = LevelState.INTRO;

    /** Ponte attuale, avanzamento e soglie di cambio. */
    private final BridgeRoute route;

    private int balance;

    /** Regole e punteggio delle frecce; la scena ne governa solo i tempi. */
    private final ArrowChallenge arrows = new ArrowChallenge(rng);

    /** Record del piano, mostrato accanto al punteggio della traversata. */
    private int best;

    private boolean cleanRun = true;
    private boolean entered;

    private AnimationTimer advance;

    private double lastZoom = 1;
    private long lastFrame;

    private PauseTransition arrowDelay;
    private PauseTransition responseTimer;
    private PauseTransition rerouteDeadline;

    private Group stage;
    private ImageView bg;
    private Rectangle dimOverlay;
    private BridgeBeacons beacons;
    private final ResultOverlay overlay = new ResultOverlay(root, () -> headerView);

    /*
     * Le quattro domande vengono mescolate una volta entrando nel piano.
     * L'indice non viene azzerato dopo una caduta, così le risposte
     * non vengono conteggiate due volte nello stesso piano.
     */
    private DilemmaSequence dilemmas = new DilemmaSequence(List.of());

    private boolean dilemmaOpen;

    public HeightsScene(
            AltitudeCrossing crossing,
            GameController game,
            QuestionRepository questions,
            RecordStore records,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (crossing == null || game == null || questions == null || records == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.crossing = crossing;
        this.config = crossing.config();
        this.route = new BridgeRoute(config, rng);
        this.questions = questions;
        /* Sulle Altezze si misura il punteggio: vince il più alto. */
        this.record = TrialRecord.higherIsBetter(records, RECORD_KEY);
        this.onSave = onSave;
        this.onExit = onExit;

        this.controller = new HeightsController(crossing, game);

        this.header =
                new HeaderBar(
                        game.getPlayerName(),
                        onSave == null
                                ? null
                                : this::saveWithGameplaySuspended,
                        onExit == null
                                ? null
                                : this::exitLevel
                );
    }

    @Override
    public Parent createView(Consumer<SceneOutcome> onFinished) {
        if (onFinished == null) {
            throw new IllegalArgumentException("Il callback di fine scena non può essere null.");
        }

        this.onFinished = onFinished;

        root.getStyleClass().add("fear-root");

        /*
         * Le domande si estraggono una volta sola: rientrare dopo una caduta
         * non deve rimescolarle.
         */
        if (!entered) {
            prepareDilemmas();
            entered = true;
        }

        best =
                record.best()
                        .orElse(0);

        buildScene();
        bindInput();
        startFrames();

        startAmbience();
        showIntro();

        return root;
    }

    /** Fondale, ponti, fari e barra di gioco. */
    private void buildScene() {
        headerView = header.createView();

        header.setProva(crossing.title());

        buildStage();

        beacons = new BridgeBeacons(root, dimOverlay);

        hud =
                new HeightsHud(root, config.balancePoints(), headerView, this::handleArrowInput);

        balance = config.balancePoints();

        hud.setBalance(balance);

        hud.setStats(arrows.score(), best, arrows.streak());
    }

    /**
     * Mouse e tastiera.
     *
     * I tasti si legano quando la scena entra in una finestra, non adesso: a
     * questo punto la Scene non esiste ancora.
     */
    private void bindInput() {
        root.setOnMouseClicked(this::onSceneClick);

        root.setOnMouseMoved(this::onSceneMove);

        root.sceneProperty().addListener((obs, oldScene, newScene) -> keyboard.bindTo(newScene));
    }

    /** Avvia il battito che fa avanzare la traversata a ogni fotogramma. */
    private void startFrames() {
        advance =
                new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        onFrame(now);
                    }
                };

        advance.start();
    }

    // ---------------------------------------------------------------------
    // Dilemmi
    // ---------------------------------------------------------------------

    private void prepareDilemmas() {
        dilemmas =
                new DilemmaSequence(questions.randomQuestions("altezze", 4));
    }

    /**
     * Mostra la prossima domanda, se ce n'è ancora una disponibile.
     * Il countdown resta fermo finché il giocatore non risponde.
     */
    private void showRerouteDilemma() {
        if (!dilemmas.hasNext()) {
            proceedReroute();
            return;
        }

        dilemmaOpen = true;
        clock.pause();

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

                    clock.resume();
                    proceedReroute();
                },
                headerView
        );
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    private void buildStage() {
        bg =
                new ImageView(SceneFx.image(crossing.backgroundResource()));

        bg.setFitWidth(STAGE_W);

        bg.setFitHeight(STAGE_H);

        /*
         * Lo zoom cambia a ogni fotogramma: senza cache JavaFX ricampiona
         * l'immagine intera sessanta volte al secondo. Con la cache scala una
         * copia gia' disegnata, e la traversata resta reattiva anche sugli
         * schermi ad alta densita', dove i pixel da riempire sono il quadruplo.
         */
        bg.setCache(true);

        bg.setCacheHint(CacheHint.SPEED);

        dimOverlay =
                new Rectangle(STAGE_W, STAGE_H, Color.rgb(0, 0, 0, 0.6));

        dimOverlay.setVisible(false);

        dimOverlay.setMouseTransparent(true);

        stage = new Group(bg, dimOverlay);

        stage.scaleXProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(root.getWidth() / STAGE_W, root.getHeight() / STAGE_H),
                        root.widthProperty(),
                        root.heightProperty()
                )
        );

        stage.scaleYProperty().bind(stage.scaleXProperty());

        root.getChildren().add(stage);
    }

    private void startAmbience() {
        Sound.stopAll();

        Sound.loop("/audio/ambience-wind.mp3", 0.30);
    }

    // ---------------------------------------------------------------------
    // Avanzamento
    // ---------------------------------------------------------------------

    private void onFrame(long now) {
        if (lastFrame == 0) {
            lastFrame = now;
            return;
        }

        double dt =
                (now - lastFrame)
                        / 1_000_000_000.0;

        lastFrame = now;

        /*
         * Frame troppo distante dal precedente: il thread grafico era occupato
         * (per esempio da una modale) e la traversata non stava avanzando.
         * Il riferimento è già riallineato: saltiamo il frame invece di
         * spingere il giocatore in avanti di tutta la pausa.
         */
        if (dt > SceneFx.MAX_FRAME_SECONDS) {
            return;
        }

        if (state != LevelState.PLAYING || !route.hasBridge()) {

            return;
        }

        double advanceBoost =
                1
                        + (momentum() - 1)
                        * ADV_FACTOR;

        route.advance(config.bridge(route.current()).advancePerSecond() * advanceBoost * dt);

        if (route.isComplete()) {
            win();
            return;
        }

        double zoom =
                1
                        + 0.13
                        * route.progress()
                        / 100.0;

        /*
         * Lo zoom cresce di 0,13 lungo l'intera traversata: fra un fotogramma e
         * il successivo la variazione e' invisibile, ma basta a far ridisegnare
         * il fondale. Aggiorniamo solo quando lo scarto si vede davvero, cosi'
         * il thread resta libero per le frecce.
         */
        if (Math.abs(zoom - lastZoom) >= 0.004) {
            lastZoom = zoom;

            bg.setScaleX(zoom);

            bg.setScaleY(zoom);
        }

        maybeReroute();
    }

    private void maybeReroute() {
        if (route.consumeThreshold()) {
            startReroute();
        }
    }

    // ---------------------------------------------------------------------
    // Ingresso
    // ---------------------------------------------------------------------

    private void showIntro() {
        state = LevelState.INTRO;

        hud.showDpad(false);

        beacons.hide();

        overlay.show(
                crossing.title(),
                crossing.intro(),
                null,
                "Scegli il ponte",
                this::toSelection
        );
    }

    private void toSelection() {
        overlay.hide();

        state = LevelState.BRIDGE_SELECTION;

        hud.showDpad(false);

        hud.setHover("Scegli uno dei tre ponti.");

        beacons.showAll();
    }

    private void selectBridge(int index) {
        route.select(index);

        beacons.hide();

        hud.setHover("");

        startClock();
        beginPlaying();
    }

    private void startClock() {
        clock.start(config.totalSeconds(), hud.countdown(), this::onTimeUp);
    }

    private void beginPlaying() {
        state = LevelState.PLAYING;

        hud.setChannel(null);

        hud.setStats(arrows.score(), best, arrows.streak());

        hud.showDpad(true);

        scheduleArrow();
    }

    // ---------------------------------------------------------------------
    // Frecce
    // ---------------------------------------------------------------------

    private void scheduleArrow() {
        cancel(arrowDelay);

        if (state != LevelState.PLAYING) {

            return;
        }

        double interval =
                Math.max(
                        MIN_INTERVAL,
                        config
                                .bridge(route.current())
                                .arrowIntervalMs()
                                / momentum()
                );

        arrowDelay = new PauseTransition(Duration.millis(interval));

        arrowDelay.setOnFinished(event -> spawnArrow());

        arrowDelay.play();
    }

    private void spawnArrow() {
        if (state != LevelState.PLAYING) {

            return;
        }

        ArrowDirection shown = arrows.show();

        double response =
                Math.max(
                        MIN_RESPONSE,
                        config
                                .bridge(route.current())
                                .responseMs()
                                / Math.sqrt(momentum())
                );

        hud.showArrow(shown, response);

        cancel(responseTimer);

        responseTimer = new PauseTransition(Duration.millis(response));

        responseTimer.setOnFinished(event -> resolveArrow(null));

        responseTimer.play();
    }

    private void handleArrowInput(ArrowDirection pressed) {
        if (state != LevelState.PLAYING || !arrows.isWaiting()) {

            return;
        }

        resolveArrow(pressed);
    }

    /**
     * @param pressed direzione premuta, oppure null se il tempo è scaduto
     */
    private void resolveArrow(ArrowDirection pressed) {
        if (!arrows.isWaiting()) {
            return;
        }

        cancel(responseTimer);

        hud.hideArrow();

        if (arrows.resolve(pressed)) {
            if (arrows.score() > best) {
                best = arrows.score();
            }

            hud.setStats(arrows.score(), best, arrows.streak());

            Sound.play("/audio/arrow-tap.wav", 0.6);

            scheduleArrow();

        } else {
            loseBalance();

            if (state == LevelState.PLAYING) {

                scheduleArrow();
            }
        }
    }

    private void loseBalance() {
        balance--;
        cleanRun = false;

        hud.setBalance(balance);

        hud.setStats(arrows.score(), best, arrows.streak());

        Sound.play("/audio/arrow-wrong.wav", 0.6);

        shake();

        if (balance <= 0) {
            fall();
        }
    }

    private double momentum() {
        return Math.min(MOM_CAP, 1 + arrows.streak() * MOM_STEP);
    }

    // ---------------------------------------------------------------------
    // Cambio ponte
    // ---------------------------------------------------------------------

    /**
     * Ferma il gameplay, apre il dilemma e solo dopo la risposta
     * avvia il passaggio verso il nuovo ponte.
     */
    private void startReroute() {
        state = LevelState.CHANGING_BRIDGE;

        hud.showDpad(false);

        cancel(arrowDelay);

        cancel(responseTimer);

        /*
         * La freccia a schermo viene scartata, non sbagliata: il cambio di
         * ponte non dipende dal giocatore e non deve costargli un errore.
         */
        arrows.discard();

        hud.hideArrow();

        showRerouteDilemma();
    }

    private void proceedReroute() {
        beacons.illuminate(route.chooseDestination());

        hud.showReroute("Il percorso cede — clicca il ponte illuminato.");

        Sound.play("/audio/torch-whoosh.wav", 0.5);

        cancel(rerouteDeadline);

        rerouteDeadline =
                new PauseTransition(Duration.seconds(config.rerouteSeconds()));

        rerouteDeadline.setOnFinished(event -> fall());

        rerouteDeadline.play();
    }

    private void performJump() {
        cancel(rerouteDeadline);

        route.jump();

        beacons.hide();

        hud.hideReroute();

        Sound.play("/audio/wood-step.wav", 0.6);

        beginPlaying();
    }

    // ---------------------------------------------------------------------
    // Ponti
    // ---------------------------------------------------------------------

    private void onSceneClick(MouseEvent event) {
        if (dilemmaOpen) {
            return;
        }

        int index = beacons.at(event.getX(), event.getY());

        if (index < 0) {
            return;
        }

        if (state == LevelState.BRIDGE_SELECTION) {

            selectBridge(index);

        } else if (state == LevelState.CHANGING_BRIDGE && route.isDestination(index)) {

            performJump();
        }
    }

    private void onSceneMove(MouseEvent event) {
        if (state != LevelState.BRIDGE_SELECTION || dilemmaOpen) {

            return;
        }

        int index = beacons.at(event.getX(), event.getY());

        beacons.hover(index);

        if (index >= 0) {
            hud.setHover("Scegli questo ponte.");
        } else {
            hud.setHover("Scegli uno dei tre ponti.");
        }
    }

    // ---------------------------------------------------------------------
    // Vittoria
    // ---------------------------------------------------------------------

    private void win() {
        if (state == LevelState.VICTORY) {

            return;
        }

        state = LevelState.VICTORY;

        endGameplay();

        Sound.play("/audio/gate-open.mp3", 0.7);

        submitRecord();

        updateHeader();

        showVictoryResult();
    }

    /** Conserva il punteggio migliore raggiunto nella traversata. */
    private void submitRecord() {
        best = record.submit(best);
    }

    private void showVictoryResult() {
        String message =
                cleanRun
                        ? crossing.victory()
                                + "\n\nAttraversata senza un solo errore."
                        : crossing.victory();

        overlay.show("L'altra sponda", message, trialStats(), "Prosegui", this::finish);
    }

    /**
     * Riepilogo della traversata: quante frecce sono state indovinate, quante
     * sbagliate, il punteggio di questa prova e il record dell'installazione.
     */
    private Label trialStats() {
        return TrialStats.line(
                "Frecce indovinate: "
                        + arrows.hits(),
                "Errori: "
                        + arrows.misses(),
                "Punteggio: "
                        + arrows.score(),
                "Record: "
                        + best
        );
    }

    // ---------------------------------------------------------------------
    // Caduta
    // ---------------------------------------------------------------------

    /**
     * Ferma il gioco lasciando la scena a schermo.
     *
     * Vittoria e caduta arrivano da strade opposte ma spengono le stesse cose:
     * i comandi, gli indicatori e i tempi.
     */
    private void endGameplay() {
        hud.showDpad(false);

        hud.hideArrow();

        beacons.hide();

        stopTimers();

        Sound.stopAll();
    }

    private void fall() {
        if (state == LevelState.GAME_OVER || state == LevelState.VICTORY) {

            return;
        }

        state = LevelState.GAME_OVER;

        dilemmaOpen = false;

        endGameplay();

        hud.hideReroute();

        playFallAudio();

        controller.registerFall();

        submitRecord();
        updateHeader();

        showFallResult(controller.canRetry());
    }

    /** Legno che cede, urlo e caduta, sfalsati per farli sentire in sequenza. */
    private void playFallAudio() {
        Sound.play("/audio/wood-break.wav", 0.85);

        Sound.play(new SoundCue("/audio/scream.wav", 0.85, 0.25));

        Sound.play(new SoundCue("/audio/fall.wav", 0.9, 0.75));
    }

    /**
     * Il buio cala sulla scena e solo quando è pieno compare il cartello:
     * mostrarlo subito sopra il ponte toglierebbe alla caduta il suo tempo.
     *
     * @param canRetry se restano tentativi per riprovare la traversata
     */
    private void showFallResult(boolean canRetry) {
        String message =
                crossing.gameOver()
                        + (canRetry
                                ? "\n\n" + attemptsNote()
                                : "\n\nTentativi esauriti: la prova ricomincia da capo.");

        Rectangle black = SceneFx.veil(root, 1.0);

        black.setOpacity(0);

        root.getChildren().add(black);

        if (headerView != null) {
            headerView.toFront();
        }

        FadeTransition drop = new FadeTransition(Duration.millis(700), black);

        drop.setFromValue(0);

        drop.setToValue(1);

        drop.setOnFinished(
                event -> {
                    root
                            .getChildren()
                            .remove(black);

                    overlay.show(
                            canRetry
                                    ? "Sei caduto"
                                    : "Prova fallita",
                            message,
                            null,
                            canRetry
                                    ? "Riprova"
                                    : "Ricomincia la prova",
                            canRetry
                                    ? this::restartLevel
                                    : this::restartTrial
                    );
                }
        );

        drop.play();
    }

    /**
     * Tentativi esauriti: si riparte con i tentativi al massimo.
     *
     * Le domande già risposte non vengono riproposte, quindi ricominciare
     * non altera il profilo.
     */
    private void restartTrial() {
        controller.restartTrial();
        updateHeader();
        restartLevel();
    }

    private String attemptsNote() {
        int left = controller.remainingAttempts();

        return left == 1
                ? "Ti resta un tentativo."
                : "Ti restano " + left + " tentativi.";
    }

    private void restartLevel() {
        stopTimers();
        overlay.hide();

        route.restart();

        balance = config.balancePoints();

        arrows.restart();

        cleanRun = true;
        dilemmaOpen = false;

        hud.hideArrow();
        hud.hideReroute();

        hud.setStats(arrows.score(), best, arrows.streak());

        hud.setBalance(balance);

        hud.setChannel(null);

        bg.setScaleX(1);

        bg.setScaleY(1);

        startAmbience();

        /*
         * La sequenza non viene ricreata: le domande già risposte non
         * vengono conteggiate di nuovo.
         */
        toSelection();
    }

    private void finish() {
        cleanup();

        onFinished.accept(SceneOutcome.NEXT);
    }

    private void onTimeUp() {
        if (state == LevelState.VICTORY || state == LevelState.GAME_OVER) {

            return;
        }

        fall();
    }

    // ---------------------------------------------------------------------
    // Tastiera
    // ---------------------------------------------------------------------

    private void onKeyPressed(KeyEvent event) {
        if (state != LevelState.PLAYING || dilemmaOpen) {

            return;
        }

        ArrowDirection direction =
                switch (event.getCode()) {
                    case UP, W ->
                            ArrowDirection.UP;

                    case DOWN, S ->
                            ArrowDirection.DOWN;

                    case LEFT, A ->
                            ArrowDirection.LEFT;

                    case RIGHT, D ->
                            ArrowDirection.RIGHT;

                    default ->
                            null;
                };

        if (direction != null) {
            handleArrowInput(direction);

            event.consume();
        }
    }

    // ---------------------------------------------------------------------
    // Effetti
    // ---------------------------------------------------------------------

    private void shake() {
        Timeline shake = new Timeline();

        for (int i = 1; i <= 6; i++) {
            double decay =
                    1.0
                            - i
                            / 7.0;

            double dx =
                    (rng.nextDouble() - 0.5)
                            * 26
                            * decay;

            shake
                    .getKeyFrames()
                    .add(
                            new KeyFrame(
                                    Duration.millis(i * 45),
                                    new KeyValue(stage.translateXProperty(), dx)
                            )
                    );
        }

        shake
                .getKeyFrames()
                .add(
                        new KeyFrame(
                                Duration.millis(7 * 45),
                                new KeyValue(stage.translateXProperty(), 0)
                        )
                );

        shake.play();
    }

    private void updateHeader() {
        header.setTentativi(controller.remainingAttempts(), controller.maxAttempts());
    }

    // ---------------------------------------------------------------------
    // Overlay
    // ---------------------------------------------------------------------

    private void stopTimers() {
        clock.stop();

        cancel(arrowDelay);

        cancel(responseTimer);

        cancel(rerouteDeadline);
    }

    /**
     * Salva tenendo ferma la traversata.
     *
     * La conferma del salvataggio è una finestra modale, e una modale JavaFX
     * gira in un event loop annidato: i pulse continuano. Senza sospendere,
     * mentre il giocatore legge il messaggio il countdown scorrerebbe, il
     * personaggio avanzerebbe verso l'altra sponda e una freccia potrebbe
     * scadere da sola facendogli perdere l'equilibrio.
     *
     * Si ferma tutto ciò che misura il tempo e si riprende com'era: il
     * countdown solo se stava correndo, le attese solo se erano in corso, e
     * l'avanzamento riazzerando il riferimento del frame perché il primo dopo
     * la modale non recuperi la pausa.
     */
    private void saveWithGameplaySuspended() {
        boolean clockRunning = !clock.isPaused();

        clock.pause();

        pauseIfRunning(arrowDelay);
        pauseIfRunning(responseTimer);
        pauseIfRunning(rerouteDeadline);

        if (advance != null) {
            advance.stop();
        }

        onSave.accept(() -> {
            if (advance != null) {
                lastFrame = 0;
                advance.start();
            }

            resumeIfPaused(arrowDelay);
            resumeIfPaused(responseTimer);
            resumeIfPaused(rerouteDeadline);

            if (clockRunning) {
                clock.resume();
            }
        });
    }

    private void pauseIfRunning(PauseTransition transition) {
        if (transition != null && transition.getStatus() == Animation.Status.RUNNING) {

            transition.pause();
        }
    }

    private void resumeIfPaused(PauseTransition transition) {
        if (transition != null && transition.getStatus() == Animation.Status.PAUSED) {

            transition.play();
        }
    }

    private void exitLevel() {
        cleanup();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void cleanup() {
        stopTimers();

        beacons.stopPulse();

        if (advance != null) {
            advance.stop();
            advance = null;
        }

        keyboard.release();

        Sound.stopAll();
    }

    private void cancel(PauseTransition transition) {
        if (transition != null) {
            transition.stop();
        }
    }
}

