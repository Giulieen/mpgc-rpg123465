package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un oggetto presente nel gioco.
 */
public class Item {

    private final String name;
    private final String description;

    /**
     * Crea un nuovo oggetto.
     *
     * @param name nome dell'oggetto
     * @param description descrizione dell'oggetto
     */
    public Item(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }

        this.name = name;
        this.description = description;
    }

    /**
     * Restituisce il nome dell'oggetto.
     *
     * @return il nome
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce la descrizione dell'oggetto.
     *
     * @return la descrizione
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}