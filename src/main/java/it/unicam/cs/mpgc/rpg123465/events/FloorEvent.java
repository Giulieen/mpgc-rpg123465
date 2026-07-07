package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;

/**
 * Rappresenta un evento presente in un piano della torre.
 */
public interface FloorEvent {

    String getTitle();

    String getDescription();

    /**
     * Esegue l'evento modificando lo stato della partita.
     *
     * @param gameEngine motore della partita corrente
     * @return risultato dell'evento
     */
    EventResult execute(GameEngine gameEngine);
}