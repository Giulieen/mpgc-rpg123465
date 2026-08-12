package it.unicam.cs.mpgc.rpg123465.ui.support;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;

import java.util.Random;

/**
 * Leggero movimento ambientale applicato a un nodo JavaFX.
 *
 * L'animazione viene fermata esplicitamente tramite {@link #stop()}
 * per evitare timer attivi dopo l'uscita dalla scena.
 */
public final class ScreenShake {

    private static final double MAX_OFFSET = 1.8;
    private static final long UPDATE_INTERVAL =
            90_000_000L;

    private final Node target;
    private final Random random =
            new Random();

    private AnimationTimer timer;
    private long lastUpdate;

    public ScreenShake(
            Node target
    ) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Il nodo da animare non può essere null."
            );
        }

        this.target = target;
    }

    /**
     * Avvia un leggero movimento ambientale.
     */
    public void startAmbient() {
        stop();

        lastUpdate = 0;

        timer =
                new AnimationTimer() {
                    @Override
                    public void handle(
                            long now
                    ) {
                        if (lastUpdate != 0
                                && now - lastUpdate
                                < UPDATE_INTERVAL) {

                            return;
                        }

                        lastUpdate = now;

                        target.setTranslateX(
                                randomOffset()
                        );

                        target.setTranslateY(
                                randomOffset()
                        );
                    }
                };

        timer.start();
    }

    /**
     * Ferma l'animazione e riporta il nodo nella posizione originale.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        target.setTranslateX(0);
        target.setTranslateY(0);

        lastUpdate = 0;
    }

    private double randomOffset() {
        return (random.nextDouble() * 2 - 1)
                * MAX_OFFSET;
    }
}