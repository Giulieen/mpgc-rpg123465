package it.unicam.cs.mpgc.rpg123465.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta la torre esplorata dal giocatore.
 */
public class Tower {

    private final List<Floor> floors;

    /**
     * Crea una torre a partire da una lista di piani.
     *
     * @param floors piani della torre
     */
    public Tower(List<Floor> floors) {
        if (floors == null || floors.isEmpty()) {
            throw new IllegalArgumentException("La torre deve contenere almeno un piano.");
        }

        this.floors = new ArrayList<>(floors);
    }

    /** @return lista non modificabile dei piani */
    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public int getTotalFloors() {
        return floors.size();
    }

    /**
     * @param index indice del piano, a partire da zero
     * @return piano corrispondente
     * @throws IllegalArgumentException se l'indice è fuori dalla Torre
     */
    public Floor getFloor(int index) {
        if (index < 0 || index >= floors.size()) {
            throw new IllegalArgumentException("Indice del piano non valido.");
        }

        return floors.get(index);
    }
}