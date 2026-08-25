package it.unicam.cs.mpgc.rpg123465.questions;

import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;

/**
 * Una risposta possibile a un dilemma "Preferiresti".
 *
 * @param text testo mostrato al giocatore
 * @param trait tratto nascosto associato alla risposta
 */
public record DilemmaOption(String text, ProfileTrait trait) {

    public DilemmaOption {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Il testo della risposta non può essere vuoto.");
        }

        if (trait == null) {
            throw new IllegalArgumentException("Il tratto non può essere null.");
        }
    }
}
