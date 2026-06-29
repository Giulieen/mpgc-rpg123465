package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta le statistiche principali di un'entità del gioco.
 */
public class Stats {

    private final int maxHealth;
    private int currentHealth;
    private final int attack;
    private final int defense;

    /**
     * Crea un nuovo insieme di statistiche.
     *
     * @param maxHealth punti vita massimi
     * @param attack valore di attacco
     * @param defense valore di difesa
     * @throws IllegalArgumentException se uno dei parametri non è valido
     */
    public Stats(int maxHealth, int attack, int defense) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("La vita massima deve essere positiva.");
        }
        if (attack < 0) {
            throw new IllegalArgumentException("L'attacco non può essere negativo.");
        }
        if (defense < 0) {
            throw new IllegalArgumentException("La difesa non può essere negativa.");
        }

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.attack = attack;
        this.defense = defense;
    }

    /**
     * Restituisce i punti vita massimi.
     *
     * @return i punti vita massimi
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Restituisce i punti vita attuali.
     *
     * @return i punti vita attuali
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Restituisce il valore di attacco.
     *
     * @return il valore di attacco
     */
    public int getAttack() {
        return attack;
    }

    /**
     * Restituisce il valore di difesa.
     *
     * @return il valore di difesa
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Verifica se l'entità è ancora in vita.
     *
     * @return {@code true} se i punti vita sono maggiori di zero,
     * {@code false} altrimenti
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }

    /**
     * Applica un danno ai punti vita.
     *
     * @param amount quantità di danno da infliggere
     * @throws IllegalArgumentException se il danno è negativo
     */
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Il danno non può essere negativo.");
        }

        currentHealth = Math.max(0, currentHealth - amount);
    }

    /**
     * Ripristina una quantità di punti vita.
     *
     * @param amount quantità di cura
     * @throws IllegalArgumentException se la cura è negativa
     */
    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La cura non può essere negativa.");
        }

        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    /**
     * Ripristina completamente i punti vita.
     */
    public void restoreHealth() {
        currentHealth = maxHealth;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "maxHealth=" + maxHealth +
                ", currentHealth=" + currentHealth +
                ", attack=" + attack +
                ", defense=" + defense +
                '}';
    }
}