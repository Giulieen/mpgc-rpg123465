package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;

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

    /**
     * Esegue l'evento modificando lo stato della partita.
     *
     * @param gameEngine motore della partita corrente
     */
    void execute(GameEngine gameEngine);
}