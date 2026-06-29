package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta il protagonista controllato dal giocatore.
 */
public class Player extends GameCharacter {

    private final Inventory inventory;

    /**
     * Crea un nuovo giocatore.
     *
     * @param name nome del giocatore
     * @param stats statistiche del giocatore
     * @param inventory inventario del giocatore
     */
    public Player(String name, Stats stats, Inventory inventory) {
        super(name, stats);

        if (inventory == null) {
            throw new IllegalArgumentException("L'inventario non può essere null.");
        }

        this.inventory = inventory;
    }

    /**
     * Restituisce l'inventario del giocatore.
     *
     * @return inventario del giocatore
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Aggiunge un oggetto all'inventario del giocatore.
     *
     * @param item oggetto da aggiungere
     */
    public void addItem(Item item) {
        inventory.addItem(item);
    }
}