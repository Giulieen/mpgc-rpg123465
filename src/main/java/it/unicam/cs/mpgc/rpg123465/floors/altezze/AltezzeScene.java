package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.DilemmaOption;
import it.unicam.cs.mpgc.rpg123465.questions.Questions;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;
import it.unicam.cs.mpgc.rpg123465.ui.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.ui.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.ui.support.CountdownClock;
import it.unicam.cs.mpgc.rpg123465.ui.support.DilemmaPrompt;
import it.unicam.cs.mpgc.rpg123465.ui.support.SceneFx;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
public class AltezzeScene implements FloorScene {

    private static final double STAGE_W = 1600;
    private static final double STAGE_H = 900;

    private static final double MOM_STEP = 0.05;
    private static final double MOM_CAP = 6.0;
    private static final double ADV_FACTOR = 0.15;
    private static final double MIN_INTERVAL = 230;
    private static final double MIN_RESPONSE = 350;

    private final AltitudeCrossing crossing;
    private final AltezzeConfig config;
    private final AltezzeController controller;
    private final HeaderBar header;
    private final Runnable onSave;
    private final Runnable onExit;

    private final StackPane root =
            new StackPane();

    private final CountdownClock clock =
            new CountdownClock();

    private final Random rng =
            new Random();

    private Consumer<SceneOutcome> onFinished;

    private Region headerView;
    private AltezzeHud hud;

    private Scene boundScene;

    private final javafx.event.EventHandler<KeyEvent> keyHandler =
            this::onKeyPressed;

    private LevelState state =
            LevelState.INTRO;

    private int currentBridge = -1;
    private int destinationBridge = -1;

    private double progress;

    private int balance;
    private int nextThreshold;
    private int routeStep;

    private ArrowDirection currentArrow;
    private ArrowDirection lastArrow;

    private int sameDirCount;
    private int streak;
    private int score;
    private int best;

    private boolean cleanRun = true;
    private boolean entered;

    private AnimationTimer advance;
    private long lastFrame;

    private PauseTransition arrowDelay;
    private PauseTransition responseTimer;
    private PauseTransition rerouteDeadline;

    private Group stage;
    private ImageView bg;
    private Rectangle dimOverlay;
    private BridgeBeacons beacons;
    private StackPane overlay;

    /*
     * Le quattro domande vengono mescolate una volta entrando nel piano.
     * L'indice non viene azzerato dopo una caduta, così le risposte
     * non vengono conteggiate due volte nello stesso piano.
     */
    private List<Dilemma> dilemmas =
            List.of();

    private int nextDilemmaIndex;

    private boolean dilemmaOpen;

    public AltezzeScene(
            AltitudeCrossing crossing,
            GameController game
    ) {
        this(
                crossing,
                game,
                null,
                null
        );
    }

    public AltezzeScene(
            AltitudeCrossing crossing,
            GameController game,
            Runnable onSave
    ) {
        this(
                crossing,
                game,
                onSave,
                null
        );
    }

    public AltezzeScene(
            AltitudeCrossing crossing,
            GameController game,
            Runnable onSave,
            Runnable onExit
    ) {
        if (crossing == null || game == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.crossing = crossing;
        this.config = crossing.config();
        this.onSave = onSave;
        this.onExit = onExit;

        this.controller =
                new AltezzeController(
                        crossing,
                        game
                );

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

        best =
                AltezzeRecord.load();

        headerView =
                header.createView();

        header.setProva(
                crossing.title()
        );

        buildStage();

        beacons =
                new BridgeBeacons(
                        root,
                        dimOverlay
                );

        hud =
                new AltezzeHud(
                        root,
                        config.balancePoints(),
                        headerView,
                        this::handleArrowInput
                );

        balance =
                config.balancePoints();

        hud.setBalance(
                balance
        );

        hud.setStats(
                score,
                best,
                streak
        );

        root.setOnMouseClicked(
                this::onSceneClick
        );

        root.setOnMouseMoved(
                this::onSceneMove
        );

        root.sceneProperty().addListener(
                (obs, oldScene, newScene) ->
                        bindKeys(newScene)
        );

        advance =
                new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        onFrame(now);
                    }
                };

        advance.start();

        startAmbience();

        showIntro();

