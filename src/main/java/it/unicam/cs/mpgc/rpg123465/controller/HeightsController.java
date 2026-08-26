package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.AltitudeCrossing;

/**
 * Controller del Piano III — Le Altezze.
 *
 * <p>
 * Tiene le conseguenze della traversata separate dalla rappresentazione
 * JavaFX. Gli errori sulle frecce restano interni alla scena, che li conta
 * come punti equilibrio: solo la caduta costa un tentativo.
 */
public final class HeightsController {

    private final GameController game;
    private final MindState mind;

    /**
     * @param crossing contenuto del piano
     * @param game controller della partita
     * @throws IllegalArgumentException se un parametro è null
     */
    public HeightsController(AltitudeCrossing crossing, GameController game) {
        if (crossing == null || game == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.game = game;
        this.mind = game.getMind();
    }

    /**
     * Registra una risposta a un dilemma "Preferiresti".
     *
     * Il tratto resta nascosto e non ha alcun effetto sulla traversata:
     * contribuisce soltanto al profilo finale.
     *
     * @param trait tratto associato alla risposta
     */
    public void registerChoice(ProfileTrait trait) {
        mind.registerTrait(trait);
    }

    /**
     * Consuma un tentativo per una caduta nel vuoto.
     *
     * È l'unica penalità del piano: i singoli errori sulle frecce tolgono
     * equilibrio, non tentativi.
     */
    public void registerFall() {
        game.loseAttempt();
    }

    /**
     * @return {@code true} se restano tentativi per una nuova traversata
     */
    public boolean canRetry() {
        return game.hasAttemptsLeft();
    }

    /**
     * Riporta i tentativi al massimo per ricominciare la prova da capo.
     */
    public void restartTrial() {
        game.resetAttempts();
    }

    public int remainingAttempts() {
        return game.getRemainingAttempts();
    }

    public int maxAttempts() {
        return game.getMaxAttempts();
    }
}
