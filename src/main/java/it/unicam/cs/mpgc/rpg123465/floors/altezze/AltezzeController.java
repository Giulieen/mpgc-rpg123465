package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;

/**
 * Controller del Piano III — Le Altezze.
 *
 * Gestisce le conseguenze della traversata sul giocatore mantenendo
 * separata la logica di dominio dalla rappresentazione JavaFX.
 */
public final class AltezzeController {

    private static final int FALL_DAMAGE = 15;
    private static final int FALL_LUCIDITY = -6;
    private static final int FALL_STRESS = 22;

    private static final int SUCCESS_LUCIDITY = 5;
    private static final int SUCCESS_STRESS = -20;

    private final AltitudeCrossing crossing;
    private final GameController game;
    private final MindState mind;

    private int checkpointLucidita;
    private int checkpointStress;
    private int checkpointVita;

    /**
     * @param crossing contenuto del piano
     * @param game controller della partita
     * @throws IllegalArgumentException se un parametro è null
     */
    public AltezzeController(
            AltitudeCrossing crossing,
            GameController game
    ) {
        if (crossing == null || game == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.crossing = crossing;
        this.game = game;
        this.mind = game.getMind();
    }

    /**
     * Entra nel piano e fissa il checkpoint.
     */
    public void enter() {
        mind.enterRoom(
                crossing.config().initialStress()
        );

        checkpointLucidita =
                mind.getLucidita();

        checkpointStress =
                mind.getStress();

        checkpointVita =
                game.getPlayerCurrentHealth();
    }

    /**
     * Registra una risposta a un dilemma "Preferiresti".
     *
     * Il tratto associato alla risposta resta nascosto al giocatore.
     *
     * @param trait tratto associato alla risposta
     */
    public void registerChoice(
            ProfileTrait trait
    ) {
        mind.registerTrait(
                trait
        );
    }

    /**
     * Registra la vittoria.
     */
    public void registerVictory() {
        mind.harm(
                SUCCESS_LUCIDITY,
                SUCCESS_STRESS
        );
    }

    /**
     * Registra una caduta.
     */
    public void registerFall() {
        game.woundPlayer(
                FALL_DAMAGE
        );

        mind.harm(
                FALL_LUCIDITY,
                FALL_STRESS
        );
    }

    public int successLucidityDelta() {
        return SUCCESS_LUCIDITY;
    }

    public int successStressDelta() {
        return SUCCESS_STRESS;
    }

    public boolean isDefeated() {
        return game.isDefeated();
    }

    /**
     * Ripristina Vita, Lucidità e Stress al checkpoint.
     *
     * I tratti già registrati non vengono cancellati: in questo modo
     * una domanda già risposta non viene conteggiata due volte dopo
     * una caduta.
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
