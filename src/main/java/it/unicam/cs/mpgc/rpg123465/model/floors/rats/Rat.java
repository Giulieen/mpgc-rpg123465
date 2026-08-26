package it.unicam.cs.mpgc.rpg123465.model.floors.rats;

import java.util.List;

/**
 * Un topo che attraversa la stanza per raggiungere un'uscita.
 *
 * <p>
 * Il percorso viene calcolato una volta sola, alla nascita, e poi seguito passo
 * per passo. Il topo non sa dove sia il giocatore e non reagisce: è il
 * giocatore a doverlo intercettare, non il contrario.
 *
 * <p>
 * Non conosce JavaFX.
 */
public final class Rat {

    private final List<GridPosition> path;
    private final GridPosition targetExit;

    private int pathIndex;

    /**
     * @param path percorso dalla tana all'uscita, tana inclusa
     * @throws IllegalArgumentException se il percorso è vuoto
     */
    public Rat(List<GridPosition> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Un topo deve avere un percorso.");
        }

        this.path = List.copyOf(path);
        this.targetExit = this.path.get(this.path.size() - 1);
    }

    /**
     * @return la cella occupata in questo momento
     */
    public GridPosition position() {
        return path.get(pathIndex);
    }

    /**
     * @return l'uscita verso cui è diretto
     */
    public GridPosition targetExit() {
        return targetExit;
    }

    /**
     * Avanza di una cella lungo il percorso.
     *
     * Arrivato in fondo resta fermo: a quel punto è sull'uscita e il livello
     * lo toglie di mezzo.
     */
    public void advance() {
        if (pathIndex < path.size() - 1) {
            pathIndex++;
        }
    }

    /**
     * @return {@code true} se ha raggiunto l'uscita
     */
    public boolean hasEscaped() {
        return position().equals(targetExit);
    }
}
