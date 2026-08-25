package it.unicam.cs.mpgc.rpg123465.controller;

/**
 * Rappresenta l'esito di un'operazione richiesta al controller.
 *
 * @param success true se l'operazione è terminata correttamente
 * @param detail dettaglio tecnico dell'eventuale errore
 */
public record OperationResult(boolean success, String detail) {

    /**
     * Crea un esito positivo.
     *
     * @return risultato positivo
     */
    public static OperationResult ok() {
        return new OperationResult(true, null);
    }

    /**
     * Crea un esito negativo.
     *
     * @param detail dettaglio dell'errore
     * @return risultato negativo
     */
    public static OperationResult failure(String detail) {
        return new OperationResult(false, detail);
    }
}
