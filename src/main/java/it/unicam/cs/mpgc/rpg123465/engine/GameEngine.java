package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.events.EventResult;

/**
 * Coordina lo stato principale della partita.
 */
public class GameEngine {

    private final Player player;
    private final Tower tower;
    private int currentFloorIndex;
    private boolean gameCompleted;

    public GameEngine(Player player, Tower tower) {
        if (player == null) {
            throw new IllegalArgumentException("Il giocatore non può essere null.");
        }
        if (tower == null) {
            throw new IllegalArgumentException("La torre non può essere null.");
        }

        this.player = player;
        this.tower = tower;
        this.currentFloorIndex = 0;
        this.gameCompleted = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Tower getTower() {
        return tower;
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    public Floor getCurrentFloor() {
        return tower.getFloor(currentFloorIndex);
    }

    /**
     * Esegue l'evento associato al piano corrente.
     *
     * @return risultato dell'evento
     */
    public EventResult executeCurrentFloorEvent() {
        EventResult result = getCurrentFloor().getEvent().execute(this);

        if (isOnLastFloor() && player.isAlive()) {
            gameCompleted = true;
        }

        return result;
    }

    public boolean isOnLastFloor() {
        return currentFloorIndex == tower.getTotalFloors() - 1;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public void advanceFloor() {
        if (gameCompleted) {
            throw new IllegalStateException("La partita è già completata.");
        }
        if (isOnLastFloor()) {
            throw new IllegalStateException("Il giocatore si trova già all'ultimo piano.");
        }

        currentFloorIndex++;
    }

    /**
     * Ripristina lo stato principale della partita.
     *
     * @param currentFloorIndex indice del piano corrente
     * @param gameCompleted indica se la partita è completata
     */
    public void restoreState(int currentFloorIndex, boolean gameCompleted) {
        if (currentFloorIndex < 0 || currentFloorIndex >= tower.getTotalFloors()) {
            throw new IllegalArgumentException("Indice del piano non valido.");
        }

        this.currentFloorIndex = currentFloorIndex;
        this.gameCompleted = gameCompleted;
    }
}