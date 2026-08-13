package it.unicam.cs.mpgc.rpg123465.testing;

import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.DilemmaOption;
import it.unicam.cs.mpgc.rpg123465.questions.QuestionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalogo deterministico per i test.
 *
 * <p>
 * Genera domande riconoscibili dal testo, così una prova può dimostrare che è
 * stato usato questo catalogo e non quello spedito col gioco, e annota le
 * richieste ricevute.
 */
public final class FakeQuestionRepository implements QuestionRepository {

    /** Prefisso dei testi generati, per distinguerli dal catalogo reale. */
    public static final String MARKER = "DOMANDA DI PROVA";

    private final List<String> requestedCategories = new ArrayList<>();

    private int nextId = 1;

    public List<String> requestedCategories() {
        return List.copyOf(requestedCategories);
    }

    @Override
    public List<Dilemma> randomQuestions(String category, int count) {
        requestedCategories.add(category);

        List<Dilemma> drawn = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int id = nextId++;

            drawn.add(new Dilemma(
                    id,
                    MARKER + " " + id + " (" + category + ")",
                    new DilemmaOption("prima " + id, ProfileTrait.CORAGGIO),
                    new DilemmaOption("seconda " + id, ProfileTrait.CURIOSITA)));
        }

        return List.copyOf(drawn);
    }
}
