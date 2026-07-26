package it.unicam.cs.mpgc.rpg123465.fear;

import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;
import it.unicam.cs.mpgc.rpg123465.domain.Attitude;

import java.util.List;

/**
 * Una possibile reazione a una paura: un gesto concreto, la sua conseguenza
 * narrativa e il prezzo che comporta.
 * <p>
 * I prezzi sono di tre nature diverse e non vanno confusi: il <b>danno</b>
 * ferisce il corpo (un morso, una caduta), la <b>Lucidità</b> logora la mente,
 * lo <b>Stress</b> è la tensione del momento. Una stessa paura può costare
 * l'uno o l'altra a seconda di come la si affronta.
 *
 * @param label         il gesto mostrato sul pulsante
 * @param reaction      cosa accade scegliendolo
 * @param damage        ferite riportate: sottratte alla Vita, mai negative
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
        int damage,
        int lucidityDelta,
        int stressDelta,
        Attitude attitude,
        List<SoundCue> sounds,
        boolean shakesRoom) {

    /**
     * @throws IllegalArgumentException se il danno è negativo: una reazione
     *         può ferire, non curare
     */
    public FearChoice {
        if (damage < 0) {
            throw new IllegalArgumentException("Il danno non può essere negativo.");
        }
    }

    /**
     * Indica se la reazione lascia un segno sul corpo, e non solo sulla mente.
     *
     * @return {@code true} se comporta ferite
     */
    public boolean hurts() {
        return damage > 0;
    }
}
