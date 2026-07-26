package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Incatena le scene di un piano, mostrandole una dopo l'altra.
 * <p>
 * Sa soltanto due cose: come far vedere una scena e cosa fare quando il piano
 * è superato. Non conosce il contenuto delle scene né la logica di gioco, così
 * lo stesso meccanismo regge un piano di sole domande e uno tutto d'azione.
 */
public class SceneFlow {

    private final List<FloorScene> scenes;
    private final Consumer<Parent> display;
    private final Runnable onFloorCompleted;

    private int current;

    /**
     * Crea la sequenza di un piano.
     *
     * @param scenes scene da attraversare, in ordine
     * @param display come mostrare la vista di una scena
     * @param onFloorCompleted azione da eseguire quando l'ultima scena è
     *                         superata
     * @throws IllegalArgumentException se le scene mancano o un parametro è null
     */
    public SceneFlow(List<FloorScene> scenes,
                     Consumer<Parent> display,
                     Runnable onFloorCompleted) {
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
     * Mostra la prima scena e avvia il piano.
     */
    public void start() {
        showCurrent();
    }

    private void showCurrent() {
        display.accept(scenes.get(current).createView(this::onSceneFinished));
    }

    private void onSceneFinished(SceneOutcome outcome) {
        if (outcome == SceneOutcome.RIPETI) {
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
