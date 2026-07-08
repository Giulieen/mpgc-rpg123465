package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.Player;

/**
 * Rappresenta una singola scelta di un {@link DialogueEvent}.
 * <p>
 * Ogni scelta incapsula il proprio effetto sul giocatore (variazione di vita
 * ed eventuale oggetto ottenuto) e il messaggio da mostrare, così da evitare
 * logica e valori "hardcoded" sparsi altrove.
 */
public class DialogueChoice {

    private final String label;
    private final String message;
    private final int healthChange;
    private final Item reward;

    /**
     * Crea una scelta priva di oggetto in premio.
     *
     * @param label testo della scelta
     * @param message messaggio mostrato dopo la selezione
     * @param healthChange variazione di vita: negativa infligge danno,
     * positiva cura, zero non ha effetto
     */
    public DialogueChoice(String label, String message, int healthChange) {
        this(label, message, healthChange, null);
    }

    /**
     * Crea una scelta.
     *
     * @param label testo della scelta
     * @param message messaggio mostrato dopo la selezione
     * @param healthChange variazione di vita: negativa infligge danno,
     * positiva cura, zero non ha effetto
     * @param reward oggetto ottenuto con la scelta, oppure {@code null}
     */
    public DialogueChoice(String label, String message, int healthChange, Item reward) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Il testo della scelta non può essere vuoto.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Il messaggio della scelta non può essere vuoto.");
        }

        this.label = label;
        this.message = message;
        this.healthChange = healthChange;
        this.reward = reward;
    }

    /**
     * @return testo della scelta
     */
    public String getLabel() {
        return label;
    }

    /**
     * Applica l'effetto della scelta al giocatore.
     *
     * @param player giocatore su cui applicare l'effetto
     * @return messaggio descrittivo dell'esito
     */
    public String applyTo(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Il giocatore non può essere null.");
        }

        if (healthChange < 0) {
            player.takeDamage(-healthChange);
        } else if (healthChange > 0) {
            player.heal(healthChange);
        }

        if (reward != null) {
            player.addItem(reward);
        }

        return message;
    }

    @Override
    public String toString() {
        return label;
    }
}
