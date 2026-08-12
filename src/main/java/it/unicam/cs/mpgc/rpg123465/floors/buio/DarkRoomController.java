package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;

/**
 * Controller del Piano II — Il Buio.
 *
 * Tiene le regole del puzzle e le conseguenze sul giocatore separate
 * dalla rappresentazione JavaFX.
 */
public final class DarkRoomController {

    private final DarkRoom room;
    private final GameController game;
    private final MindState mind;
    private final DarkRoomOutcomeHandler outcomes;

    private int checkpointLucidita;
    private int checkpointStress;
    private int checkpointVita;

    /**
     * @param room contenuto del piano
     * @param game controller della partita
     */
    public DarkRoomController(
            DarkRoom room,
            GameController game
    ) {
        if (room == null || game == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.room = room;
        this.game = game;
        this.mind = game.getMind();

        this.outcomes =
                new DarkRoomOutcomeHandler(
                        mind
                );
    }

    /**
     * Entra nella stanza e fissa il checkpoint.
     */
    public void enter() {
        mind.enterRoom(
                room.initialStress()
        );

        checkpointLucidita =
                mind.getLucidita();

        checkpointStress =
                mind.getStress();

        checkpointVita =
                game.getPlayerCurrentHealth();
    }

    /**
     * Verifica la combinazione.
     */
    public boolean opens(
            String enteredCode
    ) {
        return room.combination()
                .equals(
                        enteredCode
                );
    }

    /**
     * Registra la risposta a un dilemma del piano.
     *
     * Il tratto è nascosto al giocatore.
     */
    public void registerChoice(
            ProfileTrait trait
    ) {
        mind.registerTrait(
                trait
        );
    }

    public void registerSuccess() {
        outcomes.success();
    }

    public void registerWrong() {
        outcomes.wrongCombination();
    }

    public void registerTimeout() {
        outcomes.timeout();
    }

    public int successLucidityDelta() {
        return DarkRoomOutcomeHandler.SUCCESS_LUCIDITY;
    }

    public int successStressDelta() {
        return DarkRoomOutcomeHandler.SUCCESS_STRESS;
    }

    public boolean isDefeated() {
        return game.isDefeated();
    }

    /**
     * Ripristina corpo e statistiche mentali al checkpoint.
     *
     * I tratti già registrati non vengono cancellati, perché le domande
     * del piano non vengono ripetute durante il retry dello stesso piano.
     */
    public void restoreCheckpoint() {
        mind.restoreStats(
                checkpointLucidita,
                checkpointStress
        );

        game.setPlayerHealth(
                checkpointVita
        );
    }

    public int playerCurrentHealth() {
        return game.getPlayerCurrentHealth();
    }

    public int playerMaxHealth() {
        return game.getPlayerMaxHealth();
    }

    public int lucidita() {
        return mind.getLucidita();
    }

    public int stress() {
        return mind.getStress();
    }

    public int mindMax() {
        return MindState.MAX;
    }
}
