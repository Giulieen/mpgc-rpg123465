package it.unicam.cs.mpgc.rpg123465.events;

/**
 * Rappresenta il risultato dell'esecuzione di un evento.
 */
public class EventResult {

    private final String message;

    /**
     * Crea un nuovo risultato di un evento.
     *
     * @param message messaggio da mostrare al giocatore
     */
    public EventResult(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Il messaggio non può essere vuoto.");
        }

        this.message = message;
    }

    /**
     * Restituisce il messaggio associato al risultato.
     *
     * @return messaggio dell'evento
     */
    public String getMessage() {
        return message;
    }
}