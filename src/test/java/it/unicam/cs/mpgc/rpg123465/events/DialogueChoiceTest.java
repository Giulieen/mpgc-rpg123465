package it.unicam.cs.mpgc.rpg123465.events;

import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.ItemType;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della classe {@link DialogueChoice}.
 */
class DialogueChoiceTest {

    private Player nuovoGiocatore() {
        return new Player("Viaggiatore", new Stats(100, 14, 5), new Inventory());
    }

    @Test
    void unaSceltaConEffettoNegativoInfliggeDanno() {
        Player player = nuovoGiocatore();
        DialogueChoice choice = new DialogueChoice("Reagisci", "Ti fai male.", -15);

        String message = choice.applyTo(player);

        assertEquals("Ti fai male.", message);
        assertEquals(85, player.getStats().getCurrentHealth());
    }

    @Test
    void unaSceltaConEffettoPositivoCura() {
        Player player = nuovoGiocatore();
        player.takeDamage(40);
        DialogueChoice choice = new DialogueChoice("Respira", "Ti calmi.", 20);

        choice.applyTo(player);

        assertEquals(80, player.getStats().getCurrentHealth());
    }

    @Test
    void unaSceltaPuoConcedereUnOggetto() {
        Player player = nuovoGiocatore();
        Item reward = new Item("Momento di Calma", "Un istante di quiete.", ItemType.HEALING, 15);
        DialogueChoice choice = new DialogueChoice("Osserva", "Trovi qualcosa.", 0, reward);

        choice.applyTo(player);

        assertEquals(1, player.getInventory().size());
        assertTrue(player.hasHealingItem());
    }

    @Test
    void unaSceltaSenzaTestoVieneRifiutata() {
        assertThrows(IllegalArgumentException.class,
                () -> new DialogueChoice(" ", "messaggio", 0));
    }

    @Test
    void applicareLaSceltaAUnGiocatoreNullVieneRifiutato() {
        DialogueChoice choice = new DialogueChoice("Scelta", "Effetto.", 0);

        assertThrows(IllegalArgumentException.class, () -> choice.applyTo(null));
    }
}
