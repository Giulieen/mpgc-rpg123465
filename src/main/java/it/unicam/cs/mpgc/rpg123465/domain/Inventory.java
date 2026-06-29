package it.unicam.cs.mpgc.rpg123465.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta l'inventario del giocatore.
 */
public class Inventory {

    private final List<Item> items;

    /**
     * Crea un inventario vuoto.
     */
    public Inventory() {
        this.items = new ArrayList<>();
    }

    /**
     * Aggiunge un oggetto all'inventario.
     *
     * @param item oggetto da aggiungere
     */
    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("L'oggetto non può essere null.");
        }

        items.add(item);
    }

    /**
     * Rimuove un oggetto dall'inventario.
     *
     * @param item oggetto da rimuovere
     * @return {@code true} se l'oggetto è stato rimosso,
     *         {@code false} altrimenti
     */
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    /**
     * Restituisce gli oggetti presenti nell'inventario.
     *
     * @return lista non modificabile degli oggetti
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Verifica se l'inventario è vuoto.
     *
     * @return {@code true} se non contiene oggetti
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Restituisce il numero di oggetti presenti.
     *
     * @return numero di oggetti
     */
    public int size() {
        return items.size();
    }

    @Override
    public String toString() {
        return items.toString();
    }
}