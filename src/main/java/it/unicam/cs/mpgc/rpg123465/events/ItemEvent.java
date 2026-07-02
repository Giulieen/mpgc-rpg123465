package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;

/**
 * Evento in cui il giocatore ottiene un oggetto.
 */
public class ItemEvent implements FloorEvent {

    private final String title;
    private final String description;
    private final Item item;

    /**
     * Crea un nuovo evento di ottenimento oggetto.
     *
     * @param title titolo dell'evento
     * @param description descrizione dell'evento
     * @param item oggetto ottenuto
     */
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

    /**
     * Restituisce l'oggetto associato all'evento.
     *
     * @return oggetto ottenuto
     */
    public Item getItem() {
        return item;
    }

    /**
     * Aggiunge l'oggetto all'inventario del giocatore.
     *
     * @param gameEngine motore della partita
     */
    @Override
    public void execute(GameEngine gameEngine) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }

        gameEngine.getPlayer().addItem(item);
    }
}