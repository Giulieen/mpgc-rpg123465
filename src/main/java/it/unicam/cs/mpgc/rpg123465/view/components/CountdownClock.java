package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.animation.AnimationTimer;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.function.IntConsumer;

/**
 * Conto alla rovescia riutilizzabile.
 *
 * Supporta pausa e ripresa senza perdere il tempo rimanente.
 * Questo permette di sospendere il timer quando compare un dilemma.
 */
public final class CountdownClock {

    private static final PseudoClass URGENT = PseudoClass.getPseudoClass("urgent");

    private AnimationTimer timer;
    private MediaPlayer heartbeat;

    private Label countdown;
    private Runnable onTimeout;
    private IntConsumer onSecondChanged;

    private double totalSeconds;
    private double remainingSeconds;

    private long lastFrame;
    private int lastDisplayedSecond = -1;

    private double lastHeartbeatRate;

    private boolean paused;
    private boolean timedOut;

    /** Nasce fermo: il conto parte solo con {@link #start(int, Label, Runnable)}. */
    public CountdownClock() {
    }

    /**
     * Avvia un countdown senza callback sui singoli secondi.
     *
     * @param seconds durata del conto alla rovescia
     * @param countdown l'etichetta su cui scrivere il tempo residuo
     * @param onTimeout cosa fare quando il tempo si esaurisce
     */
    public void start(int seconds, Label countdown, Runnable onTimeout) {
        start(seconds, countdown, onTimeout, null);
    }

    /**
     * Avvia un countdown.
     *
     * @param seconds durata iniziale
     * @param countdown etichetta da aggiornare
     * @param onTimeout callback allo scadere
     * @param onSecondChanged callback invocato quando cambia il secondo
     */
    public void start(
            int seconds,
            Label countdown,
            Runnable onTimeout,
            IntConsumer onSecondChanged
    ) {
        if (seconds <= 0 || countdown == null || onTimeout == null) {

            throw new IllegalArgumentException(
                    "Durata, etichetta e callback devono essere validi."
            );
        }

        stop();

        this.countdown = countdown;
        this.onTimeout = onTimeout;
        this.onSecondChanged = onSecondChanged;

        this.totalSeconds = seconds;
        this.remainingSeconds = seconds;

        this.lastFrame = 0L;
        this.lastDisplayedSecond = -1;

        this.paused = false;
        this.timedOut = false;

        heartbeat = createHeartbeat();

        updateCountdown(remainingSeconds);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };

        timer.start();
    }

    /**
     * Mette in pausa il countdown e il battito.
     */
    public void pause() {
        if (timer == null || paused || timedOut) {
            return;
        }

        paused = true;
        lastFrame = 0L;

        if (heartbeat != null) {
            heartbeat.pause();
        }
    }

    /**
     * Riprende il countdown dal tempo rimanente.
     */
    public void resume() {
        if (timer == null || !paused || timedOut) {
            return;
        }

        paused = false;
        lastFrame = 0L;

        if (heartbeat != null) {
            heartbeat.play();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * @return secondi interi ancora disponibili
     */
    public int remainingSeconds() {
        return (int) Math.ceil(Math.max(0, remainingSeconds));
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (heartbeat != null) {
            heartbeat.stop();
            heartbeat.dispose();
            heartbeat = null;
        }

        countdown = null;
        onTimeout = null;
        onSecondChanged = null;

        totalSeconds = 0;
        remainingSeconds = 0;

        lastFrame = 0L;
        lastDisplayedSecond = -1;

        paused = false;
        timedOut = false;
    }

    private void tick(long now) {
        if (paused || timedOut) {
            lastFrame = 0L;
            return;
        }

        if (lastFrame == 0L) {
            lastFrame = now;
            return;
        }

        double delta =
                (now - lastFrame)
                        / 1_000_000_000.0;

        lastFrame = now;

        /*
         * Il frame è troppo distante dal precedente: la scena non stava
         * aggiornandosi (tipicamente una modale aperta sul thread grafico).
         * Il riferimento è già riallineato: saltiamo il frame senza scalare
         * il tempo dell'interruzione dal countdown.
         */
        if (delta > SceneFx.MAX_FRAME_SECONDS) {
            return;
        }

        remainingSeconds = Math.max(0, remainingSeconds - delta);

        updateCountdown(remainingSeconds);

        if (heartbeat != null && totalSeconds > 0) {

            double elapsed =
                    totalSeconds
                            - remainingSeconds;

            double fraction = Math.max(0, Math.min(1, elapsed / totalSeconds));

            double rate = 1.0 + 1.1 * fraction * fraction;

            /*
             * Cambiare il ritmo attraversa il motore audio del sistema: farlo a
             * ogni fotogramma costa molto piu' di quanto si senta, perche' una
             * variazione minima e' impercettibile. Lo aggiorniamo solo quando
             * supera un gradino udibile.
             */
            if (Math.abs(rate - lastHeartbeatRate) >= 0.02) {
                lastHeartbeatRate = rate;

                heartbeat.setRate(rate);
            }
        }

        if (remainingSeconds <= 0) {
            timedOut = true;

            if (timer != null) {
                timer.stop();
            }

            Runnable callback = onTimeout;

            if (callback != null) {
                callback.run();
            }
        }
    }

    private void updateCountdown(double remaining) {
        if (countdown == null) {
            return;
        }

        int total = (int) Math.ceil(remaining);

        countdown.pseudoClassStateChanged(URGENT, remaining <= 10);

        /*
         * Il metodo viene chiamato a ogni fotogramma, ma il testo cambia una
         * volta al secondo: riscriverlo sempre costringerebbe JavaFX a
         * rimisurare e ridisegnare l'etichetta sessanta volte al secondo,
         * rubando tempo al thread che deve anche rispondere ai clic.
         */
        if (total != lastDisplayedSecond) {
            lastDisplayedSecond = total;

            countdown.setText(String.format("%02d:%02d", total / 60, total % 60));

            if (onSecondChanged != null) {
                onSecondChanged.accept(total);
            }
        }
    }

    private MediaPlayer createHeartbeat() {
        URL url = getClass().getResource("/audio/heartbeat.mp3");

        if (url == null) {
            return null;
        }

        MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));

        player.setCycleCount(MediaPlayer.INDEFINITE);

        player.setVolume(0.9);

        /*
         * Il file diventa pronto in un momento qualsiasi, anche mentre
         * l'orologio e' gia' in pausa perche' e' comparso un dilemma: senza
         * questo controllo il battito partirebbe comunque, con un colpo
         * isolato fuori contesto.
         */
        player.setOnReady(() -> {
            if (!paused && !timedOut) {
                player.play();
            }
        });

        return player;
    }
}
