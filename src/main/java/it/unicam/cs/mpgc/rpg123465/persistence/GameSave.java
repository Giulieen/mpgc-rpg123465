package it.unicam.cs.mpgc.rpg123465.persistence;

import java.io.Serializable;

/**
 * Rappresenta i dati di una partita salvata.
 */
public class GameSave implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final int currentFloor;
    private final int currentHealth;

    /**
     * Crea un nuovo salvataggio.
     *
     * @param playerName nome del giocatore
     * @param currentFloor piano corrente
     * @param currentHealth punti vita correnti
     */
    public GameSave(String playerName, int currentFloor, int currentHealth) {
        this.playerName = playerName;
        this.currentFloor = currentFloor;
        this.currentHealth = currentHealth;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }
}
