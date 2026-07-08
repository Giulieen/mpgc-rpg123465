package it.unicam.cs.mpgc.rpg123465.challenge;

import java.util.List;

/**
 * Rappresenta una domanda narrativa usata durante una prova interiore.
 *
 * @param question testo della domanda
 * @param answers risposte disponibili
 * @param constructiveAnswerIndex indice della risposta costruttiva
 * @param successMessage messaggio mostrato se la risposta è costruttiva
 * @param failureMessage messaggio mostrato se la risposta alimenta la paura
 */
public record ChallengeQuestion(
        String question,
        List<String> answers,
        int constructiveAnswerIndex,
        String successMessage,
        String failureMessage
) {

    public boolean isConstructiveAnswer(int index) {
        return index == constructiveAnswerIndex;
    }
}