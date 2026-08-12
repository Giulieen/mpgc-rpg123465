package it.unicam.cs.mpgc.rpg123465.questions;

import java.util.List;

/**
 * Astrazione del catalogo delle domande.
 *
 * Le scene dipendono da questa interfaccia e non dal formato JSON.
 */
public interface QuestionRepository {

    /**
     * Estrae domande casuali senza ripetizioni dalla categoria richiesta.
     *
     * @param category categoria del piano: topi, buio o altezze
     * @param count numero di domande richieste
     * @return domande casuali senza ripetizioni
     */
    List<Dilemma> randomQuestions(
            String category,
            int count
    );
}
