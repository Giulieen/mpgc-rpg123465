package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;

/**
 * Coordina la salita della Torre: a che piano si trova il giocatore e quando il
 * cammino è concluso.
 * <p>
 * Non sa nulla di come le paure vengano affrontate: quella è materia del
 * contenuto di ciascun piano. Qui si tiene soltanto il filo del percorso.
 */
public class GameEngine {

    private final Player player;
    private final Tower tower;
    private int currentFloorIndex;
    private boolean gameCompleted;

    /**
     * Crea un nuovo motore di gioco.
     *
     * @param player giocatore
     * @param tower torre da salire
     * @throws IllegalArgumentException se un parametro è null
     */
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

    /**
     * Restituisce il giocatore.
     *
     * @return giocatore
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Restituisce la torre.
     *
     * @return torre
     */
    public Tower getTower() {
        return tower;
    }

    /**
     * Restituisce l'indice del piano corrente.
     *
     * @return indice del piano corrente, a partire da zero
     */
    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    /**
     * Restituisce il piano su cui si trova il giocatore.
     *
     * @return piano corrente
     */
    public Floor getCurrentFloor() {
        return tower.getFloor(currentFloorIndex);
    }

    /**
     * Verifica se il giocatore è sull'ultimo piano della Torre.
     *
     * @return {@code true} se non ci sono altri piani sopra
     */
    public boolean isOnLastFloor() {
        return currentFloorIndex == tower.getTotalFloors() - 1;
    }

    /**
     * Verifica se la Torre è stata salita fino in cima.
     *
     * @return {@code true} se la partita è conclusa
     */
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Sale al piano successivo, una volta affrontato quello corrente. Se il
     * piano era l'ultimo, la Torre è conclusa.
     */
    public void climb() {
        if (gameCompleted) {
            return;
        }

        if (isOnLastFloor()) {
            gameCompleted = true;
        } else {
            currentFloorIndex++;
        }
    }

    /**
     * Ripristina il punto della salita raggiunto in una partita salvata.
     *
     * @param currentFloorIndex indice del piano corrente
     * @param gameCompleted indica se la partita era già conclusa
     * @throws IllegalArgumentException se l'indice del piano non è valido
     */
    public void restoreState(int currentFloorIndex, boolean gameCompleted) {
        if (currentFloorIndex < 0 || currentFloorIndex >= tower.getTotalFloors()) {
            throw new IllegalArgumentException("Indice del piano non valido.");
        }

        this.currentFloorIndex = currentFloorIndex;
        this.gameCompleted = gameCompleted;
    }
}
