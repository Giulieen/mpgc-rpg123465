package it.unicam.cs.mpgc.rpg123465.audio;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Suoni del gioco: ambienti in loop ed effetti singoli.
 * <p>
 * Se un file non esiste il suono viene semplicemente saltato, senza errori:
 * così si possono aggiungere le tracce una alla volta senza rompere il gioco.
 * <p>
 * Gli effetti vanno {@link #preload(String...) precaricati} all'avvio: la
 * costruzione di un {@link AudioClip} decodifica l'intero file e, se avvenisse
 * al primo clic sul thread grafico, bloccherebbe l'interfaccia.
 */
public final class Sound {

    private static final List<MediaPlayer> loops = new ArrayList<>();
    private static final Map<String, AudioClip> clips = new ConcurrentHashMap<>();

    private static PauseTransition occasional;

    private Sound() {
        // Solo metodi statici.
    }

    /**
     * Decodifica in anticipo gli effetti indicati, così che la prima
     * riproduzione parta senza ritardo.
     * <p>
     * Gli {@link AudioClip} devono essere costruiti sul thread grafico,
     * altrimenti restano muti: li carichiamo uno per volta con
     * {@link Platform#runLater}, distribuendoli su più frame in modo che
     * l'avvio resti fluido.
     */
    public static void preload(String... resources) {
        for (String resource : resources) {
            Platform.runLater(() -> clip(resource));
        }
    }

    /**
     * Avvia un suono ripetuto all'infinito (ambiente di sottofondo). Più
     * chiamate si sovrappongono, così si possono stratificare gli ambienti.
     *
     * @param resource percorso nel classpath, es. {@code "/audio/ambience-forest.mp3"}
     * @param volume   volume da 0 a 1
     */
    public static void loop(String resource, double volume) {
        URL url = resolve(resource);
        if (url == null) {
            return;
        }

        MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(volume);
        player.play();
        loops.add(player);
    }

    /**
     * Riproduce un effetto singolo. Un {@code resource} nullo viene ignorato,
     * così una scelta può semplicemente non avere suono.
     *
     * @param resource percorso nel classpath, oppure {@code null}
     * @param volume   volume da 0 a 1
     */
    public static void play(String resource, double volume) {
        if (resource == null) {
            return;
        }

        AudioClip clip = clip(resource);
        if (clip != null) {
            clip.play(volume);
        }
    }

    /**
     * Riproduce un effetto rispettandone volume ed eventuale ritardo.
     *
     * @param cue il suono da riprodurre, oppure {@code null}
     */
    public static void play(SoundCue cue) {
        if (cue == null) {
            return;
        }
        if (cue.delaySeconds() <= 0) {
            play(cue.resource(), cue.volume());
            return;
        }

        PauseTransition wait = new PauseTransition(Duration.seconds(cue.delaySeconds()));
        wait.setOnFinished(event -> play(cue.resource(), cue.volume()));
        wait.play();
    }

    /**
     * Riproduce un effetto a intervalli casuali, finché non si cambia scena:
     * serve per i suoni sporadici, come il gufo nel bosco.
     *
     * @param minSeconds attesa minima fra una riproduzione e l'altra
     * @param maxSeconds attesa massima
     */
    public static void occasional(String resource, double volume,
                                  double minSeconds, double maxSeconds) {
        if (resolve(resource) == null) {
            return;
        }
        stopOccasional();
        scheduleOccasional(resource, volume, minSeconds, maxSeconds);
    }

    /**
     * Zittisce tutto: ambienti in loop, suoni sporadici ed effetti ancora in
     * corso. Da chiamare quando si cambia scena.
     */
    public static void stopAll() {
        for (MediaPlayer player : loops) {
            player.stop();
            player.dispose();
        }
        loops.clear();

        for (AudioClip clip : clips.values()) {
            clip.stop();
        }

        stopOccasional();
    }

    private static AudioClip clip(String resource) {
        return clips.computeIfAbsent(resource, key -> {
            URL url = resolve(key);
            return url == null ? null : new AudioClip(url.toExternalForm());
        });
    }

    private static void scheduleOccasional(String resource, double volume,
                                           double minSeconds, double maxSeconds) {
        double delay = minSeconds + Math.random() * (maxSeconds - minSeconds);

        PauseTransition wait = new PauseTransition(Duration.seconds(delay));
        wait.setOnFinished(event -> {
            play(resource, volume);
            scheduleOccasional(resource, volume, minSeconds, maxSeconds);
        });

        occasional = wait;
        wait.play();
    }

    private static void stopOccasional() {
        if (occasional != null) {
            occasional.stop();
            occasional = null;
        }
    }

    private static URL resolve(String resource) {
        return Sound.class.getResource(resource);
    }
}
