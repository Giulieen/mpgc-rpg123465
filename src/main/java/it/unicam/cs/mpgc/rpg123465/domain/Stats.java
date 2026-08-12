package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Le statistiche fisiche di un'entità del gioco: quanta vita ha, e quanta ne
 * resta.
 * <p>
 * Nella Torre non si combatte: le paure si affrontano con l'atteggiamento, non
 * con l'attacco. Ciò che il corpo può ancora sopportare vive qui; ciò che la
 * mente può ancora sopportare vive nel {@link MindState}.
 */
public class Stats {

    private final int maxHealth;
    private int currentHealth;

    /**
     * Crea un nuovo insieme di statistiche, con la vita al massimo.
     *
     * @param maxHealth punti vita massimi
     * @throws IllegalArgumentException se la vita massima non è positiva
     */
    public Stats(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("La vita massima deve essere positiva.");
        }

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
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
     * Verifica se l'entità è ancora in vita.
     *
     * @return {@code true} se i punti vita sono maggiori di zero,
     * {@code false} altrimenti
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }

    /**
     * Applica un danno ai punti vita, senza mai scendere sotto lo zero.
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
     * Ripristina una quantità di punti vita, senza mai superare il massimo.
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
     * Imposta i punti vita correnti a un valore specifico: serve a rimettere in
     * piedi il giocatore al checkpoint, o a ricaricare una partita salvata.
     *
     * @param value nuovo valore dei punti vita
     * @throws IllegalArgumentException se il valore è negativo
     * o superiore alla vita massima
     */
    public void setCurrentHealth(int value) {
        if (value < 0 || value > maxHealth) {
            throw new IllegalArgumentException(
                    "I punti vita devono essere compresi tra 0 e la vita massima.");
        }

        this.currentHealth = value;
    }

    @Override
    public String toString() {
        return "Stats{currentHealth=" + currentHealth + ", maxHealth=" + maxHealth + '}';
    }
}
