package it.unicam.cs.mpgc.rpg123465.floors.encounter;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;
import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;
import it.unicam.cs.mpgc.rpg123465.persistence.TrialRecord;
import it.unicam.cs.mpgc.rpg123465.ui.HeaderBar;
import it.unicam.cs.mpgc.rpg123465.ui.SceneOutcome;
import it.unicam.cs.mpgc.rpg123465.ui.support.ResultOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.SceneFx;
import it.unicam.cs.mpgc.rpg123465.ui.support.TrialStats;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * La prova del Piano I: la stanza dei Topi.
 *
 * <p>
 * Si occupa soltanto di mostrare la stanza, leggere la tastiera e scandire il
 * tempo. Le regole — chi si muove dove, cosa viene catturato, quando la prova
 * è finita — stanno in {@link RatMazeController}, che non sa nulla di grafica.
 */
public final class RatMazeScene implements FloorScene {

    /** Lato di una cella: tre volte la casella da 16 pixel del tileset. */
    private static final double CELL = 48;

    /** Ogni quanto i topi avanzano di una cella. */
    private static final Duration RAT_STEP = Duration.millis(600);

    /** Ogni quanto si prova a far comparire un topo. */
    private static final Duration SPAWN_INTERVAL = Duration.millis(1400);

    /** Ogni quanto cambia il fotogramma di topi e portatore di luce. */
    private static final Duration FRAME_STEP = Duration.millis(150);

    /** Per quanto il portatore di luce resta in posa di camminata dopo un passo. */
    private static final long WALK_HOLD_MS = 320;

    /**
     * Tinta della pietra.
     *
     * Il tileset esiste solo in varianti fredde — azzurro e viola — che accanto
     * al resto della Torre stonano. Le caselle vengono quindi prima portate in
     * scala di grigi e poi moltiplicate per questo oro spento: i muri diventano
     * quasi neri, i corridoi pietra dorata, e il piano entra nella stessa
     * gamma dei bordi e delle icone del gioco.
     */
    private static final Color STONE_TINT = Color.web("#b99a5e");

    /** Identifica il record del piano nell'archivio condiviso. */
    private static final String RECORD_KEY = "topi.tempo";

    /** Ogni quanto avanza il cronometro della prova. */
    private static final Duration CLOCK_STEP = Duration.seconds(1);

    private static final Color STONE = Color.web("#2a2620");
    private static final Color FLOOR = Color.web("#0f0d0a");
    private static final Color GOLD = Color.web("#f4c76b");
    private static final Color EXIT_GLOW = Color.web("#e8b451");
    private static final Color RAT_COLOR = Color.web("#9d9484");

    private final FearEncounter encounter;
    private final RatMaze maze = new RatMaze();
    private final RatMazeController controller;
    private final MazeSprites sprites = MazeSprites.load();
    private final HeaderBar header;
    private final TrialRecord record;
    private final Consumer<Runnable> onSave;
    private final Runnable onExit;
    private final Random rng = new Random();

    private final StackPane root = new StackPane();

    /*
     * Tre strati sovrapposti. La pietra viene tinta in blocco, quindi deve
     * stare da sola: tane, uscite, topi e portatore di luce hanno colori loro
     * e passano sopra senza essere toccati dalla tinta.
     */
    private final Pane stone = new Pane();
    private final Pane marks = new Pane();
    private final Pane actors = new Pane();
    private final StackPane board = new StackPane();

    private final Label counter = new Label();

    private final Map<Rat, RatView> ratViews = new HashMap<>();

    private final javafx.event.EventHandler<KeyEvent> keyHandler =
            this::onKeyPressed;

    private Consumer<SceneOutcome> onFinished;
    private Region headerView;
    private Scene boundScene;

    private Node playerNode;
    private ImageView playerSprite;
    private MazeSprites.Facing playerFacing = MazeSprites.Facing.FRONTE;
    private boolean playerFlipped;
    private long lastStepAt;

    private int frameTick;

    private Timeline ratTimeline;
    private Timeline spawnTimeline;
    private Timeline frameTimeline;
    private Timeline clockTimeline;

