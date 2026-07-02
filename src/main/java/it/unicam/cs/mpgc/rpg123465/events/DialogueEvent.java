package it.unicam.cs.mpgc.rpg123465.events;

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

    public List<String> getChoices() {
        return Collections.unmodifiableList(choices);
    }
}