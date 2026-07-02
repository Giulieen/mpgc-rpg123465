package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;

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

    public void executeCurrentFloorEvent() {
        getCurrentFloor().getEvent().execute(this);

        if (isOnLastFloor() && player.isAlive()) {
            gameCompleted = true;
        }
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
}