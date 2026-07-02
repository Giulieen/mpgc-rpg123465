package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un piano della torre.
 */
public class Floor {

    private final int number;
    private final String name;
    private final String description;

    /**
     * Crea un nuovo piano della torre.
     *
     * @param number numero del piano
     * @param name nome del piano
     * @param description descrizione del piano
     */
    public Floor(int number, String name, String description) {
        if (number <= 0) {
            throw new IllegalArgumentException("Il numero del piano deve essere positivo.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome del piano non può essere vuoto.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione del piano non può essere vuota.");
        }

        this.number = number;
        this.name = name;
        this.description = description;
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

    @Override
    public String toString() {
        return "Piano " + number + " - " + name;
    }
}