        return root;
    }

    // ---------------------------------------------------------------------
    // Dilemmi
    // ---------------------------------------------------------------------

    private void prepareDilemmas() {
        dilemmas =
                Questions.repository()
                        .randomQuestions(
                                "altezze",
                                4
                        );

        nextDilemmaIndex = 0;
    }

    /**
     * Mostra la prossima domanda, se ce n'è ancora una disponibile.
     * Il countdown resta fermo finché il giocatore non risponde.
     */
    private void showRerouteDilemma() {
        if (nextDilemmaIndex
                >= dilemmas.size()) {

            proceedReroute();
            return;
        }

        dilemmaOpen = true;
        clock.pause();

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

                    clock.resume();
                    proceedReroute();
                },
                headerView
        );
    }

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------

    private void bindKeys(
            Scene scene
    ) {
        if (boundScene != null) {
            boundScene.removeEventFilter(
                    KeyEvent.KEY_PRESSED,
                    keyHandler
            );
        }

        boundScene = scene;

        if (scene != null) {
            scene.addEventFilter(
                    KeyEvent.KEY_PRESSED,
                    keyHandler
            );
        }
    }

    private void buildStage() {
        bg =
                new ImageView(
                        SceneFx.image(
                                crossing.backgroundResource()
                        )
                );

        bg.setFitWidth(
                STAGE_W
        );

        bg.setFitHeight(
                STAGE_H
        );

        dimOverlay =
                new Rectangle(
                        STAGE_W,
                        STAGE_H,
                        Color.rgb(
                                0,
                                0,
                                0,
                                0.6
                        )
                );

        dimOverlay.setVisible(
                false
        );

        dimOverlay.setMouseTransparent(
                true
        );

        stage =
                new Group(
                        bg,
                        dimOverlay
                );

        stage.scaleXProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.min(
                                root.getWidth()
                                        / STAGE_W,
                                root.getHeight()
                                        / STAGE_H
                        ),
                        root.widthProperty(),
                        root.heightProperty()
                )
        );

        stage.scaleYProperty().bind(
                stage.scaleXProperty()
        );

        root.getChildren().add(
                stage
        );
    }

    private void startAmbience() {
        Sound.stopAll();

        Sound.loop(
                "/audio/ambience-wind.mp3",
                0.30
        );
    }

    // ---------------------------------------------------------------------
    // Avanzamento
    // ---------------------------------------------------------------------

    private void onFrame(
            long now
    ) {
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

        if (state != LevelState.PLAYING
                || currentBridge < 0) {

            return;
        }

        double advanceBoost =
                1
                        + (momentum() - 1)
                        * ADV_FACTOR;

        progress +=
                config
                        .bridge(
                                currentBridge
                        )
                        .advancePerSecond()
                        * advanceBoost
                        * dt;

        if (progress >= 100) {
            progress = 100;
            win();
            return;
        }

        double zoom =
                1
                        + 0.13
                        * progress
                        / 100.0;

        bg.setScaleX(
                zoom
        );

        bg.setScaleY(
                zoom
        );

        maybeReroute();
    }

    private void maybeReroute() {
        double[] thresholds =
                config.routeThresholds();

        if (nextThreshold
                < thresholds.length
                && progress
                >= thresholds[nextThreshold]) {

            nextThreshold++;
            startReroute();
        }
    }

    // ---------------------------------------------------------------------
    // Ingresso
    // ---------------------------------------------------------------------

    private void showIntro() {
        state =
                LevelState.INTRO;

        hud.showDpad(
                false
        );

        beacons.hide();

        showOverlay(
                crossing.title(),
                crossing.intro(),
                "Scegli il ponte",
                this::toSelection
        );
    }

    private void toSelection() {
        hideOverlay();

        state =
                LevelState.BRIDGE_SELECTION;

        hud.showDpad(
                false
        );

        hud.setHover(
                "Scegli uno dei tre ponti."
        );

        beacons.showAll();
    }

    private void selectBridge(
            int index
    ) {
        currentBridge = index;

        beacons.hide();

        hud.setHover("");

        startClock();
        beginPlaying();
    }

    private void startClock() {
        clock.start(
                config.totalSeconds(),
                hud.countdown(),
                this::onTimeUp
        );
    }

    private void beginPlaying() {
        state =
                LevelState.PLAYING;

        hud.setChannel(
                null
        );

        hud.setStats(
                score,
                best,
                streak
        );

        hud.showDpad(
                true
        );

        scheduleArrow();
    }

    // ---------------------------------------------------------------------
    // Frecce
    // ---------------------------------------------------------------------

    private void scheduleArrow() {
        cancel(
                arrowDelay
        );

        if (state
                != LevelState.PLAYING) {

            return;
        }

        double interval =
                Math.max(
                        MIN_INTERVAL,
                        config
                                .bridge(
                                        currentBridge
                                )
                                .arrowIntervalMs()
                                / momentum()
                );

        arrowDelay =
                new PauseTransition(
                        Duration.millis(
                                interval
                        )
                );

        arrowDelay.setOnFinished(
                event -> spawnArrow()
        );

        arrowDelay.play();
    }

    private void spawnArrow() {
        if (state
                != LevelState.PLAYING) {

            return;
        }

        currentArrow =
                pickDirection();

        double response =
                Math.max(
                        MIN_RESPONSE,
                        config
                                .bridge(
                                        currentBridge
                                )
                                .responseMs()
                                / Math.sqrt(
                                        momentum()
                                )
                );

        hud.showArrow(
                currentArrow,
                response
        );

        cancel(
                responseTimer
        );

        responseTimer =
                new PauseTransition(
                        Duration.millis(
                                response
                        )
                );

        responseTimer.setOnFinished(
                event -> resolveArrow(
                        false
                )
        );

        responseTimer.play();
    }

    private ArrowDirection pickDirection() {
        ArrowDirection[] all =
                ArrowDirection.values();

        ArrowDirection chosen;

        do {
            chosen =
                    all[
                            rng.nextInt(
                                    all.length
                            )
                            ];

        } while (chosen == lastArrow
                && sameDirCount >= 2);

        if (chosen == lastArrow) {
            sameDirCount++;
        } else {
            lastArrow = chosen;
            sameDirCount = 1;
        }

        return chosen;
    }

    private void handleArrowInput(
            ArrowDirection pressed
    ) {
        if (state
                != LevelState.PLAYING
                || currentArrow == null) {

            return;
        }

        resolveArrow(
                pressed
                        == currentArrow.opposite()
        );
    }

    private void resolveArrow(
            boolean correct
    ) {
        if (currentArrow == null) {
            return;
        }

        cancel(
                responseTimer
        );

        currentArrow = null;

        hud.hideArrow();

        if (correct) {
            streak++;

            score +=
                    10
                            + streak;

            if (score > best) {
                best = score;
            }

            hud.setStats(
                    score,
                    best,
                    streak
            );

            Sound.play(
                    "/audio/arrow-tap.mp3",
                    0.6
            );

            scheduleArrow();

        } else {
            loseBalance();

            if (state
                    == LevelState.PLAYING) {

                scheduleArrow();
            }
        }
    }

    private void loseBalance() {
        balance--;
        streak = 0;
        cleanRun = false;

        hud.setBalance(
                balance
        );

        hud.setStats(
                score,
                best,
                streak
        );

        Sound.play(
                "/audio/arrow-wrong.mp3",
                0.6
        );

        shake();

        if (balance <= 0) {
            fall();
        }
    }

    private double momentum() {
        return Math.min(
                MOM_CAP,
                1
                        + streak
                        * MOM_STEP
        );
    }

    // ---------------------------------------------------------------------
    // Cambio ponte
    // ---------------------------------------------------------------------

    /**
     * Ferma il gameplay, apre il dilemma e solo dopo la risposta
     * avvia il passaggio verso il nuovo ponte.
     */
    private void startReroute() {
        state =
                LevelState.CHANGING_BRIDGE;

        hud.showDpad(
                false
        );

        cancel(
                arrowDelay
        );

        cancel(
                responseTimer
        );

        currentArrow = null;

        hud.hideArrow();

        showRerouteDilemma();
    }

    private void proceedReroute() {
        destinationBridge =
                chooseDestination();

        beacons.illuminate(
                destinationBridge
        );

        hud.showReroute(
                "Il percorso cede — clicca il ponte illuminato."
        );

        Sound.play(
                "/audio/torch-whoosh.mp3",
                0.5
        );

        cancel(
                rerouteDeadline
        );

        rerouteDeadline =
                new PauseTransition(
                        Duration.seconds(
                                config.rerouteSeconds()
                        )
                );

        rerouteDeadline.setOnFinished(
                event -> fall()
        );

        rerouteDeadline.play();
    }

    private int chooseDestination() {
        int[] manual =
                config.manualRoute();

        if (manual != null
                && routeStep
                < manual.length) {

            return manual[
                    routeStep++
                    ];
        }

        routeStep++;

        int offset =
                rng.nextInt(2)
                        + 1;

        return (currentBridge + offset)
                % 3;
    }

    private void performJump() {
        cancel(
                rerouteDeadline
        );

        currentBridge =
                destinationBridge;

        destinationBridge = -1;

        beacons.hide();

        hud.hideReroute();

        Sound.play(
                "/audio/wood-step.mp3",
                0.6
        );

        beginPlaying();
    }

    // ---------------------------------------------------------------------
    // Ponti
    // ---------------------------------------------------------------------

    private void onSceneClick(
            MouseEvent event
    ) {
        if (dilemmaOpen) {
            return;
        }

        int index =
                beacons.at(
                        event.getX(),
                        event.getY()
                );

        if (index < 0) {
            return;
        }

        if (state
                == LevelState.BRIDGE_SELECTION) {

            selectBridge(
                    index
            );

        } else if (state
                == LevelState.CHANGING_BRIDGE
                && index
                == destinationBridge) {

            performJump();
        }
    }

    private void onSceneMove(
            MouseEvent event
    ) {
        if (state
                != LevelState.BRIDGE_SELECTION
                || dilemmaOpen) {

            return;
        }

        int index =
                beacons.at(
                        event.getX(),
                        event.getY()
                );

        beacons.hover(
                index
        );

        if (index >= 0) {
            hud.setHover(
                    "Scegli questo ponte."
            );
        } else {
            hud.setHover(
                    "Scegli uno dei tre ponti."
            );
        }
    }

    // ---------------------------------------------------------------------
    // Vittoria
    // ---------------------------------------------------------------------

    private void win() {
        if (state
                == LevelState.VICTORY) {

            return;
        }

        state =
                LevelState.VICTORY;

        hud.showDpad(
                false
        );

        hud.hideArrow();

        beacons.hide();

        stopTimers();

        Sound.stopAll();

        Sound.play(
                "/audio/gate-open.mp3",
                0.7
        );

        AltezzeRecord.save(
                best
        );

        updateHeader();

        showVictoryResult();
    }

    private void showVictoryResult() {
        StringBuilder message =
                new StringBuilder();

        message
                .append(
                        crossing.victory()
                )
                .append("\n\n")
                .append("Punteggio: ")
                .append(
                        score
                );

        if (cleanRun) {
            message.append(
                    "\nAttraversata senza errori."
            );
        }

        showOverlay(
                "L'altra sponda",
                message.toString(),
                "Prosegui",
                this::finish
        );
    }

    // ---------------------------------------------------------------------
    // Caduta
    // ---------------------------------------------------------------------

    private void fall() {
        if (state
                == LevelState.GAME_OVER
                || state
                == LevelState.VICTORY) {

            return;
        }

        state =
                LevelState.GAME_OVER;

        dilemmaOpen = false;

        hud.showDpad(
                false
        );

        hud.hideArrow();
        hud.hideReroute();

        beacons.hide();

        stopTimers();

        Sound.stopAll();

        Sound.play(
                "/audio/wood-break.mp3",
                0.85
        );

        Sound.play(
                new SoundCue(
                        "/audio/scream.mp3",
                        0.85,
                        0.25
                )
        );

        Sound.play(
                new SoundCue(
                        "/audio/fall.mp3",
                        0.9,
                        0.75
                )
        );

        controller.registerFall();

        AltezzeRecord.save(
                best
        );

        boolean canRetry =
                controller.canRetry();

        String note =
                canRetry
                        ? "\n\n" + attemptsNote()
                        : "\n\nTentativi esauriti: la prova ricomincia da capo.";

        updateHeader();

        Rectangle black =
                SceneFx.veil(
                        root,
                        1.0
                );

        black.setOpacity(
                0
        );

        root.getChildren().add(
                black
        );

        if (headerView != null) {
            headerView.toFront();
        }

        FadeTransition drop =
                new FadeTransition(
                        Duration.millis(
                                700
                        ),
                        black
                );

        drop.setFromValue(
                0
        );

        drop.setToValue(
                1
        );

        String message =
                crossing.gameOver()
                        + note;

        drop.setOnFinished(
                event -> {
                    root
                            .getChildren()
                            .remove(
                                    black
                            );

                    showOverlay(
                            canRetry
                                    ? "Sei caduto"
                                    : "Prova fallita",
                            message,
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
        int left =
                controller.remainingAttempts();

        return left == 1
                ? "Ti resta un tentativo."
                : "Ti restano " + left + " tentativi.";
    }

    private void restartLevel() {
        stopTimers();
        hideOverlay();

        progress = 0;

        balance =
                config.balancePoints();

        currentBridge = -1;
        destinationBridge = -1;

        nextThreshold = 0;
        routeStep = 0;

        currentArrow = null;
        lastArrow = null;

        sameDirCount = 0;
        streak = 0;
        score = 0;

        cleanRun = true;
        dilemmaOpen = false;

        hud.hideArrow();
        hud.hideReroute();

        hud.setStats(
                score,
                best,
                streak
        );

        hud.setBalance(
                balance
        );

        hud.setChannel(
                null
        );

        bg.setScaleX(
                1
        );

        bg.setScaleY(
                1
        );

        startAmbience();

        /*
         * nextDilemmaIndex non viene azzerato:
         * le domande già risposte non vengono conteggiate di nuovo.
         */
        toSelection();
    }

    private void finish() {
        cleanup();

        onFinished.accept(
                SceneOutcome.AVANTI
        );
    }

    private void onTimeUp() {
        if (state
                == LevelState.VICTORY
                || state
                == LevelState.GAME_OVER) {

            return;
        }

        fall();
    }

    // ---------------------------------------------------------------------
    // Tastiera
    // ---------------------------------------------------------------------

    private void onKeyPressed(
            KeyEvent event
    ) {
        if (state
                != LevelState.PLAYING
                || dilemmaOpen) {

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
            handleArrowInput(
                    direction
            );

            event.consume();
        }
    }

    // ---------------------------------------------------------------------
    // Effetti
    // ---------------------------------------------------------------------

    private void shake() {
        Timeline shake =
                new Timeline();

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
                                    Duration.millis(
                                            i * 45
                                    ),
                                    new KeyValue(
                                            stage.translateXProperty(),
                                            dx
                                    )
                            )
                    );
        }

        shake
                .getKeyFrames()
                .add(
                        new KeyFrame(
                                Duration.millis(
                                        7 * 45
                                ),
                                new KeyValue(
                                        stage.translateXProperty(),
                                        0
                                )
                        )
                );

        shake.play();
    }

    private void updateHeader() {
        header.setTentativi(
                controller.remainingAttempts(),
                controller.maxAttempts()
        );
    }

    // ---------------------------------------------------------------------
    // Overlay
    // ---------------------------------------------------------------------

    private void showOverlay(
            String title,
            String message,
            String buttonText,
            Runnable action
    ) {
        Label heading =
                new Label(
                        title
                );

        heading
                .getStyleClass()
                .add(
                        "fear-title"
                );

        Button button =
                new Button(
                        buttonText
                );

        button
                .getStyleClass()
                .add(
                        "menu-button"
                );

        button.setOnAction(
                event -> action.run()
        );

        VBox card =
                new VBox(
                        22,
                        heading,
                        SceneFx.paragraph(
                                message
                        ),
                        button
                );

        card.setAlignment(
                Pos.CENTER
        );

        card.setMaxWidth(
                760
        );

        card
                .getStyleClass()
                .add(
                        "fear-panel"
                );

        overlay =
                new StackPane(
                        SceneFx.veil(
                                root,
                                0.82
                        ),
                        card
                );

        StackPane.setAlignment(
                card,
                Pos.CENTER
        );

        root.getChildren().add(
                overlay
        );

        if (headerView != null) {
            headerView.toFront();
        }
    }

    private void hideOverlay() {
        if (overlay == null) {
            return;
        }

        root
                .getChildren()
                .remove(
                        overlay
                );

        overlay = null;
    }

    // ---------------------------------------------------------------------
    // Timer e cleanup
    // ---------------------------------------------------------------------

    private void stopTimers() {
        clock.stop();

        cancel(
                arrowDelay
        );

        cancel(
                responseTimer
        );

        cancel(
                rerouteDeadline
        );
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

        try {
            onSave.run();

        } finally {
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
        }
    }

    private void pauseIfRunning(
            PauseTransition transition
    ) {
        if (transition != null
                && transition.getStatus()
                == Animation.Status.RUNNING) {

            transition.pause();
        }
    }

    private void resumeIfPaused(
            PauseTransition transition
    ) {
        if (transition != null
                && transition.getStatus()
                == Animation.Status.PAUSED) {

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

        if (boundScene != null) {
            boundScene.removeEventFilter(
                    KeyEvent.KEY_PRESSED,
                    keyHandler
            );

            boundScene = null;
        }

        Sound.stopAll();
    }

    private void cancel(
            PauseTransition transition
    ) {
        if (transition != null) {
            transition.stop();
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

