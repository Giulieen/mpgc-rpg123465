package it.unicam.cs.mpgc.rpg123465.challenge;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;

/**
 * Applica al nemico l'esito di un confronto e ne descrive il risultato.
 * <p>
 * Concentra qui la logica del confronto (risposta costruttiva o negativa)
 * così da mantenere il controller privo di grandi blocchi condizionali.
 * Il confronto non sostituisce il combattimento: applica solo un piccolo
 * bonus o malus, senza toccare {@code CombatEngine}.
 */
public class ConfrontationResolver {

    /** Danno inflitto al nemico quando la risposta è costruttiva. */
    private static final int ENEMY_HP_PENALTY = 5;

    /** Vita recuperata dal nemico quando la risposta alimenta la paura. */
    private static final int ENEMY_HP_RECOVERY = 3;

    /**
     * Valuta la risposta scelta e ne applica l'effetto al nemico.
     *
     * @param question domanda posta durante il confronto
     * @param answerIndex indice della risposta scelta
     * @param enemy nemico coinvolto nel combattimento
     * @return messaggio descrittivo dell'esito
     */
    public String resolve(ChallengeQuestion question, int answerIndex, Enemy enemy) {
        if (question == null) {
            throw new IllegalArgumentException("La domanda non può essere null.");
        }
        if (enemy == null) {
            throw new IllegalArgumentException("Il nemico non può essere null.");
        }

        if (question.isConstructiveAnswer(answerIndex)) {
            enemy.takeDamage(ENEMY_HP_PENALTY);
            return "La Paura vacilla. " + question.successMessage()
                    + " (" + enemy.getName() + " perde " + ENEMY_HP_PENALTY + " HP)";
        }

        enemy.heal(ENEMY_HP_RECOVERY);
        return "La Paura sorride. " + question.failureMessage()
                + " (" + enemy.getName() + " recupera " + ENEMY_HP_RECOVERY + " HP)";
    }
}
