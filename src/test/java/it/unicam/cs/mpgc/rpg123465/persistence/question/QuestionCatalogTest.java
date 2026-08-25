package it.unicam.cs.mpgc.rpg123465.persistence.question;

import it.unicam.cs.mpgc.rpg123465.model.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionCatalogException;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sul catalogo che il gioco spedisce davvero.
 *
 * <p>
 * Verificano solo l'integrità strutturale — identificatori, forma delle
 * domande, quantità sufficiente per ogni piano — e non la distribuzione dei
 * tratti, che è una scelta narrativa ancora aperta.
 */
class QuestionCatalogTest {

    /** Quante domande ogni piano estrae dalla propria categoria. */
    private static final Map<String, Integer> REQUIRED = Map.of("topi", 1, "buio", 3, "altezze", 4);

    private static Map<String, List<Dilemma>> catalog() {
        Type type = new TypeToken<Map<String, List<Dilemma>>>() { }.getType();

        try (InputStream stream = QuestionCatalogTest.class
                .getResourceAsStream("/data/questions.json")) {

            assertNotNull(stream, "catalogo del gioco non trovato");

            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);

        } catch (Exception exception) {
            throw new AssertionError("catalogo del gioco illeggibile", exception);
        }
    }

    @Test
    void ilCatalogoContieneLeCategorieUsateDaiPiani() {
        assertEquals(REQUIRED.keySet(), catalog().keySet());
    }

    @Test
    void ogniCategoriaHaAbbastanzaDomandePerIlSuoPiano() {
        Map<String, List<Dilemma>> catalog = catalog();

        REQUIRED.forEach((category, needed) ->
                assertTrue(catalog.get(category).size() >= needed,
                        "la categoria " + category + " ha "
                                + catalog.get(category).size()
                                + " domande ma il piano ne estrae " + needed));
    }

    @Test
    void tuttiGliIdentificatoriSonoPositiviEUnivoci() {
        Set<Integer> seen = new HashSet<>();

        catalog().forEach((category, questions) ->
                questions.forEach(dilemma -> {
                    assertTrue(dilemma.id() > 0, "identificatore non positivo in " + category);
                    assertTrue(seen.add(dilemma.id()), "identificatore ripetuto: " + dilemma.id());
                }));
    }

    @Test
    void ogniDomandaHaUnTestoEDueRisposteComplete() {
        catalog().forEach((category, questions) ->
                questions.forEach(dilemma -> {
                    assertFalse(dilemma.question().isBlank(), "domanda vuota, id " + dilemma.id());

                    assertNotNull(dilemma.first(), "prima risposta mancante, id " + dilemma.id());
                    assertNotNull(dilemma.second(), "seconda risposta mancante, id " + dilemma.id());

                    assertFalse(dilemma.first().text().isBlank(),
                            "prima risposta vuota, id " + dilemma.id());
                    assertFalse(dilemma.second().text().isBlank(),
                            "seconda risposta vuota, id " + dilemma.id());
                }));
    }

    @Test
    void ogniRispostaPortaUnTrattoRiconosciuto() {
        Set<ProfileTrait> known = Set.of(ProfileTrait.values());

        catalog().forEach((category, questions) ->
                questions.forEach(dilemma -> {
                    assertTrue(known.contains(dilemma.first().trait()),
                            "tratto ignoto nella prima risposta, id " + dilemma.id());
                    assertTrue(known.contains(dilemma.second().trait()),
                            "tratto ignoto nella seconda risposta, id " + dilemma.id());
                }));
    }

    /**
     * Un dilemma con lo stesso tratto su entrambe le risposte non offrirebbe
     * alcuna scelta: qualunque risposta produrrebbe lo stesso effetto.
     */
    @Test
    void ogniDomandaOffreDueTrattiDiversi() {
        catalog().forEach((category, questions) ->
                questions.forEach(dilemma ->
                        assertFalse(dilemma.first().trait() == dilemma.second().trait(),
                                "le due risposte portano lo stesso tratto, id " + dilemma.id())));
    }

    @Test
    void ilCatalogoRealeSiCaricaAncheDalRepository() {
        QuestionRepository repository = new JsonQuestionRepository();

        REQUIRED.forEach((category, needed) ->
                assertEquals(needed, repository.randomQuestions(category, needed).size()));
    }
}
