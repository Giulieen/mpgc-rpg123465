package it.unicam.cs.mpgc.rpg123465.view;

import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Incatena le scene di un piano mostrandole una dopo l'altra.
 */
public class SceneFlow {

    private final List<FloorScene> scenes;
    private final Consumer<Parent> display;
    private final Runnable onFloorCompleted;

    private int current;

    /**
     * @param scenes scene del piano
     * @param display operazione che mostra una vista
     * @param onFloorCompleted operazione eseguita alla fine del piano
     */
    public SceneFlow(List<FloorScene> scenes, Consumer<Parent> display, Runnable onFloorCompleted) {
        if (scenes == null || scenes.isEmpty()) {
            throw new IllegalArgumentException("Un piano deve avere almeno una scena.");
        }

        if (display == null || onFloorCompleted == null) {

            throw new IllegalArgumentException("I callback non possono essere null.");
        }

        this.scenes = new ArrayList<>(scenes);

        this.display = display;
        this.onFloorCompleted = onFloorCompleted;
        this.current = 0;
    }

    /**
     * Avvia il piano.
     */
    public void start() {
        showCurrent();
    }

    private void showCurrent() {
        FloorScene scene = scenes.get(current);

        display.accept(scene.createView(this::onSceneFinished));
    }

    private void onSceneFinished(SceneOutcome outcome) {
        if (outcome == SceneOutcome.REPEAT) {
            showCurrent();
            return;
        }

        current++;

        if (current >= scenes.size()) {
            onFloorCompleted.run();
        } else {
            showCurrent();
        }
    }
}
