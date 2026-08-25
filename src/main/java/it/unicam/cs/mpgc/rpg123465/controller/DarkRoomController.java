package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.model.floors.buio.DarkRoom;

/**
 * Controller del Piano II — Il Buio.
 *
 * <p>
 * Tiene le regole del puzzle separate dalla rappresentazione JavaFX. Le regole
 * sono due: la combinazione apre la porta, e ogni errore — cifre sbagliate o
 * tempo scaduto — costa un tentativo.
 */
public final class DarkRoomController {

    private final DarkRoom room;
    private final GameController game;
    private final MindState mind;

    /**
     * @param room contenuto del piano
     * @param game controller della partita
     */
    public DarkRoomController(DarkRoom room, GameController game) {
        if (room == null || game == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.room = room;
        this.game = game;
        this.mind = game.getMind();
    }

    /**
     * Verifica la combinazione.
     */
    public boolean opens(String enteredCode) {
        return room.combination()
                .equals(enteredCode);
    }

    /**
     * Registra la risposta a un dilemma del piano.
     *
     * Il tratto è nascosto al giocatore e non ha alcun effetto sulla prova:
     * contribuisce soltanto al profilo finale.
     */
    public void registerChoice(ProfileTrait trait) {
        mind.registerTrait(trait);
    }

    /**
     * Consuma un tentativo per una combinazione errata o per il tempo scaduto.
     */
    public void registerFailedAttempt() {
        game.loseAttempt();
    }

    /**
     * @return {@code true} se restano tentativi per riprovare la serratura
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
