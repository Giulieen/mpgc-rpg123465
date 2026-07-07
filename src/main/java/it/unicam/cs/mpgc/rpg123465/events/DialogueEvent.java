package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evento narrativo con una o più scelte disponibili.
 */
public class DialogueEvent implements FloorEvent {

    private final String title;
    private final String description;
    private final List<String> choices;

    /**
     * Crea un nuovo evento di dialogo.
     *
     * @param title titolo dell'evento
     * @param description descrizione dell'evento
     * @param choices scelte disponibili
     */
    public DialogueEvent(String title, String description, List<String> choices) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione non può essere vuota.");
        }
        if (choices == null || choices.isEmpty()) {
            throw new IllegalArgumentException("Il dialogo deve contenere almeno una scelta.");
        }

        this.title = title;
        this.description = description;
        this.choices = new ArrayList<>(choices);
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
     * Restituisce le scelte disponibili.
     *
     * @return lista non modificabile delle scelte
     */
    public List<String> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    /**
     * Esegue l'evento di dialogo.
     * L'implementazione sarà completata quando verrà introdotto
     * il sistema di dialoghi interattivi.
     *
     * @param gameEngine motore della partita
     */
    @Override
    public EventResult execute(GameEngine gameEngine) {
    if (gameEngine == null) {
        throw new IllegalArgumentException("Il motore di gioco non può essere null.");
    }

    return new EventResult("Hai riflettuto sulle scelte disponibili.");
    }
}