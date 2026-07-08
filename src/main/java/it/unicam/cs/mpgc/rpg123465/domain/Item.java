package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un oggetto presente nel gioco.
 */
public class Item {

    private final String name;
    private final String description;
    private final ItemType type;
    private final int healingPower;

    /**
     * Crea un nuovo oggetto privo di potere curativo.
     *
     * @param name nome dell'oggetto
     * @param description descrizione dell'oggetto
     * @param type tipo dell'oggetto
     */
    public Item(String name, String description, ItemType type) {
        this(name, description, type, 0);
    }

    /**
     * Crea un nuovo oggetto.
     *
     * @param name nome dell'oggetto
     * @param description descrizione dell'oggetto
     * @param type tipo dell'oggetto
     * @param healingPower quantità di vita ripristinata quando l'oggetto viene
     * usato (0 se l'oggetto non è curativo)
     */
    public Item(String name, String description, ItemType type, int healingPower) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Il tipo dell'oggetto non può essere null.");
        }

        if (healingPower < 0) {
            throw new IllegalArgumentException("La potenza curativa non può essere negativa.");
        }

        this.name = name;
        this.description = description;
        this.type = type;
        this.healingPower = healingPower;
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

    /**
     * Restituisce la quantità di vita ripristinata dall'oggetto.
     *
     * @return la potenza curativa (0 se l'oggetto non è curativo)
     */
    public int getHealingPower() {
        return healingPower;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}