    /*
     * Quanto dura la prova. Il cronometro e' una Timeline come le altre, cosi'
     * si ferma da se' quando la prova viene sospesa per salvare e quando
     * l'esito e' deciso: il tempo misurato e' solo quello giocato.
     */
    private int elapsedSeconds;
    private final ResultOverlay overlay =
            new ResultOverlay(root, () -> headerView);

    /**
     * Ciò che serve a disegnare un topo: la sua vista, la variante di colore
     * sorteggiata alla nascita e il verso in cui è rivolto.
     *
     * Nessuna di queste cose risale al controller: per le regole i topi sono
     * tutti uguali.
     */
    private static final class RatView {

        private final Node node;
        private final ImageView sprite;
        private GridPosition lastPosition;
        private boolean flipped;

        private RatView(Node node, ImageView sprite, GridPosition start) {
            this.node = node;
            this.sprite = sprite;
            this.lastPosition = start;
        }
    }

    public RatMazeScene(
            FearEncounter encounter,
            GameController game,
            RecordStore records,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (encounter == null || game == null || records == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.encounter = encounter;
        this.controller = new RatMazeController(maze, game);

        /* Sui Topi si misura quanto ci si mette: vince il tempo piu' basso. */
        this.record = TrialRecord.lowerIsBetter(records, RECORD_KEY);

        this.onSave = onSave;
        this.onExit = onExit;

        this.header = new HeaderBar(
                game.getPlayerName(),
                onSave == null ? null : this::saveWithTrialSuspended,
                onExit == null ? null : this::exitLevel
        );
    }

    // ---------------------------------------------------------------------
    // Costruzione
    // ---------------------------------------------------------------------

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

        root.getStyleClass().add("fear-root");

        headerView = header.createView();
        header.setProva(encounter.title());
        updateHeader();

        buildBoard();
        updateCounter();

        root.getChildren().setAll(
                SceneFx.cover(root, encounter.backgroundResource()),
                SceneFx.veil(root, 0.78),
                centerPanel(),
                headerView
        );

        StackPane.setAlignment(headerView, Pos.TOP_CENTER);

        root.sceneProperty().addListener(
                (obs, oldScene, newScene) -> bindKeys(newScene)
        );

        startAmbience();
        startTimelines();

        return root;
    }

    private VBox centerPanel() {
        counter.getStyleClass().add("maze-counter");

        Label hint = new Label(
                "Muoviti con le frecce e non far arrivare i topi alle uscite."
        );
        hint.getStyleClass().add("fear-memory");

        StackPane frame = new StackPane(board);
        frame.getStyleClass().add("maze-frame");
        frame.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox panel = new VBox(14, counter, frame, hint);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        panel.setPadding(new Insets(18, 22, 16, 22));
        panel.getStyleClass().add("maze-panel");

        StackPane.setAlignment(panel, Pos.CENTER);
        StackPane.setMargin(panel, new Insets(64, 0, 0, 0));

        return panel;
    }

    /**
     * Disegna la stanza una volta sola: muri, corridoi, tane e uscite non
     * cambiano mai. Solo il portatore di luce e i topi vengono spostati.
     */
    private void buildBoard() {
        double width = maze.columns() * CELL;
        double height = maze.rows() * CELL;

        for (Pane layer : new Pane[] {stone, marks, actors}) {
            layer.setPrefSize(width, height);
            layer.setMinSize(width, height);
            layer.setMaxSize(width, height);
            layer.setMouseTransparent(true);
        }

        for (int row = 0; row < maze.rows(); row++) {
            for (int column = 0; column < maze.columns(); column++) {
                GridPosition position = new GridPosition(row, column);

                place(stone, pavementNode(position), position);

                if (maze.isExit(position)) {
                    place(marks, exitMark(), position);

                } else if (maze.isDen(position)) {
                    place(marks, denMark(), position);
                }
            }
        }

        playerNode = buildPlayerNode();
        actors.getChildren().add(playerNode);

        board.getChildren().setAll(tintedStone(width, height), marks, actors);
        board.setMaxSize(width, height);

        movePlayerNode();
    }

