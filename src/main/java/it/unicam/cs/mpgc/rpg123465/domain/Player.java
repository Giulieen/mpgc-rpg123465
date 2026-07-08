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

    /**
     * Verifica se il giocatore possiede almeno un oggetto curativo.
     *
     * @return {@code true} se è presente un oggetto di tipo curativo
     */
    public boolean hasHealingItem() {
        return findHealingItem() != null;
    }

    /**
     * Usa il primo oggetto curativo disponibile: ripristina la vita del
     * giocatore in base alla potenza dell'oggetto e lo rimuove dall'inventario.
     *
     * @return l'oggetto utilizzato, oppure {@code null} se non ne era disponibile alcuno
     */
    public Item useHealingItem() {
        Item healingItem = findHealingItem();

        if (healingItem == null) {
            return null;
        }

        heal(healingItem.getHealingPower());
        inventory.removeItem(healingItem);

        return healingItem;
    }

    private Item findHealingItem() {
        for (Item item : inventory.getItems()) {
            if (item.getType() == ItemType.HEALING) {
                return item;
            }
        }

        return null;
    }
}