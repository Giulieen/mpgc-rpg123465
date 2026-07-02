package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;

/**
 * Evento in cui il giocatore affronta un nemico.
 */
public class CombatEvent implements FloorEvent {

    private final String title;
    private final String description;
    private final Enemy enemy;

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

    public Enemy getEnemy() {
        return enemy;
    }
}