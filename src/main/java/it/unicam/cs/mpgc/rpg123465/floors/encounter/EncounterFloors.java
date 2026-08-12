package it.unicam.cs.mpgc.rpg123465.floors.encounter;

import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.Questions;

import java.util.List;

/**
 * Catalogo dei piani basati sui dilemmi "Preferiresti".
 */
public final class EncounterFloors {

    private EncounterFloors() {
        // Solo metodi statici.
    }

    /**
     * Piano I — I Topi.
     *
     * La domanda viene estratta casualmente dal catalogo JSON.
     */
    public static FearEncounter topi() {
        Dilemma dilemma =
                Questions.repository()
                        .randomQuestions(
                                "topi",
                                1
                        )
                        .getFirst();

        return new FearEncounter(
                "Piano I — I Topi",
                "/images/scenes/floor1-topi.jpg",
                dilemma.question(),
                45,
                List.of(
                        new FearChoice(
                                dilemma.first().text(),
                                dilemma.first().trait()
                        ),
                        new FearChoice(
                                dilemma.second().text(),
                                dilemma.second().trait()
                        )
                )
        );
    }
}