    /**
     * Porta la pietra in scala di grigi e la moltiplica per la tinta calda.
     *
     * L'ordine conta: prima si toglie il colore freddo dell'originale, poi si
     * aggiunge il proprio. Facendo il contrario la desaturazione cancellerebbe
     * anche la tinta appena data.
     */
    private Pane tintedStone(double width, double height) {
        stone.setEffect(new ColorAdjust(0, -1, 0, 0));

        Rectangle tint = new Rectangle(width, height, STONE_TINT);
        tint.setBlendMode(BlendMode.MULTIPLY);

        Pane tinted = new Pane(stone, tint);
        tinted.setPrefSize(width, height);
        tinted.setMinSize(width, height);
        tinted.setMaxSize(width, height);
        tinted.setMouseTransparent(true);

        return tinted;
    }

    private void place(Pane layer, Node node, GridPosition position) {
        node.setLayoutX(position.column() * CELL);
        node.setLayoutY(position.row() * CELL);
        layer.getChildren().add(node);
    }

    /** La sola pietra: muro o corridoio, senza i segni che ci stanno sopra. */
    private Node pavementNode(GridPosition position) {
        return maze.isWall(position)
                ? wallNode()
                : floorNode();
    }

    private Node wallNode() {
        if (sprites.hasTiles()) {
            return sprites.wall(CELL);
        }

        Rectangle tile = new Rectangle(CELL, CELL, STONE);
        tile.setStroke(Color.rgb(0, 0, 0, 0.55));
        return tile;
    }

    private Node floorNode() {
        if (sprites.hasTiles()) {
            return sprites.floor(CELL);
        }

        return new Rectangle(CELL, CELL, FLOOR);
    }

