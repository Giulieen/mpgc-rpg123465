package it.unicam.cs.mpgc.rpg123465.questions;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sul caricamento del catalogo. Usano fixture dedicate sotto
 * {@code src/test/resources/data}, non il catalogo del gioco: le regole
 * strutturali devono restare vere anche quando i contenuti cambiano.
 */
class JsonQuestionRepositoryTest {

    private static final String VALID = "/data/questions-valid.json";
    private static final String MALFORMED = "/data/questions-malformed.json";
    private static final String BAD_TRAIT = "/data/questions-bad-trait.json";
    private static final String EMPTY = "/data/questions-empty.json";

    private QuestionRepository valid() {
        return new JsonQuestionRepository(VALID);
    }

    // --- caricamento -----------------------------------------------------

    @Test
    void unCatalogoValidoViCarica() {
        assertEquals(3, valid().randomQuestions("alfa", 3).size());
        assertEquals(2, valid().randomQuestions("beta", 2).size());
    }

    @Test
    void leDomandeArrivanoCompleteDelLoroContenuto() {
        Dilemma dilemma = valid().randomQuestions("beta", 1).get(0);

        assertTrue(dilemma.id() > 0);
        assertTrue(dilemma.question().startsWith("Preferiresti"));
        assertNotNull(dilemma.first().trait());
        assertNotNull(dilemma.second().trait());
        assertTrue(dilemma.first().text().length() > 0);
    }

    @Test
    void ilNomeDellaCategoriaNonDistingueMaiuscoleESpazi() {
        assertEquals(2, valid().randomQuestions("  ALFA  ", 2).size());
    }

    // --- estrazione ------------------------------------------------------

    @Test
    void unEstrazioneNonRipeteLaStessaDomanda() {
        for (int attempt = 0; attempt < 30; attempt++) {
            List<Dilemma> drawn = valid().randomQuestions("alfa", 3);

            Set<Integer> ids = new HashSet<>();
            drawn.forEach(dilemma -> ids.add(dilemma.id()));

            assertEquals(drawn.size(), ids.size(), "domanda ripetuta nella stessa estrazione");
        }
    }

    @Test
    void leDomandeEstratteAppartengonoAllaCategoriaRichiesta() {
        for (Dilemma dilemma : valid().randomQuestions("beta", 2)) {
            assertTrue(dilemma.id() == 4 || dilemma.id() == 5,
                    "domanda estranea alla categoria: " + dilemma.id());
        }
    }

    @Test
    void laListaRestituitaNonEModificabile() {
        List<Dilemma> drawn = valid().randomQuestions("alfa", 1);

        assertThrows(UnsupportedOperationException.class, () -> drawn.add(drawn.get(0)));
    }

    // --- errori di richiesta ---------------------------------------------

    @Test
    void chiederePiuDomandeDiQuanteCeNeSonoEUnErroreDelCatalogo() {
        assertThrows(QuestionCatalogException.class,
                () -> valid().randomQuestions("beta", 3));
    }

    @Test
    void unaCategoriaInesistenteEUnErroreDelCatalogo() {
        assertThrows(QuestionCatalogException.class,
                () -> valid().randomQuestions("gamma", 1));
    }

    @Test
    void unaRichiestaMalPostaEUnErroreDiProgrammazione() {
        assertThrows(IllegalArgumentException.class,
                () -> valid().randomQuestions("alfa", 0));

        assertThrows(IllegalArgumentException.class,
                () -> valid().randomQuestions("alfa", -1));

        assertThrows(IllegalArgumentException.class,
                () -> valid().randomQuestions("", 1));

        assertThrows(IllegalArgumentException.class,
                () -> valid().randomQuestions(null, 1));
    }

    // --- errori del file -------------------------------------------------

    /**
     * Un file rotto deve produrre un errore raccontabile e non un guasto
     * dell'inizializzazione: è il caso che il gioco traduce in un messaggio.
     */
    @Test
    void unJsonMalformatoEUnErroreControllato() {
        QuestionCatalogException error = assertThrows(QuestionCatalogException.class,
                () -> new JsonQuestionRepository(MALFORMED));

        assertTrue(error.getMessage().contains(MALFORMED));
    }

    /**
     * Sintassi valida ma tratto inesistente: la validazione dei record deve
     * scattare in caricamento, non a metà partita.
     */
    @Test
    void unTrattoInesistenteEUnErroreControllato() {
        assertThrows(QuestionCatalogException.class,
                () -> new JsonQuestionRepository(BAD_TRAIT));
    }

    @Test
    void unCatalogoVuotoEUnErroreControllato() {
        assertThrows(QuestionCatalogException.class,
                () -> new JsonQuestionRepository(EMPTY));
    }

    @Test
    void unaRisorsaInesistenteEUnErroreControllato() {
        assertThrows(QuestionCatalogException.class,
                () -> new JsonQuestionRepository("/data/non-esiste.json"));
    }

    @Test
    void unPercorsoVuotoEUnErroreDiProgrammazione() {
        assertThrows(IllegalArgumentException.class,
                () -> new JsonQuestionRepository(""));

        assertThrows(IllegalArgumentException.class,
                () -> new JsonQuestionRepository((String) null));
    }

    // --- validazione dei record ------------------------------------------

    @Test
    void unDilemmaRichiedeIdTestoEDueRisposte() {
        DilemmaOption option = new DilemmaOption("testo", it.unicam.cs.mpgc.rpg123465
                .domain.ProfileTrait.CORAGGIO);

        assertThrows(IllegalArgumentException.class,
                () -> new Dilemma(0, "domanda", option, option));

        assertThrows(IllegalArgumentException.class,
                () -> new Dilemma(1, "  ", option, option));

        assertThrows(IllegalArgumentException.class,
                () -> new Dilemma(1, "domanda", null, option));
    }

    @Test
    void unaRispostaRichiedeTestoETratto() {
        assertThrows(IllegalArgumentException.class,
                () -> new DilemmaOption("", it.unicam.cs.mpgc.rpg123465
                        .domain.ProfileTrait.CORAGGIO));

        assertThrows(IllegalArgumentException.class,
                () -> new DilemmaOption("testo", null));
    }
}
