package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;

/**
 * Evento in cui il giocatore affronta un nemico.
 */
public class CombatEvent implements FloorEvent {

    private final String title;
    private final String description;
    private final Enemy enemy;

    /**
     * Crea un nuovo evento di combattimento.
     *
     * @param title titolo dell'evento
     * @param description descrizione dell'evento
     * @param enemy nemico da affrontare
     */
    public CombatEvent(String title, String description, Enemy enemy) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }
        if (enemy == null) {
            throw new IllegalArgumentException("Il nemico non può essere null.");
        }

        this.title = title;
        this.description = description;
        this.enemy = enemy;
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
     * Restituisce il nemico associato all'evento.
     *
     * @return il nemico
     */
    public Enemy getEnemy() {
        return enemy;
    }

    /**
     * Avvia il combattimento senza risolverlo automaticamente.
     *
     * @param gameEngine motore della partita
     * @return risultato descrittivo dell'avvio del combattimento
     */
    @Override
    public EventResult execute(GameEngine gameEngine) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }

        return new EventResult(
                "Il combattimento contro " + enemy.getName() + " ha inizio."
        );
    }
}