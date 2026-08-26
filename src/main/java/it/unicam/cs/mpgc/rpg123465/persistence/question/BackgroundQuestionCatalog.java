package it.unicam.cs.mpgc.rpg123465.persistence.question;

import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionCatalogException;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Legge un catalogo di domande su un thread separato, mentre l'interfaccia
 * continua a rispondere.
 *
 * <p>
 * Il file JSON viene aperto e analizzato all'avvio dell'applicazione, quando
 * il giocatore è ancora sul menu: al momento della prima partita il lavoro è
 * quasi sempre già finito e la richiesta non attende. Se invece non lo fosse,
 * {@link #randomQuestions(String, int)} si blocca finché il risultato non
 * arriva — il comportamento resta identico a una lettura immediata, non si
 * perde nulla.
 *
 * <p>
 * Un fallimento non viene memorizzato: la richiesta successiva rilegge il
 * file. Serve a lasciare al giocatore la possibilità di correggere il
 * catalogo e riprovare senza riavviare il gioco.
 */
public final class BackgroundQuestionCatalog implements QuestionRepository {

    private final Callable<QuestionRepository> sorgente;

    private Future<QuestionRepository> caricamento;

    /**
     * Avvia subito la lettura in background.
     *
     * @param sorgente come costruire il catalogo, eseguito fuori dal thread
     *                 grafico
     */
    public BackgroundQuestionCatalog(Callable<QuestionRepository> sorgente) {
        if (sorgente == null) {
            throw new IllegalArgumentException("La sorgente del catalogo non può essere null.");
        }

        this.sorgente = sorgente;

        this.caricamento = avvia();
    }

    @Override
    public List<Dilemma> randomQuestions(String category, int count) {
        return catalogo().randomQuestions(category, count);
    }

    /**
     * Un solo thread, marcato come daemon perché la chiusura della finestra
     * non debba aspettare che finisca di leggere. Il pool viene chiuso subito
     * dopo l'invio: accetta quel compito e nient'altro.
     *
     * @return il risultato futuro della lettura
     */
    private Future<QuestionRepository> avvia() {
        ExecutorService esecutore = Executors.newSingleThreadExecutor(compito -> {
            Thread thread = new Thread(compito, "caricamento-catalogo");

            thread.setDaemon(true);

            return thread;
        });

        try {
            return esecutore.submit(sorgente);

        } finally {
            esecutore.shutdown();
        }
    }

    /**
     * @return il catalogo, attendendo la lettura se non è ancora finita
     * @throws QuestionCatalogException se il file non è utilizzabile
     */
    private QuestionRepository catalogo() {
        try {
            return caricamento.get();

        } catch (ExecutionException fallito) {
            // Il prossimo tentativo riparte da capo, come una lettura diretta.
            caricamento = avvia();

            throw riporta(fallito.getCause());

        } catch (InterruptedException interrotto) {
            // Chi ci ha interrotti deve poterlo sapere: il segnale va rimesso.
            Thread.currentThread().interrupt();

            throw new QuestionCatalogException(
                    "Lettura del catalogo interrotta.", interrotto);
        }
    }

    /**
     * Rimette a galla l'errore originale: chi ha chiamato si aspetta il
     * problema del catalogo, non l'involucro tecnico dell'esecutore.
     *
     * @param causa l'eccezione sollevata durante la lettura
     * @return l'eccezione da propagare
     */
    private QuestionCatalogException riporta(Throwable causa) {
        if (causa instanceof QuestionCatalogException errore) {
            return errore;
        }

        return new QuestionCatalogException(
                "Il catalogo delle domande non è utilizzabile.", causa);
    }
}
