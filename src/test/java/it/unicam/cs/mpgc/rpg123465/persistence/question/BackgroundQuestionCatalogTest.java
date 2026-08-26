package it.unicam.cs.mpgc.rpg123465.persistence.question;

import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionCatalogException;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;
import it.unicam.cs.mpgc.rpg123465.testing.FakeQuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackgroundQuestionCatalogTest {

    @Test
    void leDomandeArrivanoDalCatalogoLetto() {
        QuestionRepository catalog =
                new BackgroundQuestionCatalog(FakeQuestionRepository::new);

        assertEquals(2, catalog.randomQuestions("topi", 2).size());
    }

    /**
     * Il punto dell'intera classe: chi chiede le domande non deve accorgersi
     * che la lettura era ancora in corso, deve solo riceverle.
     */
    @Test
    void unaLetturaLentaVieneAttesaSenzaPerdereIlRisultato() {
        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            Thread.sleep(150);
            return new FakeQuestionRepository();
        });

        assertEquals(3, catalog.randomQuestions("buio", 3).size());
    }

    /** La lettura non deve avvenire sul thread che costruisce l'oggetto. */
    @Test
    void laLetturaAvvieneSuUnAltroThread() {
        AtomicReference<String> reader = new AtomicReference<>();

        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            reader.set(Thread.currentThread().getName());
            return new FakeQuestionRepository();
        });

        catalog.randomQuestions("topi", 1);

        assertNotEquals(Thread.currentThread().getName(), reader.get());
        assertFalse(reader.get() == null);
    }

    /**
     * L'errore del catalogo deve riemergere così com'è: l'interfaccia lo
     * intercetta per tipo, e un involucro dell'esecutore lo renderebbe
     * invisibile.
     */
    @Test
    void ilCatalogoRottoRiportaLEccezioneOriginale() {
        QuestionCatalogException expected =
                new QuestionCatalogException("catalogo illeggibile");

        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            throw expected;
        });

        QuestionCatalogException thrown = assertThrows(
                QuestionCatalogException.class,
                () -> catalog.randomQuestions("topi", 1));

        assertEquals(expected, thrown);
    }

    /** Un guasto qualsiasi diventa comunque un errore di catalogo. */
    @Test
    void unGuastoImprevistoDiventaUnErroreDiCatalogo() {
        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            throw new IllegalStateException("disco staccato");
        });

        QuestionCatalogException thrown = assertThrows(
                QuestionCatalogException.class,
                () -> catalog.randomQuestions("topi", 1));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    /**
     * Un fallimento non va memorizzato: chi corregge il file e riprova deve
     * ottenere una lettura nuova, non l'errore di prima.
     */
    @Test
    void dopoUnFallimentoIlTentativoSuccessivoRilegge() {
        AtomicInteger reads = new AtomicInteger();

        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            if (reads.incrementAndGet() == 1) {
                throw new QuestionCatalogException("primo tentativo fallito");
            }

            return new FakeQuestionRepository();
        });

        assertThrows(QuestionCatalogException.class,
                () -> catalog.randomQuestions("topi", 1));

        assertEquals(1, catalog.randomQuestions("topi", 1).size());
        assertEquals(2, reads.get());
    }

    /** Una lettura riuscita avviene una volta sola, non a ogni richiesta. */
    @Test
    void ilCatalogoLettoVieneRiusato() {
        AtomicInteger reads = new AtomicInteger();

        QuestionRepository catalog = new BackgroundQuestionCatalog(() -> {
            reads.incrementAndGet();
            return new FakeQuestionRepository();
        });

        catalog.randomQuestions("topi", 1);
        catalog.randomQuestions("buio", 1);
        catalog.randomQuestions("altezze", 1);

        assertEquals(1, reads.get());
    }

    @Test
    void laSorgenteEObbligatoria() {
        assertThrows(IllegalArgumentException.class,
                () -> new BackgroundQuestionCatalog(null));
    }
}
