package it.unicam.cs.mpgc.rpg123465.fear;

import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;

import java.util.List;

/**
 * Una possibile reazione a una paura: un gesto concreto, la sua conseguenza
 * narrativa e l'effetto su Lucidità e Stress.
 *
 * @param label         il gesto mostrato sul pulsante
 * @param reaction      cosa accade scegliendolo
 * @param lucidityDelta variazione di Lucidità (può essere negativa)
 * @param stressDelta   variazione di Stress (può essere negativa)
 * @param attitude      atteggiamento che Chimeris memorizza
 * @param sounds        effetti sonori della reazione, riprodotti insieme così
 *                      da poterli mescolare; lista vuota per il silenzio
 * @param shakesRoom    se la stanza deve tremare alla reazione
 */
public record FearChoice(
        String label,
        String reaction,
        int lucidityDelta,
        int stressDelta,
        Attitude attitude,
        List<SoundCue> sounds,
        boolean shakesRoom) {
}
