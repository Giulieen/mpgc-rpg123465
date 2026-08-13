package it.unicam.cs.mpgc.rpg123465.questions;

/**
 * Punto unico di accesso al catalogo predefinito delle domande.
 *
 * <p>
 * Il catalogo viene letto alla prima richiesta e non in un inizializzatore
 * statico. La differenza conta quando il file è rotto: un errore in un
 * inizializzatore statico diventa {@code ExceptionInInitializerError} e marca
 * la classe come inutilizzabile per tutta la vita della JVM, cosicché ogni
 * accesso successivo fallisce con {@code NoClassDefFoundError} — un messaggio
 * che non dice nulla e che sopravvive anche dopo aver corretto il file.
 *
 * <p>
 * Costruendolo dentro {@link #repository()}, invece, l'errore risale come
 * {@link QuestionCatalogException} normale: chi avvia la partita può
 * intercettarlo e mostrarlo, e un tentativo successivo riprova davvero a
 * leggere il file. Il risultato viene memorizzato solo in caso di successo,
 * così un guasto non resta impresso.
 */
public final class Questions {

    private static QuestionRepository defaultRepository;

    private Questions() {
        // Solo metodi statici.
    }

    /**
     * Restituisce il catalogo predefinito, leggendolo alla prima richiesta.
     *
     * @return repository condiviso delle domande
     * @throws QuestionCatalogException se il catalogo è assente, illeggibile,
     *         vuoto o non valido
     */
    public static synchronized QuestionRepository repository() {
        if (defaultRepository == null) {
            /*
             * L'assegnazione avviene solo se il costruttore va a buon fine:
             * se lancia, il campo resta null e la chiamata successiva riprova.
             */
            defaultRepository = new JsonQuestionRepository();
        }

        return defaultRepository;
    }
}
