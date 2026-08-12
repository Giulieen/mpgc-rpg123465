package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.domain.MindState;

/**
 * Applica le conseguenze mentali degli esiti del Piano II.
 *
 * Mantiene i valori di bilanciamento separati dal controller
 * e dalla rappresentazione JavaFX.
 */
public final class DarkRoomOutcomeHandler {

    public static final int SUCCESS_LUCIDITY = 5;
    public static final int SUCCESS_STRESS = -20;

    public static final int WRONG_LUCIDITY = -4;
    public static final int WRONG_STRESS = 18;

    public static final int TIMEOUT_LUCIDITY = -2;
    public static final int TIMEOUT_STRESS = 20;

    private final MindState mind;

    public DarkRoomOutcomeHandler(
            MindState mind
    ) {
        if (mind == null) {
            throw new IllegalArgumentException(
                    "Lo stato mentale non può essere null."
            );
        }

        this.mind = mind;
    }

    /**
     * Conseguenze della combinazione corretta.
     */
    public void success() {
        mind.harm(
                SUCCESS_LUCIDITY,
                SUCCESS_STRESS
        );
    }

    /**
     * Conseguenze di una combinazione errata.
     */
    public void wrongCombination() {
        mind.harm(
                WRONG_LUCIDITY,
                WRONG_STRESS
        );
    }

    /**
     * Conseguenze dello scadere del tempo.
     */
    public void timeout() {
        mind.harm(
                TIMEOUT_LUCIDITY,
                TIMEOUT_STRESS
        );
    }
}