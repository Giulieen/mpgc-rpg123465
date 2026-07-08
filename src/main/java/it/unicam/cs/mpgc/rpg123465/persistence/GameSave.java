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
    private final boolean gameCompleted;

    /**
     * Crea un nuovo salvataggio.
     *
     * @param playerName nome del giocatore
     * @param currentFloor piano corrente
     * @param currentHealth punti vita correnti
     * @param gameCompleted indica se la partita è stata completata
     */
    public GameSave(String playerName,
                    int currentFloor,
                    int currentHealth,
                    boolean gameCompleted) {

        this.playerName = playerName;
        this.currentFloor = currentFloor;
        this.currentHealth = currentHealth;
        this.gameCompleted = gameCompleted;
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

    public boolean isGameCompleted() {
        return gameCompleted;
    }
}