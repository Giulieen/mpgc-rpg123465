package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un oggetto presente nel gioco.
 */
public class Item {

    private final String name;
    private final String description;
    private final ItemType type;

    /**
     * Crea un nuovo oggetto.
     *
     * @param name nome dell'oggetto
     * @param description descrizione dell'oggetto
     * @param type tipo dell'oggetto
     */
    public Item(String name, String description, ItemType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Il tipo dell'oggetto non può essere null.");
        }

        this.name = name;
        this.description = description;
        this.type = type;
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

    /**
     * Restituisce il tipo dell'oggetto.
     *
     * @return il tipo dell'oggetto
     */
    public ItemType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}