    /**
     * La tana: un'imboccatura nera scavata nella pietra.
     *
     * Ha la stessa forma ad arco dell'uscita ma il colore opposto, così le due
     * si leggono come una coppia: dal nero i topi arrivano, verso l'oro se ne
     * vanno. Il tileset non ha una casella adatta — quella più vicina è troppo
     * chiara e legge come una piastrella — quindi si compone qui.
     */
    private Node denMark() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL, CELL);

        Rectangle mouth = new Rectangle(CELL * 0.52, CELL * 0.6,
                Color.web("#05040a"));
        mouth.setArcWidth(CELL * 0.5);
        mouth.setArcHeight(CELL * 0.5);
        mouth.setTranslateY(CELL * 0.08);
        mouth.setEffect(new InnerShadow(9, Color.rgb(120, 110, 140, 0.5)));

        // Il bordo di pietra sbrecciata attorno al foro.
        Rectangle rim = new Rectangle(CELL * 0.62, CELL * 0.68,
                Color.rgb(0, 0, 0, 0.55));
        rim.setArcWidth(CELL * 0.6);
        rim.setArcHeight(CELL * 0.6);
        rim.setTranslateY(CELL * 0.07);

        cell.getChildren().addAll(rim, mouth);

        return cell;
    }

    /**
     * L'uscita: un varco illuminato d'oro nella parete.
     *
     * Il colore la distingue dalle tane, che sono buchi neri: da una parte i
     * topi entrano nella stanza, dall'altra la lasciano — e solo il secondo
     * caso costa qualcosa al giocatore.
     */
    private Node exitMark() {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL, CELL);

        Rectangle arch = new Rectangle(CELL * 0.52, CELL * 0.64, EXIT_GLOW);
        arch.setArcWidth(CELL * 0.5);
        arch.setArcHeight(CELL * 0.5);
        arch.setOpacity(0.88);
        arch.setEffect(new DropShadow(20, EXIT_GLOW));

        Rectangle depth = new Rectangle(CELL * 0.3, CELL * 0.42,
                Color.rgb(20, 12, 4, 0.75));
        depth.setArcWidth(CELL * 0.3);
        depth.setArcHeight(CELL * 0.3);
        depth.setTranslateY(CELL * 0.06);

        cell.getChildren().addAll(arch, depth);

        // Un respiro lento, perché l'occhio le trovi senza doverle cercare.
        FadeTransition pulse =
                new FadeTransition(Duration.seconds(1.6), arch);
        pulse.setFromValue(0.9);
        pulse.setToValue(0.45);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        return cell;
    }

    private Node buildPlayerNode() {
        if (sprites.hasPlayer()) {
            playerSprite = sprites.player(CELL);
            playerSprite.setEffect(SceneFx.glow(20, 0.45));
            return playerSprite;
        }

        Circle circle = new Circle(CELL * 0.26, GOLD);
        circle.setEffect(SceneFx.glow(18, 0.55));

        StackPane cell = new StackPane(circle);
        cell.setPrefSize(CELL, CELL);
        return cell;
    }

    // ---------------------------------------------------------------------
    // Tempo
    // ---------------------------------------------------------------------

    private void startTimelines() {
        ratTimeline = loop(RAT_STEP, event -> onRatStep());
        spawnTimeline = loop(SPAWN_INTERVAL, event -> onSpawnTick());
        frameTimeline = loop(FRAME_STEP, event -> onFrameTick());
        clockTimeline = loop(CLOCK_STEP, event -> onClockTick());

        // Il primo topo non deve farsi attendere due secondi.
        onSpawnTick();
    }

    private Timeline loop(
            Duration every,
            javafx.event.EventHandler<javafx.event.ActionEvent> action
    ) {
        Timeline timeline = new Timeline(new KeyFrame(every, action));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        return timeline;
    }

    /**
     * Fa scorrere i fotogrammi di tutti gli sprite.
     *
     * È una sola battuta condivisa: i topi non hanno un'animazione ciascuno,
     * così fermarli tutti costa una riga sola in {@link #stopTrial()}.
     */
    private void onFrameTick() {
        frameTick++;

        if (sprites.hasRats()) {
            for (RatView view : ratViews.values()) {
                if (view.sprite != null) {
                    view.sprite.setViewport(
                            sprites.ratViewport(frameTick)
                    );
                }
            }
        }

        updatePlayerSprite();
    }

    private void onSpawnTick() {
        Rat rat = controller.spawnRat();

        if (rat == null) {
            return;
        }

        ratViews.put(rat, buildRatView(rat));
        moveRatNode(rat);

        Sound.play("/audio/scurrying.mp3", 0.28);
    }

    private RatView buildRatView(Rat rat) {
        if (sprites.hasRats()) {
            ImageView sprite =
                    sprites.rat(rng.nextInt(sprites.ratVariants()), CELL);

            actors.getChildren().add(sprite);

            return new RatView(sprite, sprite, rat.position());
        }

        FontIcon icon = new FontIcon("mdi2r-rodent");
        icon.setIconSize((int) (CELL * 0.6));
        icon.setIconColor(RAT_COLOR);

        actors.getChildren().add(icon);

        return new RatView(icon, null, rat.position());
    }

    private void onRatStep() {
        RatMazeController.TickOutcome outcome = controller.advanceRats();

        syncRatNodes();

        if (outcome.escaped() > 0) {
            updateHeader();
            Sound.play("/audio/squeak.mp3", 0.55);
            flashEscape();
        }

        if (outcome.captured() > 0) {
            onCaptured(outcome.captured());
        }

        checkOutcome();
    }

    // ---------------------------------------------------------------------
    // Input
    // ---------------------------------------------------------------------

    private void bindKeys(Scene scene) {
        if (boundScene != null) {
            boundScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        }

        boundScene = scene;

        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        }
    }

    private void onKeyPressed(KeyEvent event) {
        if (controller.state() != RatMazeController.MazeState.PLAYING) {
            return;
        }

        Direction direction = switch (event.getCode()) {
            case UP, W -> Direction.SU;
            case DOWN, S -> Direction.GIU;
            case LEFT, A -> Direction.SINISTRA;
            case RIGHT, D -> Direction.DESTRA;
            default -> null;
        };

        if (direction == null) {
            return;
        }

        int captured = controller.movePlayer(direction);

        faceTowards(direction);
        lastStepAt = System.currentTimeMillis();

        movePlayerNode();
        updatePlayerSprite();
        syncRatNodes();

        if (captured > 0) {
            onCaptured(captured);
        }

        checkOutcome();

        event.consume();
    }

    private void faceTowards(Direction direction) {
        switch (direction) {
            case SU -> playerFacing = MazeSprites.Facing.SPALLE;
            case GIU -> playerFacing = MazeSprites.Facing.FRONTE;
            case SINISTRA -> {
                playerFacing = MazeSprites.Facing.PROFILO;
                playerFlipped = true;
            }
            case DESTRA -> {
                playerFacing = MazeSprites.Facing.PROFILO;
                playerFlipped = false;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Aggiornamento della vista
    // ---------------------------------------------------------------------

    private void movePlayerNode() {
        GridPosition position = controller.playerPosition();

        playerNode.setLayoutX(position.column() * CELL);
        playerNode.setLayoutY(position.row() * CELL);
    }

    private void updatePlayerSprite() {
        if (playerSprite == null) {
            return;
        }

        boolean walking =
                System.currentTimeMillis() - lastStepAt < WALK_HOLD_MS;

        playerSprite.setViewport(
                sprites.playerViewport(playerFacing, frameTick, walking)
        );

        playerSprite.setScaleX(playerFlipped ? -1 : 1);
    }

    private void moveRatNode(Rat rat) {
        RatView view = ratViews.get(rat);

        if (view == null) {
            return;
        }

        GridPosition position = rat.position();

        view.node.setLayoutX(position.column() * CELL);
        view.node.setLayoutY(position.row() * CELL);

        /*
         * Il foglio disegna il topo rivolto a destra: si specchia solo quando
         * cambia colonna, così muovendosi in verticale mantiene l'ultimo verso
         * invece di girarsi su se stesso.
         */
        if (position.column() != view.lastPosition.column()) {
            view.flipped = position.column() < view.lastPosition.column();
        }

        view.node.setScaleX(view.flipped ? -1 : 1);
        view.lastPosition = position;
    }

    /**
     * Allinea i nodi ai topi ancora vivi e toglie quelli spariti, sia che
     * siano stati catturati sia che siano fuggiti.
     */
    private void syncRatNodes() {
        var alive = controller.rats();

        ratViews.entrySet().removeIf(entry -> {
            if (alive.contains(entry.getKey())) {
                return false;
            }

            actors.getChildren().remove(entry.getValue().node);
            return true;
        });

        for (Rat rat : alive) {
            moveRatNode(rat);
        }
    }

    private void onCaptured(int howMany) {
        updateCounter();
        flashCapture();

        for (int i = 0; i < howMany; i++) {
            Sound.play("/audio/mousetrap-snap.mp3", 0.5);
        }
    }

    /** Un lampo brevissimo sul portatore di luce: la cattura si vede subito. */
    private void flashCapture() {
        ScaleTransition pop =
                new ScaleTransition(Duration.millis(110), playerNode);

        pop.setFromX(1);
        pop.setFromY(1);
        pop.setToX(1.25);
        pop.setToY(1.25);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);
        pop.play();
    }

    private void updateCounter() {
        counter.setText(
                "Topi catturati   "
                        + controller.capturedRats()
                        + " / "
                        + controller.targetRats()
                        + "     ·     Tempo   "
                        + TrialStats.time(elapsedSeconds)
        );
    }

    private void onClockTick() {
        elapsedSeconds++;
        updateCounter();
    }

    private void updateHeader() {
        header.setTentativi(
                controller.remainingAttempts(),
                controller.maxAttempts()
        );
    }

    /** Un avviso breve, che non interrompe il gioco. */
    private void flashEscape() {
        Label message = new Label("Un topo è fuggito.");
        message.getStyleClass().add("maze-alert");
        message.setMouseTransparent(true);

        StackPane.setAlignment(message, Pos.TOP_CENTER);
        StackPane.setMargin(message, new Insets(110, 0, 0, 0));

        root.getChildren().add(message);
        headerView.toFront();

        FadeTransition fade =
                new FadeTransition(Duration.millis(900), message);

        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setDelay(Duration.millis(500));
        fade.setOnFinished(event -> root.getChildren().remove(message));
        fade.play();
    }

    // ---------------------------------------------------------------------
    // Esiti
    // ---------------------------------------------------------------------

    private void checkOutcome() {
        switch (controller.state()) {
            case COMPLETED -> {
                stopTrial();
                Sound.stopAll();
                Sound.play("/audio/gate-open.mp3", 0.7);

                overlay.show(
                        "PROVA SUPERATA",
                        "L'ultimo topo scompare tra le ombre.\n"
                                + "Le uscite sono di nuovo al sicuro.",
                        trialStats(record.submit(elapsedSeconds)),
                        "Continua",
                        this::finish
                );
            }

            case FAILED -> {
                stopTrial();
                syncRatNodes();
                updateHeader();

                Sound.stopAll();
                Sound.play("/audio/rats-many.mp3", 0.6);

                /*
                 * Il tempo di una prova fallita non entra nel record: si
                 * confrontano solo le stanze ripulite fino all'ultimo topo.
                 */
                overlay.show(
                        "PROVA FALLITA",
                        "Troppi topi sono sfuggiti.",
                        trialStats(record.best().orElse(0)),
                        "Riprova",
                        this::restartTrial
                );
            }

            default -> { }
        }
    }

    /**
     * Riepilogo della prova: quanti topi sono stati presi, quanto ci si e'
     * messi e il miglior tempo mai registrato su questa installazione.
     *
     * @param best record da mostrare, zero se la prova non e' mai stata
     *             superata
     */
    private Label trialStats(int best) {
        String bestLabel =
                best > 0
                        ? TrialStats.time(best)
                        : "—";

        return TrialStats.line(
                "Topi presi: "
                        + controller.capturedRats()
                        + " / "
                        + controller.targetRats(),
                "Tempo: " + TrialStats.time(elapsedSeconds),
                "Record: " + bestLabel
        );
    }

    /**
     * Riporta la prova all'inizio.
     *
     * Il dilemma non viene riproposto: era una fase precedente e la risposta
     * resta quella data allora.
     */
    private void restartTrial() {
        overlay.hide();

        controller.reset();

        elapsedSeconds = 0;

        syncRatNodes();
        movePlayerNode();
        updateCounter();
        updateHeader();

        startAmbience();
        startTimelines();
    }

    private void finish() {
        cleanup();
        onFinished.accept(SceneOutcome.AVANTI);
    }

    // ---------------------------------------------------------------------
    // Audio, salvataggio, uscita
    // ---------------------------------------------------------------------

    private void startAmbience() {
        Sound.stopAll();
        Sound.loop("/audio/ambience-topi.mp3", 0.32);
        Sound.loop("/audio/fire-crackle.mp3", 0.20);
    }

    /**
     * Salva tenendo ferma la prova.
     *
     * La conferma è una modale, e una modale JavaFX gira in un event loop
     * annidato: senza sospendere, mentre il giocatore legge il messaggio i
     * topi continuerebbero ad avanzare verso le uscite.
     */
    private void saveWithTrialSuspended() {
        boolean running =
                controller.state() == RatMazeController.MazeState.PLAYING;

        if (running) {
            pauseTimelines();
        }

        onSave.accept(() -> {
            if (running) {
                resumeTimelines();
            }
        });
    }

    private void pauseTimelines() {
        forEachTimeline(Timeline::pause);
    }

    private void resumeTimelines() {
        forEachTimeline(Timeline::play);
    }

    private void forEachTimeline(Consumer<Timeline> action) {
        for (Timeline timeline :
                new Timeline[] {
                        ratTimeline,
                        spawnTimeline,
                        frameTimeline,
                        clockTimeline}) {

            if (timeline != null) {
                action.accept(timeline);
            }
        }
    }

    /** Ferma spawn, movimento e animazioni, lasciando la stanza a schermo. */
    private void stopTrial() {
        forEachTimeline(Timeline::stop);

        ratTimeline = null;
        spawnTimeline = null;
        frameTimeline = null;
        clockTimeline = null;
    }

    private void exitLevel() {
        cleanup();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void cleanup() {
        stopTrial();

        if (boundScene != null) {
            boundScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            boundScene = null;
        }

        Sound.stopAll();
    }
}
