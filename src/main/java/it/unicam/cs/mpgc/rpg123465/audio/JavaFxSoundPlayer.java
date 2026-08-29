package it.unicam.cs.mpgc.rpg123465.audio;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Il servizio audio realizzato con JavaFX.
 *
 * <p>
 * Usa due meccanismi diversi, perché le due famiglie di suoni hanno esigenze
 * opposte. Gli effetti passano da {@link AudioClip}: il file viene caricato
 * per intero in memoria una volta sola e poi suonato senza latenza, che è ciò
 * che serve quando il suono deve rispondere a un tasto. Gli ambienti passano
 * invece da {@link MediaPlayer}, che riproduce mentre legge: per una traccia
 * lunga sarebbe uno spreco tenerla tutta in memoria, e qualche millisecondo di
 * attesa all'avvio non si nota.
 *
 * <p>
 * Se un file non esiste il suono viene semplicemente saltato, senza errori:
 * così si possono aggiungere le tracce una alla volta senza rompere il gioco.
 */
public final class JavaFxSoundPlayer implements SoundPlayer {

    private final List<MediaPlayer> loops = new ArrayList<>();
    private final Map<String, AudioClip> clips = new ConcurrentHashMap<>();

    /*
     * Gli effetti con ritardo sono programmati e non ancora partiti: vanno
     * tenuti, perché un cambio di scena deve poterli annullare. Senza questo
     * elenco l'urlo della caduta suonerebbe sopra il menu se il giocatore
     * uscisse entro i tre quarti di secondo che lo separano dal tonfo.
     */
    private final List<PauseTransition> pending = new ArrayList<>();

    private PauseTransition occasional;

    /**
     * Carica in anticipo gli effetti indicati, così che la prima riproduzione
     * parta senza ritardo.
     *
     * <p>
     * Gli {@link AudioClip} devono essere costruiti sul thread grafico,
     * altrimenti restano muti. Accodarli però tutti insieme li farebbe
     * eseguire nello stesso impulso, con una pausa visibile proprio mentre il
     * menu si sta disegnando: qui la coda avanza di un elemento per volta,
     * perché ogni caricamento chiede il successivo. Il thread resta così
     * libero fra un file e l'altro.
     *
     * @param resources percorsi nel classpath degli effetti da preparare
     */
    @Override
    public void preload(String... resources) {
        if (resources == null) {
            return;
        }

        Deque<String> queue = new ArrayDeque<>(Arrays.asList(resources));

        loadNext(queue);
    }

    private void loadNext(Deque<String> queue) {
        String resource = queue.poll();

        if (resource == null) {
            return;
        }

        Platform.runLater(() -> {
            clip(resource);

            loadNext(queue);
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Più chiamate si sovrappongono, così si possono stratificare gli
     * ambienti.
     */
    @Override
    public void loop(String resource, double volume) {
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
     * {@inheritDoc}
     *
     * <p>
     * Un {@code resource} nullo viene ignorato, così una scelta può
     * semplicemente non avere suono.
     */
    @Override
    public void play(String resource, double volume) {
        if (resource == null) {
            return;
        }

        AudioClip clip = clip(resource);
        if (clip != null) {
            clip.play(volume);
        }
    }

    @Override
    public void play(SoundCue cue) {
        if (cue == null) {
            return;
        }
        if (cue.delaySeconds() <= 0) {
            play(cue.resource(), cue.volume());
            return;
        }

        PauseTransition wait = new PauseTransition(Duration.seconds(cue.delaySeconds()));
        wait.setOnFinished(event -> {
            pending.remove(wait);

            play(cue.resource(), cue.volume());
        });

        pending.add(wait);
        wait.play();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Serve per i suoni sporadici, come il gufo nel bosco.
     */
    @Override
    public void occasional(String resource, double volume,
                           double minSeconds, double maxSeconds) {
        if (resolve(resource) == null) {
            return;
        }
        stopOccasional();
        scheduleOccasional(resource, volume, minSeconds, maxSeconds);
    }

    @Override
    public void stopAll() {
        for (MediaPlayer player : loops) {
            player.stop();
            player.dispose();
        }
        loops.clear();

        for (AudioClip clip : clips.values()) {
            clip.stop();
        }

        for (PauseTransition wait : pending) {
            wait.stop();
        }
        pending.clear();

        stopOccasional();
    }

    private AudioClip clip(String resource) {
        return clips.computeIfAbsent(resource, key -> {
            URL url = resolve(key);
            return url == null ? null : new AudioClip(url.toExternalForm());
        });
    }

    private void scheduleOccasional(String resource, double volume,
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

    private void stopOccasional() {
        if (occasional != null) {
            occasional.stop();
            occasional = null;
        }
    }

    private URL resolve(String resource) {
        return JavaFxSoundPlayer.class.getResource(resource);
    }
}
