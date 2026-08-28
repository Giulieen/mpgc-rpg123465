package it.unicam.cs.mpgc.rpg123465.testing;

import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;
import it.unicam.cs.mpgc.rpg123465.audio.SoundPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Servizio audio finto: non riproduce nulla, annota soltanto cosa gli è stato
 * chiesto.
 *
 * <p>
 * Esiste perché il motore multimediale di JavaFX non si può avviare in una
 * prova automatica. Sostituendolo al posto di quello vero si può verificare
 * <em>che</em> un suono sia stato richiesto, senza dipendere da una scheda
 * audio.
 */
public final class FakeSoundPlayer implements SoundPlayer {

    private final List<String> played = new ArrayList<>();
    private final List<String> looped = new ArrayList<>();
    private final List<String> preloaded = new ArrayList<>();

    private int stopAllCount;

    @Override
    public void preload(String... resources) {
        if (resources == null) {
            return;
        }

        for (String resource : resources) {
            preloaded.add(resource);
        }
    }

    @Override
    public void play(String resource, double volume) {
        if (resource == null) {
            return;
        }

        played.add(resource);
    }

    @Override
    public void play(SoundCue cue) {
        if (cue == null) {
            return;
        }

        played.add(cue.resource());
    }

    @Override
    public void loop(String resource, double volume) {
        looped.add(resource);
    }

    @Override
    public void occasional(String resource, double volume,
                           double minSeconds, double maxSeconds) {
        played.add(resource);
    }

    @Override
    public void stopAll() {
        stopAllCount++;
    }

    /** @return gli effetti riprodotti, nell'ordine in cui sono stati chiesti */
    public List<String> played() {
        return List.copyOf(played);
    }

    /** @return gli ambienti avviati in loop */
    public List<String> looped() {
        return List.copyOf(looped);
    }

    /** @return gli effetti preparati in anticipo */
    public List<String> preloaded() {
        return List.copyOf(preloaded);
    }

    /** @return quante volte è stato chiesto di zittire tutto */
    public int stopAllCount() {
        return stopAllCount;
    }
}
