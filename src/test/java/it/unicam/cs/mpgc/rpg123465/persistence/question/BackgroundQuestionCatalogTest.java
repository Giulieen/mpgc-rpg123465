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
        QuestionRepository catalogo =
                new BackgroundQuestionCatalog(FakeQuestionRepository::new);

        assertEquals(2, catalogo.randomQuestions("topi", 2).size());
    }

    /**
     * Il punto dell'intera classe: chi chiede le domande non deve accorgersi
     * che la lettura era ancora in corso, deve solo riceverle.
     */
    @Test
    void unaLetturaLentaVieneAttesaSenzaPerdereIlRisultato() {
        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            Thread.sleep(150);
            return new FakeQuestionRepository();
        });

        assertEquals(3, catalogo.randomQuestions("buio", 3).size());
    }

    /** La lettura non deve avvenire sul thread che costruisce l'oggetto. */
    @Test
    void laLetturaAvvieneSuUnAltroThread() {
        AtomicReference<String> lettore = new AtomicReference<>();

        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            lettore.set(Thread.currentThread().getName());
            return new FakeQuestionRepository();
        });

        catalogo.randomQuestions("topi", 1);

        assertNotEquals(Thread.currentThread().getName(), lettore.get());
        assertFalse(lettore.get() == null);
    }

    /**
     * L'errore del catalogo deve riemergere così com'è: l'interfaccia lo
     * intercetta per tipo, e un involucro dell'esecutore lo renderebbe
     * invisibile.
     */
    @Test
    void ilCatalogoRottoRiportaLEccezioneOriginale() {
        QuestionCatalogException atteso =
                new QuestionCatalogException("catalogo illeggibile");

        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            throw atteso;
        });

        QuestionCatalogException uscita = assertThrows(
                QuestionCatalogException.class,
                () -> catalogo.randomQuestions("topi", 1));

        assertEquals(atteso, uscita);
    }

    /** Un guasto qualsiasi diventa comunque un errore di catalogo. */
    @Test
    void unGuastoImprevistoDiventaUnErroreDiCatalogo() {
        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            throw new IllegalStateException("disco staccato");
        });

        QuestionCatalogException uscita = assertThrows(
                QuestionCatalogException.class,
                () -> catalogo.randomQuestions("topi", 1));

        assertTrue(uscita.getCause() instanceof IllegalStateException);
    }

    /**
     * Un fallimento non va memorizzato: chi corregge il file e riprova deve
     * ottenere una lettura nuova, non l'errore di prima.
     */
    @Test
    void dopoUnFallimentoIlTentativoSuccessivoRilegge() {
        AtomicInteger letture = new AtomicInteger();

        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            if (letture.incrementAndGet() == 1) {
                throw new QuestionCatalogException("primo tentativo fallito");
            }

            return new FakeQuestionRepository();
        });

        assertThrows(QuestionCatalogException.class,
                () -> catalogo.randomQuestions("topi", 1));

        assertEquals(1, catalogo.randomQuestions("topi", 1).size());
        assertEquals(2, letture.get());
    }

    /** Una lettura riuscita avviene una volta sola, non a ogni richiesta. */
    @Test
    void ilCatalogoLettoVieneRiusato() {
        AtomicInteger letture = new AtomicInteger();

        QuestionRepository catalogo = new BackgroundQuestionCatalog(() -> {
            letture.incrementAndGet();
            return new FakeQuestionRepository();
        });

        catalogo.randomQuestions("topi", 1);
        catalogo.randomQuestions("buio", 1);
        catalogo.randomQuestions("altezze", 1);

        assertEquals(1, letture.get());
    }

    @Test
    void laSorgenteEObbligatoria() {
        assertThrows(IllegalArgumentException.class,
                () -> new BackgroundQuestionCatalog(null));
    }
}
