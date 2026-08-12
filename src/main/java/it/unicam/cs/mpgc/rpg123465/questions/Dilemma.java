package it.unicam.cs.mpgc.rpg123465.questions;

/**
 * Un dilemma "Preferiresti" caricato dal catalogo JSON.
 *
 * @param id identificatore della domanda
 * @param question testo della domanda
 * @param first prima risposta
 * @param second seconda risposta
 */
public record Dilemma(
        int id,
        String question,
        DilemmaOption first,
        DilemmaOption second
) {

    public Dilemma {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "L'id della domanda deve essere positivo."
            );
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "La domanda non può essere vuota."
            );
        }

        if (first == null || second == null) {
            throw new IllegalArgumentException(
                    "Un dilemma deve avere due risposte."
            );
        }
    }
}
