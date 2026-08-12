package it.unicam.cs.mpgc.rpg123465.floors.encounter;

import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;

/**
 * Una possibile risposta a un dilemma "Preferiresti".
 *
 * Il tratto associato alla risposta non viene mostrato al giocatore:
 * serve soltanto per costruire progressivamente il profilo finale.
 *
 * @param label testo mostrato sul pulsante
 * @param trait tratto nascosto associato alla risposta
 */
public record FearChoice(
        String label,
        ProfileTrait trait
) {

    public FearChoice {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "Il testo della risposta non può essere vuoto."
            );
        }

        if (trait == null) {
            throw new IllegalArgumentException(
                    "Il tratto della risposta non può essere null."
            );
        }
    }
}
