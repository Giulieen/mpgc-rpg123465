package it.unicam.cs.mpgc.rpg123465.events;

/**
 * Rappresenta un evento presente in un piano della torre.
 */
public interface FloorEvent {

    /**
     * Restituisce il titolo dell'evento.
     *
     * @return titolo dell'evento
     */
    String getTitle();

    /**
     * Restituisce la descrizione narrativa dell'evento.
     *
     * @return descrizione dell'evento
     */
    String getDescription();
}