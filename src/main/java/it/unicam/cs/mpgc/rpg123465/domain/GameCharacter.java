package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un personaggio generico del gioco.
 */
public abstract class GameCharacter {

    private final String name;
    private final Stats stats;

    /**
     * Crea un personaggio generico.
     *
     * @param name nome del personaggio
     * @param stats statistiche del personaggio
     */
    protected GameCharacter(String name, Stats stats) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        if (stats == null) {
            throw new IllegalArgumentException("Le statistiche non possono essere null.");
        }

        this.name = name;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public boolean isAlive() {
        return stats.isAlive();
    }

    public void takeDamage(int amount) {
        stats.takeDamage(amount);
    }

    public void heal(int amount) {
        stats.heal(amount);
    }

    @Override
    public String toString() {
        return name + " " + stats;
    }
}