package it.unicam.cs.mpgc.rpg123465.floors.encounter;

import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;

import java.util.List;

/**
 * Un piano basato su un dilemma "Preferiresti".
 *
 * @param title titolo del piano
 * @param backgroundResource immagine di sfondo nel classpath
 * @param situation domanda mostrata al giocatore
 * @param initialStress stress applicato entrando nel piano
 * @param choices le due risposte disponibili
 */
public record FearEncounter(
        String title,
        String backgroundResource,
        String situation,
        int initialStress,
        List<FearChoice> choices
) implements FloorContent {

    public FearEncounter {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Il titolo non può essere vuoto."
            );
        }

        if (backgroundResource == null || backgroundResource.isBlank()) {
            throw new IllegalArgumentException(
                    "Lo sfondo non può essere vuoto."
            );
        }

        if (situation == null || situation.isBlank()) {
            throw new IllegalArgumentException(
                    "La domanda non può essere vuota."
            );
        }

        if (initialStress < 0) {
            throw new IllegalArgumentException(
                    "Lo stress iniziale non può essere negativo."
            );
        }

        if (choices == null || choices.size() != 2) {
            throw new IllegalArgumentException(
                    "Un dilemma deve avere esattamente due risposte."
            );
        }

        choices = List.copyOf(choices);
    }
}
