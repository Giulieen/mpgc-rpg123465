package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.controller.GameController;
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

    private final DarkRoom room;
    private final DarkRoomController controller;
    private final HeaderBar header;
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

    public DarkRoomScene(
            DarkRoom room,
            GameController game
    ) {
        this(
                room,
                game,
                null,
                null
        );
    }

    public DarkRoomScene(
            DarkRoom room,
            GameController game,
            Runnable onSave
    ) {
        this(
                room,
                game,
                onSave,
                null
        );
    }

    public DarkRoomScene(
            DarkRoom room,
            GameController game,
            Runnable onSave,
            Runnable onExit
    ) {
        if (room == null || game == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.room = room;

        this.controller =
                new DarkRoomController(
                        room,
                        game
                );

        this.onExit = onExit;

        this.header =
                new HeaderBar(
                        game.getPlayerName(),
                        onSave,
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
            controller.enter();
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

        clock.stop();

        if (closeup.isOpen()) {
            closeup.close();
        }

        Sound.stopAll();

        Sound.play(
                "/audio/gate-open.mp3",
                0.7
        );

        controller.registerSuccess();

        updateHeader();
        showSuccessResult();
    }

    private void showSuccessResult() {
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

        Label effects =
                new Label(
                        formatEffects(
                                controller.successLucidityDelta(),
                                controller.successStressDelta()
                        )
                );

        effects.getStyleClass().add(
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
                        effects,
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

        controller.registerWrong();
        updateHeader();

        showInterlude(
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

        controller.registerTimeout();
        updateHeader();

        showInterlude(
                room.timeoutText()
        );
    }

    private void showInterlude(
            String message
    ) {
        String note = null;

        if (controller.isDefeated()) {
            controller.restoreCheckpoint();
            updateHeader();

            note =
                    "La Torre ti ha sopraffatto. "
                            + "Ti risvegli sulla soglia "
                            + "e provi ancora.";
        }

        SceneFx.interlude(
                root,
                message,
                note,
                note != null
                        ? 3.4
                        : 2.8,
                this::startAttempt
        );

        headerView.toFront();
    }

    private String formatEffects(
            int lucidityDelta,
            int stressDelta
    ) {
        return "Lucidità "
                + signed(
                        lucidityDelta
                )
                + "     ·     Stress "
                + signed(
                        stressDelta
                );
    }

    private String signed(
            int value
    ) {
        return value > 0
                ? "+" + value
                : Integer.toString(
                        value
                );
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
        header.setVita(
                controller.playerCurrentHealth(),
                controller.playerMaxHealth()
        );

        header.setLucidita(
                controller.lucidita(),
                controller.mindMax()
        );

        header.setStress(
                controller.stress(),
                controller.mindMax()
        );
    }

    private void exitLevel() {
        cleanup();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void cleanup() {
        clock.stop();

        if (closeup.isOpen()) {
            closeup.close();
        }

        Sound.stopAll();
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

