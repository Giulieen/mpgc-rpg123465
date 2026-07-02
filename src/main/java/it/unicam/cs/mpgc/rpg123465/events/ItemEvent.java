package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Item;

/**
 * Evento in cui il giocatore ottiene un oggetto.
 */
public class ItemEvent implements FloorEvent {

    private final String title;
    private final String description;
    private final Item item;

    public ItemEvent(String title, String description, Item item) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }
        if (item == null) {
            throw new IllegalArgumentException("L'oggetto non può essere null.");
        }

        this.title = title;
        this.description = description;
        this.item = item;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Item getItem() {
        return item;
    }
}