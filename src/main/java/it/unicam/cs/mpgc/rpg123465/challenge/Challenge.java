package it.unicam.cs.mpgc.rpg123465.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta una prova narrativa associata a un piano della torre.
 */
public class Challenge {

    private final String name;
    private final List<ChallengeQuestion> questions;
    private int currentIndex;

    public Challenge(String name, List<ChallengeQuestion> questions) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome della prova non può essere vuoto.");
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("La prova deve contenere almeno una domanda.");
        }

        this.name = name;
        this.questions = new ArrayList<>(questions);
        Collections.shuffle(this.questions);
        this.currentIndex = 0;
    }

    public String getName() {
        return name;
    }

    public ChallengeQuestion nextQuestion() {
        if (currentIndex >= questions.size()) {
            currentIndex = 0;
            Collections.shuffle(questions);
        }

        return questions.get(currentIndex++);
    }
}