package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un personaggio generico del gioco.
 */
public abstract class GameCharacter {

    private final String name;

    /**
     * Crea un personaggio generico.
     *
     * @param name nome del personaggio
     * @throws IllegalArgumentException se il nome è vuoto
     */
    protected GameCharacter(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
