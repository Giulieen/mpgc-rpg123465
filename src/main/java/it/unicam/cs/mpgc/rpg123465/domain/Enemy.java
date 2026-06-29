package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un nemico affrontabile dal giocatore.
 */
public class Enemy extends GameCharacter {

    private final String description;

    /**
     * Crea un nuovo nemico.
     *
     * @param name nome del nemico
     * @param stats statistiche del nemico
     * @param description descrizione del nemico
     */
    public Enemy(String name, Stats stats, String description) {
        super(name, stats);

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }

        this.description = description;
    }

    /**
     * Restituisce la descrizione del nemico.
     *
     * @return descrizione del nemico
     */
    public String getDescription() {
        return description;
    }
}