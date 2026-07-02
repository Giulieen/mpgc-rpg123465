package it.unicam.cs.mpgc.rpg123465.domain;

import it.unicam.cs.mpgc.rpg123465.events.FloorEvent;

/**
 * Rappresenta un piano della torre.
 */
public class Floor {

    private final int number;
    private final String name;
    private final String description;
    private final FloorEvent event;

    public Floor(int number, String name, String description, FloorEvent event) {
        if (number <= 0) {
            throw new IllegalArgumentException("Il numero del piano deve essere positivo.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome del piano non può essere vuoto.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione del piano non può essere vuota.");
        }
        if (event == null) {
            throw new IllegalArgumentException("L'evento del piano non può essere null.");
        }

        this.number = number;
        this.name = name;
        this.description = description;
        this.event = event;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public FloorEvent getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "Piano " + number + " - " + name;
    }
}