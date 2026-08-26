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
 * Il file viene aperto e analizzato all'avvio, quando il giocatore è ancora
 * sul menu: alla prima partita il lavoro è quasi sempre già finito e la
 * richiesta non attende. Se non lo fosse, attende: il comportamento resta
 * quello di una lettura immediata, non si perde nulla.
 *
 * <p>
 * Un fallimento non viene memorizzato, così chi corregge il file può
 * riprovare senza riavviare il gioco.
 */
public final class BackgroundQuestionCatalog implements QuestionRepository {

    private final Callable<QuestionRepository> source;

    private Future<QuestionRepository> loading;

    /**
     * Avvia subito la lettura in background.
     *
     * @param source come costruire il catalogo, eseguito fuori dal thread
     *               grafico
     */
    public BackgroundQuestionCatalog(Callable<QuestionRepository> source) {
        if (source == null) {
            throw new IllegalArgumentException("La sorgente del catalogo non può essere null.");
        }

        this.source = source;

        this.loading = start();
    }

    @Override
    public List<Dilemma> randomQuestions(String category, int count) {
        return catalog().randomQuestions(category, count);
    }

    /**
     * Il thread è daemon perché chiudere la finestra non debba aspettare la
     * fine della lettura. Il pool viene chiuso subito dopo l'invio: accetta
     * quel compito e nient'altro.
     *
     * @return il risultato futuro della lettura
     */
    private Future<QuestionRepository> start() {
        ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "question-catalog-loader");

            thread.setDaemon(true);

            return thread;
        });

        try {
            return executor.submit(source);

        } finally {
            executor.shutdown();
        }
    }

    /**
     * @return il catalogo, attendendo la lettura se non è ancora finita
     * @throws QuestionCatalogException se il file non è utilizzabile
     */
    private QuestionRepository catalog() {
        try {
            return loading.get();

        } catch (ExecutionException failure) {
            // Il prossimo tentativo riparte da capo, come una lettura diretta.
            loading = start();

            throw asCatalogFailure(failure.getCause());

        } catch (InterruptedException interruption) {
            // Chi ci ha interrotti deve poterlo sapere: il segnale va rimesso.
            Thread.currentThread().interrupt();

            throw new QuestionCatalogException(
                    "Lettura del catalogo interrotta.", interruption);
        }
    }

    /**
     * Rimette a galla l'errore originale: chi ha chiamato si aspetta il
     * problema del catalogo, non l'involucro tecnico dell'esecutore.
     *
     * @param cause l'eccezione sollevata durante la lettura
     * @return l'eccezione da propagare
     */
    private QuestionCatalogException asCatalogFailure(Throwable cause) {
        if (cause instanceof QuestionCatalogException failure) {
            return failure;
        }

        return new QuestionCatalogException(
                "Il catalogo delle domande non è utilizzabile.", cause);
    }
}
