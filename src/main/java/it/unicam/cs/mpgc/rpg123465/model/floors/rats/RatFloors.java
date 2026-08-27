package it.unicam.cs.mpgc.rpg123465.model.floors.rats;

import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;

import java.util.List;

/**
 * Catalogo dei piani basati sui dilemmi "Preferiresti".
 */
public final class RatFloors {

    private RatFloors() {
        // Solo metodi statici.
    }

    /**
     * Piano I — I Topi.
     *
     * @param questions catalogo da cui estrarre la domanda del piano
     * @return contenuto del Piano I
     */
    public static FearEncounter fearEncounter(QuestionRepository questions) {
        Dilemma dilemma =
                questions
                        .randomQuestions("topi", 1)
                        .getFirst();

        return new FearEncounter(
                "Piano I — I Topi",
                "/images/scenes/floor1-rats.jpg",
                dilemma.question(),
                List.of(
                        new FearChoice(dilemma.first().text(), dilemma.first().trait()),
                        new FearChoice(dilemma.second().text(), dilemma.second().trait())
                )
        );
    }
}
