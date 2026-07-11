package it.unicam.cs.mpgc.rpg123465.persistence;

import it.unicam.cs.mpgc.rpg123465.domain.MindState;

import java.io.Serializable;

/**
 * Rappresenta i dati di una partita salvata.
 */
public class GameSave implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String playerName;
    private final int currentFloor;
    private final int currentHealth;
    private final boolean gameCompleted;

    /**
     * Stato interiore del giocatore: Lucidità, Stress e la memoria di come ha
     * reagito alle paure. Senza questo, ricaricando una partita si perderebbe
     * il percorso da cui nasce l'alter ego finale.
     */
    private final MindState mindState;

    /**
     * Crea un nuovo salvataggio.
     *
     * @param playerName nome del giocatore
     * @param currentFloor piano corrente
     * @param currentHealth punti vita correnti
     * @param gameCompleted indica se la partita è stata completata
     * @param mindState stato interiore del giocatore
     */
    public GameSave(String playerName,
                    int currentFloor,
                    int currentHealth,
                    boolean gameCompleted,
                    MindState mindState) {

        this.playerName = playerName;
        this.currentFloor = currentFloor;
        this.currentHealth = currentHealth;
        this.gameCompleted = gameCompleted;
        this.mindState = mindState;
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

    /**
     * @return lo stato interiore salvato, oppure {@code null} se il
     *         salvataggio proviene da una versione precedente del gioco
     */
    public MindState getMindState() {
        return mindState;
    }
}
