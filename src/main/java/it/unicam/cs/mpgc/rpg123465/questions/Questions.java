package it.unicam.cs.mpgc.rpg123465.questions;

/**
 * Punto unico di accesso al catalogo predefinito delle domande.
 */
public final class Questions {

    private static final QuestionRepository DEFAULT =
            new JsonQuestionRepository();

    private Questions() {
        // Solo metodi statici.
    }

    /**
     * @return repository condiviso delle domande
     */
    public static QuestionRepository repository() {
        return DEFAULT;
    }
}
